/*
 * Copyright 2016 "Henry Tao <hi@henrytao.me>"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.henrytao.firechatengine.core;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by henrytao on 7/2/16.
 */
public class FirechatReference<T> {

  private final String mChild;

  private final Class<T> mClass;

  private final double mEndAt;

  private final int mLimitToLast;

  private final DatabaseReference mRef;

  private final PublishSubject<T> mSubject;

  protected FirechatReference(Class<T> zClass, String child, double endAt, int limitToLast) {
    mClass = zClass;
    mChild = child;
    mEndAt = endAt;
    mLimitToLast = limitToLast;

    mSubject = PublishSubject.create();
    mRef = FirebaseDatabase.getInstance().getReference().child(child);

    mSubject.doOnUnsubscribe(this::onUnsubscribe);
  }

  //public FirechatReference<T> next(int count) {
  //  return new FirechatReference<>(mChild, -1, count);
  //}

  public Observable<T> observe() {
    retrieve();
    return mSubject;
  }

  private void onUnsubscribe() {

  }

  private void retrieve() {
    Query query = mRef.orderByPriority();
    if (mEndAt > 0) {
      query = query.endAt(mEndAt);
    }
    if (mLimitToLast > 0) {
      query = query.limitToLast(mLimitToLast);
    }
    query.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onCancelled(DatabaseError databaseError) {
        mSubject.onError(databaseError.toException());
      }

      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {

        dataSnapshot.getValue(mClass);
      }
    });
  }

  public static class Builder<T> {

    private final String mChild;

    private final Class<T> mClass;

    private int mLimitToLast = 0;

    public Builder(Class<T> zClass, String child) {
      mClass = zClass;
      mChild = child;
    }

    public FirechatReference<T> build() {
      return new FirechatReference<>(mClass, mChild, 0, mLimitToLast);
    }

    public Builder<T> limitToLast(int limitToLast) {
      mLimitToLast = limitToLast;
      return this;
    }
  }
}
