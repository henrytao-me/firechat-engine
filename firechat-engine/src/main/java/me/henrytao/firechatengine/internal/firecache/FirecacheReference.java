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

  private Func1<Wrapper<T>, Boolean> mFilter = wrapper -> wrapper != null;

  private int mLimitToLast = Config.DEFAULT_LIMIT_TO_LAST;

  private double mStartAt = Config.DEFAULT_START_AT;

  public FirecacheReference(Class<T> tClass, DatabaseReference ref) {
    mClass = tClass;
    mRef = ref;
    mCache = Cache.getInstance();
  }

  public Observable<T> addChildEventListener() {
    return mCache.get(mClass, mRef.toString(), mStartAt, mEndAt, mLimitToLast)
        .onErrorReturn(throwable -> new ArrayList<>())
        .flatMap(caches -> {
          Observable<Wrapper<T>> syncObservable = syncDataAfterHavingCache(caches)
              .flatMap(syncs -> Observable.just(syncs)
                  .flatMapIterable(wrappers -> wrappers)
                  .mergeWith(createListenerIfNecessary(caches, syncs)))
              .flatMap(wrapper -> mCache.set(mRef.toString(), wrapper.key, wrapper).map(aVoid -> wrapper));
          return Observable.just(caches)
              .flatMapIterable(wrappers -> wrappers)
              .mergeWith(syncObservable)
              .filter(mFilter::call)
              .map(wrapper -> wrapper.data);
        });
  }

  public Observable<T> addListenerForSingleValueEvent() {
    return FirechatUtils
        .observeSingleValueEvent(mClass, getQuery())
        .filter(mFilter::call)
        .map(wrapper -> wrapper.data);
  }

  public Observable<T> addValueEventListener() {
    return mCache.get(mClass, mRef.toString())
        .onErrorReturn(throwable -> null)
        .mergeWith(FirechatUtils
            .observeValueEvent(mClass, getQuery())
            .flatMap(wrapper -> {
              return mCache.set(mRef.toString(), wrapper).map(aVoid -> {
                return wrapper;
              });
            })
        )
        .filter(wrapper -> wrapper != null)
        .filter(mFilter::call)
        .map(wrapper -> wrapper.data)
        .distinctUntilChanged();
  }

  public FirecacheReference<T> endAt(double endAt) {
    mEndAt = endAt;
    return this;
  }

  public FirecacheReference<T> filter(Func1<Wrapper<T>, Boolean> filter) {
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

  private Observable<Wrapper<T>> createListenerIfNecessary(List<Wrapper<T>> caches, List<Wrapper<T>> syncs) {
    return Observable.just(null)
        .flatMap(o -> {
          if (mEndAt != Config.DEFAULT_END_AT) {
            return Observable.create(SubscriptionUtils::onComplete);
          } else {
            Wrapper<T> lastCache = caches.size() > 0 ? caches.get(caches.size() - 1) : null;
            Wrapper<T> lastSync = syncs.size() > 0 ? syncs.get(syncs.size() - 1) : null;
            double startAt = lastSync != null ?
                lastSync.priority + 1 :
                (lastCache != null ? lastCache.priority + 1 : Config.DEFAULT_START_AT);
            return FirechatUtils
                .observeChildEvent(mClass, FirechatUtils.getQuery(
                    mRef.orderByPriority(),
                    startAt,
                    Config.DEFAULT_END_AT,
                    startAt != Config.DEFAULT_START_AT ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast));
          }
        });
  }

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

  private Observable<List<Wrapper<T>>> syncDataAfterHavingCache(List<Wrapper<T>> caches) {
    return Observable.just(null).flatMap(o -> {
      Wrapper<T> lastCache = caches.size() > 0 ? caches.get(caches.size() - 1) : null;
      return FirechatUtils
          .observeSingleValueEvent(mClass, FirechatUtils.getQuery(
              mRef.orderByPriority(),
              lastCache != null ? lastCache.priority + 1 : Config.DEFAULT_START_AT,
              mEndAt,
              mEndAt != Config.DEFAULT_END_AT ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast))
          .toList();
    });
  }
}
