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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import me.henrytao.firechatengine.utils.firechat.FirechatUtils;
import me.henrytao.firechatengine.utils.firechat.Wrapper;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by henrytao on 7/9/16.
 */
public class FirecacheReference<T> {

  public static <T> FirecacheReference<T> create(Class<T> tClass, DatabaseReference ref) {
    return new FirecacheReference<>(tClass, ref);
  }

  private final Cache mCache;

  private final Class<T> mClass;

  private final DatabaseReference mRef;

  private double mEndAt = Config.DEFAULT_END_AT;

  private Func1<Wrapper, Boolean> mFilter = wrapper -> wrapper != null;

  private int mLimitToLast = Config.DEFAULT_LIMIT_TO_LAST;

  private double mStartAt = Config.DEFAULT_START_AT;

  public FirecacheReference(Class<T> tClass, DatabaseReference ref) {
    mClass = tClass;
    mRef = ref;
    mCache = Cache.getInstance();
  }

  //public Observable<T> addChildEventListener() {
  //  return mCache.get(mClass, mRef, mStartAt, mEndAt, mLimitToLast)
  //      .onErrorReturn(throwable -> new ArrayList<>())
  //      .flatMap(cacheSnapshots -> {
  //        Observable<T> syncObservable = syncSnapshotAfterHavingCache(cacheSnapshots)
  //            .flatMap(syncDataSnapshots -> Observable.just(syncDataSnapshots)
  //                .mergeWith(createListenerIfNecessary(cacheSnapshots, syncDataSnapshots)));
  //        return Observable.just(cacheSnapshots)
  //            .map(this::getValue)
  //            .flatMapIterable(ts -> ts)
  //            .mergeWith(syncObservable.flatMap(t -> mCache.set(mClass, mRef, )));
  //      });
  //
  //  Observable<Wrapper> observable = mCache.get(mRef, mStartAt, mEndAt, mLimitToLast)
  //      .onErrorReturn(throwable -> new ArrayList<>())
  //      .flatMap(cachedSnapshots -> {
  //        Observable<Wrapper> syncObservable =
  //            syncSnapshotAfterHavingCache(cachedSnapshots)
  //                .flatMap(newSnapshots -> Observable.just(newSnapshots)
  //                    .flatMapIterable(dataSnapshots -> dataSnapshots)
  //                    .map(Wrapper::create)
  //                    .mergeWith(createListenerIfNecessary(cachedSnapshots, newSnapshots)));
  //        return Observable
  //            .just(cachedSnapshots)
  //            .flatMapIterable(dataSnapshots -> dataSnapshots)
  //            .map(Wrapper::create)
  //            .mergeWith(syncObservable
  //                .flatMap(wrapper -> mCache
  //                    .set(mRef, wrapper.dataSnapshot.getKey(), wrapper.dataSnapshot)
  //                    .map(dataSnapshot -> Wrapper.create(dataSnapshot, wrapper.type))));
  //      });
  //  mSubscriptionManager.manageSubscription(observable
  //      .compose(Transformer.applyJobExecutorScheduler())
  //      .subscribe(wrapper -> {
  //        switch (wrapper.type) {
  //          case ON_CHILD_ADDED:
  //            mChildEventListener.onChildAdded(wrapper.dataSnapshot, wrapper.previousKey);
  //            break;
  //          case ON_CHILD_CHANGED:
  //            mChildEventListener.onChildChanged(wrapper.dataSnapshot, wrapper.previousKey);
  //            break;
  //          case ON_CHILD_MOVED:
  //            mChildEventListener.onChildMoved(wrapper.dataSnapshot, wrapper.previousKey);
  //            break;
  //          case ON_CHILD_REMOVED:
  //            mChildEventListener.onChildRemoved(wrapper.dataSnapshot);
  //            break;
  //        }
  //      }, throwable -> {
  //        if (throwable instanceof DatabaseErrorException) {
  //          mChildEventListener.onCancelled(((DatabaseErrorException) throwable).getException());
  //        } else {
  //          throwable.printStackTrace();
  //        }
  //      }));
  //}

