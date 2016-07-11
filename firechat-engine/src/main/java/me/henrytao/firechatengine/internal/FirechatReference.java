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

package me.henrytao.firechatengine.internal;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.henrytao.firechatengine.utils.firechat.FirechatUtils;
import me.henrytao.firechatengine.utils.rx.SubscriptionManager;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by henrytao on 7/2/16.
 */
public class FirechatReference<T> {

  private static final double DEFAULT_END_AT = 0;

  private static final int DEFAULT_LIMIT_TO_LAST = 0;

  private static final double DEFAULT_START_AT = 0;

  private final String mChild;

  private final Class<T> mClass;

  private final BehaviorSubject<Double> mEndAtSubject;

  private final int mLimitToLast;

  private final BehaviorSubject<Double> mNextEndAtSubject;

  private final BehaviorSubject<Double> mNextStartAtSubject;

  private final BehaviorSubject<Double> mStartAtSubject;

  private final SubscriptionManager mSubscriptionManager;

  public FirechatReference(Class<T> zClass, String child, int limitToLast, @NonNull BehaviorSubject<Double> startAtSubject,
      @NonNull BehaviorSubject<Double> endAtSubject) {
    mClass = zClass;
    mChild = child;
    mLimitToLast = limitToLast;
    mStartAtSubject = startAtSubject;
    mEndAtSubject = endAtSubject;

    mNextStartAtSubject = BehaviorSubject.create();
    mNextEndAtSubject = BehaviorSubject.create();
    mSubscriptionManager = new SubscriptionManager();
  }

  public FirechatReference<T> next(int count) {
    return new FirechatReference<>(mClass, mChild, count, mNextStartAtSubject, mNextEndAtSubject);
  }

  public Observable<T> observe() {
    Observable<T> observable = Observable.create(subscriber -> {
      mSubscriptionManager.manageSubscription(retrieve().subscribe(
          t -> SubscriptionUtils.onNext(subscriber, t),
          throwable -> SubscriptionUtils.onError(subscriber, throwable)));
    });
    return observable.doOnUnsubscribe(this::onUnsubscribe);
  }

  private Query getQuery() {
    return FirebaseDatabase.getInstance().getReference().child(mChild).orderByPriority();
  }

  private void onUnsubscribe() {
    mStartAtSubject.onCompleted();
    mEndAtSubject.onCompleted();
    mNextStartAtSubject.onCompleted();
    mNextEndAtSubject.onCompleted();
    mSubscriptionManager.unsubscribe();
  }

  private Observable<T> retrieve() {
    return null;
    //return mStartAtSubject
    //    .flatMap(startAt -> {
    //      if (startAt == DEFAULT_START_AT) {
    //        return FirechatUtils.observeSingleValueEvent(getQuery().limitToFirst(1)).map(dataSnapshot -> {
    //          for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
    //            return FirechatUtils.getPriority(snapshot);
    //          }
    //          return DEFAULT_END_AT;
    //        });
    //      }
    //      return Observable.just(startAt);
    //    })
    //    .zipWith(mEndAtSubject, (startAt, endAt) -> {
    //      Query query = getQuery();
    //      if (startAt > DEFAULT_START_AT) {
    //        query = query.startAt(startAt);
    //      }
    //      if (endAt > DEFAULT_END_AT) {
    //        query = query.endAt(endAt);
    //      }
    //      if (mLimitToLast > DEFAULT_LIMIT_TO_LAST) {
    //        query = query.limitToLast(mLimitToLast);
    //      }
    //      return FirechatUtils.observeSingleValueEvent(query)
    //          .flatMap(dataSnapshot -> {
    //            List<DataSnapshot> data = new ArrayList<>();
    //            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
    //              data.add(snapshot);
    //            }
    //            Query listener = null;
    //            int size = data.size();
    //            if (size == 0) {
    //              if (endAt == DEFAULT_END_AT) {
    //                listener = getQuery();
    //                if (startAt > DEFAULT_START_AT) {
    //                  listener = listener.startAt(startAt);
    //                }
    //                mNextStartAtSubject.onNext(startAt);
    //                mNextEndAtSubject.onNext(FirechatUtils.getPriority(data.get(0)) - 1d);
    //              }
    //            } else {
    //
    //            }
    //
    //            if (size == 0 && endAt == DEFAULT_END_AT) {
    //
    //            } else if (size > 0) {
    //
    //            }
    //
    //            mNextStartAtSubject.onNext(startAt);
    //            mNextEndAtSubject.onNext(FirechatUtils.getPriority(data.get(0)) - 1d);
    //            if (endAt == DEFAULT_END_AT) {
    //              listener = getQuery().startAt(FirechatUtils.getPriority(data.get(data.size() - 1)) + 1d);
    //            }
    //            return Observable.just(data)
    //                .flatMapIterable(dataSnapshots -> dataSnapshots);
    //                //.mergeWith(FirechatUtils.observeChildEvent(listener));
    //          });
    //    })
    //    .flatMap(observable -> observable)
    //    .map(dataSnapshot -> dataSnapshot.getValue(mClass));
  }

  public static class Builder<T> {

    private final String mChild;

    private final Class<T> mClass;

    private double mEndAt = DEFAULT_END_AT;

    private int mLimitToLast = DEFAULT_LIMIT_TO_LAST;

    private double mStartAt = DEFAULT_START_AT;

    public Builder(Class<T> zClass, String child) {
      mClass = zClass;
      mChild = child;
    }

    public FirechatReference<T> build() {
      return new FirechatReference<>(mClass, mChild,
          mLimitToLast,
          BehaviorSubject.create(mStartAt),
          BehaviorSubject.create(mEndAt));
    }

    public Builder<T> endAt(double endAt) {
      mEndAt = endAt;
      return this;
    }

    public Builder<T> limitToLast(int limitToLast) {
      mLimitToLast = limitToLast;
      return this;
    }

    public Builder<T> startAt(double startAt) {
      mStartAt = startAt;
      return this;
    }
  }
}
