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

package me.henrytao.firechatengine.internal.firecache;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Stack;

import me.henrytao.firechatengine.internal.exception.DatabaseErrorException;
import me.henrytao.firechatengine.utils.firechat.FirechatUtils;
import me.henrytao.firechatengine.utils.rx.SubscriptionManager;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import me.henrytao.firechatengine.utils.rx.Transformer;
import rx.Observable;

/**
 * Created by henrytao on 7/9/16.
 */
public class FirecacheReference {

  private static final double DEFAULT_END_AT = 0;

  private static final int DEFAULT_LIMIT_TO_FIRST = 0;

  private static final int DEFAULT_LIMIT_TO_LAST = 0;

  private static final double DEFAULT_START_AT = 0;

  private final Cache mCache;

  private final double mEndAt;

  private final int mLimitToFirst;

  private final int mLimitToLast;

  private final DatabaseReference mRef;

  private final double mStartAt;

  private final SubscriptionManager mSubscriptionManager;

  private ChildEventListener mChildEventListener;

  private Query mQuery;

  private ValueEventListener mSingleValueEventListener;

  private ValueEventListener mValueEventListener;

  protected FirecacheReference(DatabaseReference ref, double startAt, double endAt, int limitToFirst, int limitToLast,
      ValueEventListener singleValueEventListener, ValueEventListener valueEventListener, ChildEventListener childEventListener) {
    mRef = ref;
    mStartAt = startAt;
    mEndAt = endAt;
    mLimitToFirst = limitToFirst;
    mLimitToLast = limitToLast;
    mSingleValueEventListener = singleValueEventListener;
    mValueEventListener = valueEventListener;
    mChildEventListener = childEventListener;

    mCache = Cache.getInstance();
    mSubscriptionManager = new SubscriptionManager();

    reload();
  }

  public void reload() {
    mQuery = mRef.orderByPriority();
    if (mStartAt > DEFAULT_START_AT) {
      mQuery = mQuery.startAt(mStartAt);
    }
    if (mEndAt > DEFAULT_END_AT) {
      mQuery = mQuery.endAt(mEndAt);
    }
    if (mLimitToFirst > DEFAULT_LIMIT_TO_FIRST) {
      mQuery = mQuery.limitToFirst(mLimitToFirst);
    }
    if (mLimitToLast > DEFAULT_LIMIT_TO_LAST) {
      mQuery = mQuery.limitToLast(mLimitToLast);
    }
    removeEventListeners(false);
    loadChildEvent();
    loadSingleValueEvent();
    loadValueEvent();
  }

  public void removeEventListeners() {
    removeEventListeners(true);
  }