  //public Observable<T> addListenerForSingleValueEvent() {
  //  return FirechatUtils
  //      .observeSingleValueEvent(mClass, getQuery())
  //      .filter(mFilter::call)
  //      .map(wrapper -> {
  //        List<T> data = new ArrayList<>();
  //        if (wrapper.dataSnapshot.getChildrenCount() > 0) {
  //          for (DataSnapshot snapshot : wrapper.dataSnapshot.getChildren()) {
  //            data.add(snapshot.getValue(mClass));
  //          }
  //        } else {
  //          data.add(wrapper.dataSnapshot.getValue(mClass));
  //        }
  //        return data;
  //      })
  //      .flatMapIterable(ts -> ts);
  //}
  //
  //public Observable<T> addValueEventListener() {
  //  return mCache.get(mClass, mRef)
  //      .map(this::getValue)
  //      .onErrorReturn(throwable -> null)
  //      .mergeWith(FirechatUtils
  //          .observeValueEvent(getQuery())
  //          .filter(mFilter::call)
  //          .flatMap(wrapper -> mCache.set(mClass, mRef, wrapper.dataSnapshot)
  //              .map(aVoid -> getValue(wrapper)))
  //      )
  //      .distinctUntilChanged();
  //}

  public FirecacheReference<T> endAt(double endAt) {
    mEndAt = endAt;
    return this;
  }

  public FirecacheReference<T> filter(Func1<Wrapper, Boolean> filter) {
    mFilter = filter;
    return this;
  }

  public FirecacheReference<T> limitToLast(int limitToLast) {
    mLimitToLast = limitToLast;
    return this;
  }

  public FirecacheReference<T> startAt(double startAt) {
    mStartAt = startAt;
    return this;
  }

  //private Observable<Wrapper> createListenerIfNecessary(List<Cache.CacheSnapshot<T>> cacheSnapshots, List<DataSnapshot> dataSnapshots) {
  //  return Observable.just(null)
  //      .flatMap(o -> {
  //        if (mEndAt != Config.DEFAULT_END_AT) {
  //          return Observable.create(SubscriptionUtils::onComplete);
  //        } else {
  //          Cache.CacheSnapshot<T> lastCacheSnapshot = cacheSnapshots.size() > 0 ? cacheSnapshots.get(cacheSnapshots.size() - 1) : null;
  //          DataSnapshot lastDataSnapshot = dataSnapshots.size() > 0 ? dataSnapshots.get(dataSnapshots.size() - 1) : null;
  //          double startAt = lastDataSnapshot != null ?
  //              FirechatUtils.getPriority(lastDataSnapshot) + 1 :
  //              (lastCacheSnapshot != null ? lastCacheSnapshot.priority + 1 : Config.DEFAULT_START_AT);
  //          return FirechatUtils
  //              .observeChildEvent(FirechatUtils.getQuery(
  //                  mRef.orderByPriority(),
  //                  startAt,
  //                  Config.DEFAULT_END_AT,
  //                  startAt != Config.DEFAULT_START_AT ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast));
  //        }
  //      });
  //}

