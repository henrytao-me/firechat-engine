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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
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

  private final Cache mCache;

  private final double mEndAt;

  private final int mLimitToLast;

  private final DatabaseReference mRef;

  private final double mStartAt;

  private final SubscriptionManager mSubscriptionManager;

  private ChildEventListener mChildEventListener;

  private Query mQuery;

  private ValueEventListener mSingleValueEventListener;

  private ValueEventListener mValueEventListener;

  protected FirecacheReference(DatabaseReference ref, double startAt, double endAt, int limitToLast,
      ValueEventListener singleValueEventListener, ValueEventListener valueEventListener, ChildEventListener childEventListener) {
    mRef = ref;
    mStartAt = startAt;
    mEndAt = endAt;
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
    if (mStartAt != Config.DEFAULT_START_AT) {
      mQuery = mQuery.startAt(mStartAt);
    }
    if (mEndAt != Config.DEFAULT_END_AT) {
      mQuery = mQuery.endAt(mEndAt);
    }
    if (mLimitToLast != Config.DEFAULT_LIMIT_TO_LAST) {
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

  protected void loadChildEvent() {
    if (mChildEventListener == null) {
      return;
    }
    Observable<Wrapper> observable = mCache.get(mRef, mStartAt, mEndAt, mLimitToLast)
        .onErrorReturn(throwable -> new ArrayList<>())
        .flatMap(cachedSnapshots -> {
          Observable<Wrapper> syncObservable =
              syncSnapshotAfterHavingCache(cachedSnapshots)
                  .flatMap(newSnapshots -> Observable.just(newSnapshots)
                      .flatMapIterable(dataSnapshots -> dataSnapshots)
                      .map(Wrapper::create)
                      .mergeWith(createListenerIfNecessary(cachedSnapshots, newSnapshots)));
          return Observable
              .just(cachedSnapshots)
              .flatMapIterable(dataSnapshots -> dataSnapshots)
              .map(Wrapper::create)
              .mergeWith(syncObservable
                  .flatMap(wrapper -> mCache
                      .set(mRef, wrapper.dataSnapshot.getKey(), wrapper.dataSnapshot)
                      .map(dataSnapshot -> Wrapper.create(dataSnapshot, wrapper.type))));
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

  protected void loadSingleValueEvent() {
    if (mSingleValueEventListener == null) {
      return;
    }
    mSubscriptionManager.manageSubscription(FirechatUtils.observeSingleValueEvent(mQuery)
        .compose(Transformer.applyJobExecutorScheduler())
        .subscribe(wrapper -> mSingleValueEventListener.onDataChange(wrapper.dataSnapshot), throwable -> {
          if (throwable instanceof DatabaseErrorException) {
            mSingleValueEventListener.onCancelled(((DatabaseErrorException) throwable).getException());
          } else {
            throwable.printStackTrace();
          }
        }));
  }

  protected void loadValueEvent() {
    if (mValueEventListener == null) {
      return;
    }
    final Stack<DataSnapshot> stack = new Stack<>();
    Observable<DataSnapshot> observable = mCache.get(mRef)
        .onErrorReturn(throwable -> null)
        .mergeWith(FirechatUtils.observeValueEvent(mQuery).map(wrapper -> wrapper.dataSnapshot))
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
        .flatMap(dataSnapshot -> mCache.set(mRef, dataSnapshot))
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

  protected void removeEventListeners(boolean force) {
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

  private Observable<Wrapper> createListenerIfNecessary(List<DataSnapshot> cachedSnapshots, List<DataSnapshot> newSnapshots) {
    return Observable.just(null)
        .flatMap(o -> {
          if (mEndAt != Config.DEFAULT_END_AT) {
            return Observable.create(SubscriptionUtils::onComplete);
          } else {
            DataSnapshot lastCachedSnapshot = cachedSnapshots.size() > 0 ? cachedSnapshots.get(cachedSnapshots.size() - 1) : null;
            DataSnapshot lastNewSnapshot = newSnapshots.size() > 0 ? newSnapshots.get(newSnapshots.size() - 1) : null;
            double startAt = lastNewSnapshot != null ? FirechatUtils.getPriority(lastNewSnapshot) + 1 :
                (lastCachedSnapshot != null ? FirechatUtils.getPriority(lastCachedSnapshot) + 1 : Config.DEFAULT_START_AT);
            return FirechatUtils
                .observeChildEvent(FirechatUtils.getQuery(
                    mRef.orderByPriority(),
                    startAt,
                    Config.DEFAULT_END_AT,
                    startAt != Config.DEFAULT_START_AT ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast));
          }
        });
  }

  private Observable<List<DataSnapshot>> syncSnapshotAfterHavingCache(List<DataSnapshot> cachedSnapshots) {
    return Observable.just(null).flatMap(o -> {
      DataSnapshot lastCachedSnapshot = cachedSnapshots.size() > 0 ? cachedSnapshots.get(cachedSnapshots.size() - 1) : null;
      return FirechatUtils
          .observeSingleValueEvent(FirechatUtils.getQuery(
              mRef.orderByPriority(),
              lastCachedSnapshot != null ? FirechatUtils.getPriority(lastCachedSnapshot) + 1 : Config.DEFAULT_START_AT,
              mEndAt,
              mEndAt != Config.DEFAULT_END_AT ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast))
          .map(wrapper -> {
            List<DataSnapshot> data = new ArrayList<>();
            for (DataSnapshot snapshot : wrapper.dataSnapshot.getChildren()) {
              data.add(snapshot);
            }
            return data;
          });
    });
  }

  public static class Builder {

    private final DatabaseReference mRef;

    private ChildEventListener mChildEventListener;

    private double mEndAt = Config.DEFAULT_END_AT;

    private int mLimitToLast = Config.DEFAULT_LIMIT_TO_LAST;

    private ValueEventListener mSingleValueEventListener;

    private double mStartAt = Config.DEFAULT_START_AT;

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
      FirecacheReference reference = new FirecacheReference(mRef, mStartAt, mEndAt, mLimitToLast,
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