  private void loadChildEvent() {
    if (mChildEventListener == null) {
      return;
    }
    Observable<DataSnapshotWrapper> observable = mCache.get(mQuery, mStartAt, mEndAt, mLimitToFirst, mLimitToLast)
        .onErrorReturn(throwable -> new ArrayList<>())
        .flatMap(cachedSnapshots -> {
          Query query = mQuery;
          if (cachedSnapshots.size() > 0) {
            double cachedStartAt = FirechatUtils.getPriority(cachedSnapshots.get(0));
            double cachedEndAt = FirechatUtils.getPriority(cachedSnapshots.get(cachedSnapshots.size() - 1));
            if (mLimitToFirst > DEFAULT_LIMIT_TO_FIRST) {
              query = mRef.orderByPriority().endAt(cachedStartAt - 1);
            } else {
              query = mRef.orderByPriority().startAt(cachedEndAt + 1);
            }
          }
          final Query listener = query;
          return Observable
              .just(cachedSnapshots)
              .flatMapIterable(dataSnapshots -> dataSnapshots)
              .map(dataSnapshot -> new DataSnapshotWrapper(dataSnapshot, DataSnapshotWrapper.Type.ON_CHILD_ADDED))
              .mergeWith(Observable.create(subscriber -> {
                listener.addChildEventListener(new ChildEventListener() {
                  @Override
                  public void onCancelled(DatabaseError databaseError) {
                    SubscriptionUtils.onError(subscriber, new DatabaseErrorException(databaseError));
                  }

                  @Override
                  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    SubscriptionUtils.onNext(subscriber, new DataSnapshotWrapper(dataSnapshot, s, DataSnapshotWrapper.Type.ON_CHILD_ADDED));
                  }

                  @Override
                  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    SubscriptionUtils
                        .onNext(subscriber, new DataSnapshotWrapper(dataSnapshot, s, DataSnapshotWrapper.Type.ON_CHILD_CHANGED));
                  }

                  @Override
                  public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    SubscriptionUtils.onNext(subscriber, new DataSnapshotWrapper(dataSnapshot, s, DataSnapshotWrapper.Type.ON_CHILD_MOVED));
                  }

                  @Override
                  public void onChildRemoved(DataSnapshot dataSnapshot) {
                    SubscriptionUtils.onNext(subscriber, new DataSnapshotWrapper(dataSnapshot, DataSnapshotWrapper.Type.ON_CHILD_REMOVED));
                  }
                });
              }));
        });
    mSubscriptionManager.manageSubscription(observable
        .compose(Transformer.applyJobExecutorScheduler())
        .subscribe(wrapper -> {
          switch (wrapper.type) {
            case ON_CHILD_ADDED:
              mChildEventListener.onChildAdded(wrapper.dataSnapshot, wrapper.previousKey);
              break;
            case ON_CHILD_CHANGED:
              mChildEventListener.onChildChanged(wrapper.dataSnapshot, wrapper.previousKey);
              break;
            case ON_CHILD_MOVED:
              mChildEventListener.onChildMoved(wrapper.dataSnapshot, wrapper.previousKey);
              break;
            case ON_CHILD_REMOVED:
              mChildEventListener.onChildRemoved(wrapper.dataSnapshot);
              break;
          }
        }, throwable -> {
          if (throwable instanceof DatabaseErrorException) {
            mChildEventListener.onCancelled(((DatabaseErrorException) throwable).getException());
          } else {
            throwable.printStackTrace();
          }
        }));
  }

  private void loadSingleValueEvent() {
    if (mSingleValueEventListener == null) {
      return;
    }
    Observable<DataSnapshot> observable = Observable.create(subscriber -> {
      mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onCancelled(DatabaseError databaseError) {
          SubscriptionUtils.onError(subscriber, new DatabaseErrorException(databaseError));
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          SubscriptionUtils.onNextAndComplete(subscriber, dataSnapshot);
        }
      });
    });
    mSubscriptionManager.manageSubscription(observable
        .compose(Transformer.applyJobExecutorScheduler())
        .subscribe(mSingleValueEventListener::onDataChange, throwable -> {
          if (throwable instanceof DatabaseErrorException) {
            mSingleValueEventListener.onCancelled(((DatabaseErrorException) throwable).getException());
          } else {
            throwable.printStackTrace();
          }
        }));
  }

  private void loadValueEvent() {
    if (mValueEventListener == null) {
      return;
    }
    final Stack<DataSnapshot> stack = new Stack<>();
    Observable<DataSnapshot> observable = mCache.get(mQuery)
        .onErrorReturn(throwable -> null)
        .mergeWith(Observable.create(subscriber -> {
          mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
              SubscriptionUtils.onError(subscriber, new DatabaseErrorException(databaseError));
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              SubscriptionUtils.onNext(subscriber, dataSnapshot);
            }
          });
        }))
        .filter(dataSnapshot -> {
          if (dataSnapshot == null) {
            return false;
          }
          if (stack.size() == 0) {
            stack.push(dataSnapshot);
            return true;
          }
          DataSnapshot previousDataSnapshot = stack.pop();
          stack.push(dataSnapshot);
          return dataSnapshot.getPriority() != previousDataSnapshot.getPriority()
              || dataSnapshot.getValue() != previousDataSnapshot.getValue();
        })
        .flatMap(dataSnapshot -> mCache.set(mQuery, dataSnapshot))
        .doOnUnsubscribe(stack::clear);
    mSubscriptionManager.manageSubscription(observable
        .compose(Transformer.applyJobExecutorScheduler())
        .subscribe(dataSnapshot -> {
          mValueEventListener.onDataChange(dataSnapshot);
        }, throwable -> {
          if (throwable instanceof DatabaseErrorException) {
            mValueEventListener.onCancelled(((DatabaseErrorException) throwable).getException());
          } else {
            throwable.printStackTrace();
          }
        }));
  }

  private void removeEventListeners(boolean force) {
    if (mSingleValueEventListener != null) {
      mRef.removeEventListener(mSingleValueEventListener);
    }
    if (mValueEventListener != null) {
      mRef.removeEventListener(mValueEventListener);
    }
    if (mChildEventListener != null) {
      mRef.removeEventListener(mChildEventListener);
    }
    if (force) {
      mSingleValueEventListener = null;
      mValueEventListener = null;
      mChildEventListener = null;
    }
    mSubscriptionManager.unsubscribe();
  }

  public static class Builder {

    private final DatabaseReference mRef;

    private ChildEventListener mChildEventListener;

    private double mEndAt = DEFAULT_END_AT;

    private int mLimitToFirst;

    private int mLimitToLast = DEFAULT_LIMIT_TO_LAST;

    private ValueEventListener mSingleValueEventListener;

    private double mStartAt = DEFAULT_START_AT;

    private ValueEventListener mValueEventListener;

    public Builder(DatabaseReference ref) {
      mRef = ref;
    }

    public Builder addChildEventListener(ChildEventListener childEventListener) {
      mChildEventListener = childEventListener;
      return this;
    }

    public Builder addListenerForSingleValueEvent(ValueEventListener valueEventListener) {
      mSingleValueEventListener = valueEventListener;
      return this;
    }

    public Builder addValueEventListener(ValueEventListener valueEventListener) {
      mValueEventListener = valueEventListener;
      return this;
    }

    public FirecacheReference build() {
      FirecacheReference reference = new FirecacheReference(mRef, mStartAt, mEndAt, mLimitToFirst, mLimitToLast,
          mSingleValueEventListener, mValueEventListener, mChildEventListener);
      mSingleValueEventListener = null;
      mValueEventListener = null;
      mChildEventListener = null;
      return reference;
    }

    public Builder endAt(double endAt) {
      mEndAt = endAt;
      return this;
    }

    public Builder limitToFirst(int limitToFirst) {
      mLimitToFirst = limitToFirst;
      return this;
    }

    public Builder limitToLast(int limitToLast) {
      mLimitToLast = limitToLast;
      return this;
    }

    public Builder startAt(double startAt) {
      mStartAt = startAt;
      return this;
    }
  }
}