  //protected void loadChildEvent() {
  //  if (mChildEventListener == null) {
  //    return;
  //  }
  //  Observable<Wrapper> observable = mCache.get(mRef, mStartAt, mEndAt, mLimitToLast)
  //      .onErrorReturn(throwable -> new ArrayList<>())
  //      .flatMap(cachedSnapshots -> {
  //        Observable<Wrapper> syncObservable =
  //            syncSnapshotAfterHavingCache(cachedSnapshots)
  //                .flatMap(newSnapshots -> Observable.just(newSnapshots)
  //                    .flatMapIterable(dataSnapshots -> dataSnapshots)
  //                    .map(Wrapper::create)
  //                    .mergeWith(createListenerIfNecessary(cachedSnapshots, newSnapshots)));
  //        return Observable
  //            .just(cachedSnapshots)
  //            .flatMapIterable(dataSnapshots -> dataSnapshots)
  //            .map(Wrapper::create)
  //            .mergeWith(syncObservable
  //                .flatMap(wrapper -> mCache
  //                    .set(mRef, wrapper.dataSnapshot.getKey(), wrapper.dataSnapshot)
  //                    .map(dataSnapshot -> Wrapper.create(dataSnapshot, wrapper.type))));
  //      });
  //  mSubscriptionManager.manageSubscription(observable
  //      .compose(Transformer.applyJobExecutorScheduler())
  //      .subscribe(wrapper -> {
  //        switch (wrapper.type) {
  //          case ON_CHILD_ADDED:
  //            mChildEventListener.onChildAdded(wrapper.dataSnapshot, wrapper.previousKey);
  //            break;
  //          case ON_CHILD_CHANGED:
  //            mChildEventListener.onChildChanged(wrapper.dataSnapshot, wrapper.previousKey);
  //            break;
  //          case ON_CHILD_MOVED:
  //            mChildEventListener.onChildMoved(wrapper.dataSnapshot, wrapper.previousKey);
  //            break;
  //          case ON_CHILD_REMOVED:
  //            mChildEventListener.onChildRemoved(wrapper.dataSnapshot);
  //            break;
  //        }
  //      }, throwable -> {
  //        if (throwable instanceof DatabaseErrorException) {
  //          mChildEventListener.onCancelled(((DatabaseErrorException) throwable).getException());
  //        } else {
  //          throwable.printStackTrace();
  //        }
  //      }));
  //}

  private Query getQuery() {
    Query query = mRef.orderByPriority();
    if (mStartAt != Config.DEFAULT_START_AT) {
      query = query.startAt(mStartAt);
    }
    if (mEndAt != Config.DEFAULT_END_AT) {
      query = query.endAt(mEndAt);
    }
    if (mLimitToLast != Config.DEFAULT_LIMIT_TO_LAST) {
      query = query.limitToLast(mLimitToLast);
    }
    return query;
  }

  private T getValue(Cache.CacheSnapshot<T> cacheSnapshot) {
    T data = cacheSnapshot.data;
    if (data instanceof BaseModel) {
      ((BaseModel) data).setPriority(cacheSnapshot.priority);
    }
    return data;
  }

  private T getValue(DataSnapshot dataSnapshot) {
    T data = dataSnapshot.getValue(mClass);
    if (data instanceof BaseModel) {
      ((BaseModel) data).setPriority(FirechatUtils.getPriority(dataSnapshot));
    }
    return data;
  }

  //private T getValue(Wrapper wrapper) {
  //  return getValue(wrapper.dataSnapshot);
  //}

  private List<T> getValue(List<Cache.CacheSnapshot<T>> cacheSnapshots) {
    List<T> data = new ArrayList<>();
    for (Cache.CacheSnapshot<T> cacheSnapshot : cacheSnapshots) {
      data.add(getValue(cacheSnapshot));
    }
    return data;
  }

  //private Observable<List<DataSnapshot>> syncSnapshotAfterHavingCache(List<Cache.CacheSnapshot<T>> cacheSnapshots) {
  //  return Observable.just(null).flatMap(o -> {
  //    Cache.CacheSnapshot<T> lastCacheSnapshot = cacheSnapshots.size() > 0 ? cacheSnapshots.get(cacheSnapshots.size() - 1) : null;
  //    return FirechatUtils
  //        .observeSingleValueEvent(FirechatUtils.getQuery(
  //            mRef.orderByPriority(),
  //            lastCacheSnapshot != null ? lastCacheSnapshot.priority + 1 : Config.DEFAULT_START_AT,
  //            mEndAt,
  //            mEndAt != Config.DEFAULT_END_AT ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast))
  //        .map(wrapper -> {
  //          List<DataSnapshot> data = new ArrayList<>();
  //          for (DataSnapshot snapshot : wrapper.dataSnapshot.getChildren()) {
  //            data.add(snapshot);
  //          }
  //          return data;
  //        });
  //  });
  //}
}
