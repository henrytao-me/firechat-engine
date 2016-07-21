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

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import me.henrytao.firechatengine.internal.exception.NoDataFoundException;
import me.henrytao.firechatengine.utils.firechat.FirechatUtils;
import me.henrytao.firechatengine.utils.firechat.Wrapper;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import me.henrytao.firechatengine.utils.rx.Transformer;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

/**
 * Created by henrytao on 7/9/16.
 */
public class FirecacheReference<T> {

  public static <T> FirecacheReference<T> create(Class<T> tClass, DatabaseReference ref) {
    return new FirecacheReference<>(tClass, ref, false);
  }

  private final Cache mCache;

  private final Class<T> mClass;

  private final DatabaseReference mRef;

  private BehaviorSubject<Double> mEndAt = BehaviorSubject.create(Config.DEFAULT_END_AT);

  private Func1<Wrapper<T>, Boolean> mFilter = wrapper -> wrapper != null;

  private boolean mIsOnNext = false;

  private int mLimitToLast = Config.DEFAULT_LIMIT_TO_LAST;

  private BehaviorSubject<Double> mNextEndAt = BehaviorSubject.create();

  private boolean mShouldKeepSyncing = true;

  private BehaviorSubject<Double> mStartAt = BehaviorSubject.create(Config.DEFAULT_START_AT);

  protected FirecacheReference(Class<T> tClass, DatabaseReference ref, boolean isOnNext) {
    mClass = tClass;
    mRef = ref;
    mCache = Cache.getInstance();
    mIsOnNext = isOnNext;
  }

  public Observable<T> addChildEventListener() {
    Observable<T> observable = onReady()
        .flatMap(startEndAt -> mCache
            .getList(mClass, mRef.toString(), startEndAt.first, startEndAt.second, mLimitToLast)
            .onErrorReturn(throwable -> new ArrayList<>()))
        .flatMap(caches -> Observable
            .just(caches)
            .flatMapIterable(wrappers -> wrappers)
            .mergeWith(syncDataAfterHavingCache(caches)
                .flatMap(wrapper -> mCache.set(wrapper).map(aVoid -> Wrapper.clone((Wrapper<T>) wrapper))))
            .filter(mFilter::call)
            .map(wrapper -> wrapper.data));
    return observable.compose(Transformer.applyJobExecutorScheduler());
  }

  public Observable<T> addListenerForSingleValueEvent() {
    Observable<T> observable = onReady()
        .flatMap(startEndAt -> FirechatUtils.observeSingleValueEvent(mClass, getQuery()))
        .filter(mFilter::call)
        .map(wrapper -> wrapper.data);
    return observable.compose(Transformer.applyJobExecutorScheduler());
  }

  public Observable<T> addValueEventListener() {
    Observable<T> observable = onReady()
        .flatMap(startEndAt -> mCache.get(mClass, mRef.toString()))
        .onErrorReturn(throwable -> null)
        .mergeWith(FirechatUtils
            .observeValueEvent(mClass, getQuery())
            .flatMap(wrapper -> mCache.set(wrapper).map(aVoid -> Wrapper.clone((Wrapper<T>) wrapper)))
        )
        .filter(wrapper -> wrapper != null)
        .filter(mFilter::call)
        .map(wrapper -> wrapper.data)
        .distinctUntilChanged();
    return observable.compose(Transformer.applyJobExecutorScheduler());
  }

  public FirecacheReference<T> endAt(double endAt) {
    mEndAt.onNext(endAt);
    return this;
  }

  public FirecacheReference<T> endAt(BehaviorSubject<Double> endAt) {
    mEndAt = endAt;
    return this;
  }

  public FirecacheReference<T> filter(Func1<Wrapper<T>, Boolean> filter) {
    mFilter = filter;
    return this;
  }

  public FirecacheReference<T> keepSyncing(boolean keepSyncing) {
    mShouldKeepSyncing = keepSyncing;
    return this;
  }

  public FirecacheReference<T> limitToLast(int limitToLast) {
    mLimitToLast = limitToLast;
    return this;
  }

  public FirecacheReference<T> next() {
    return new FirecacheReference<>(mClass, mRef, true)
        .startAt(mStartAt)
        .endAt(mNextEndAt)
        .limitToLast(mLimitToLast)
        .keepSyncing(false);
  }

  public FirecacheReference<T> startAt(double startAt) {
    mStartAt.onNext(startAt);
    return this;
  }

  private Observable<Wrapper<T>> createListenerIfNecessary(List<Wrapper<T>> caches, List<Wrapper<T>> syncs) {
    return Observable.just(null).flatMap(o -> {
      List<Wrapper<T>> data = FirechatUtils.merge(caches, syncs);
      Wrapper<T> first = FirechatUtils.getFirstItem(data);
      Wrapper<T> last = FirechatUtils.getLastItem(data);
      if (!mShouldKeepSyncing) {
        mNextEndAt.onNext(first != null ? first.priority - 1 : Config.DEFAULT_END_AT);
        return Observable.create(SubscriptionUtils::onComplete);
      } else {
        if (first != null) {
          mNextEndAt.onNext(first.priority - 1);
        }
        return FirechatUtils.observeChildEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            last != null ? last.priority + 1 : mStartAt.getValue(),
            Config.DEFAULT_END_AT,
            last != null ? Config.DEFAULT_LIMIT_TO_LAST : mLimitToLast))
            .map(wrapper -> {
              if (!mNextEndAt.hasValue()) {
                mNextEndAt.onNext(wrapper.priority - 1);
              }
              return wrapper;
            });
      }
    });
  }

  private Query getQuery() {
    Query query = mRef.orderByPriority();
    if (mStartAt.getValue() != Config.DEFAULT_START_AT) {
      query = query.startAt(mStartAt.getValue());
    }
    if (mEndAt.getValue() != Config.DEFAULT_END_AT) {
      query = query.endAt(mEndAt.getValue());
    }
    if (mLimitToLast != Config.DEFAULT_LIMIT_TO_LAST) {
      query = query.limitToLast(mLimitToLast);
    }
    return query;
  }

  private Observable<Pair<Double, Double>> onReady() {
    return mStartAt.flatMap(startAt -> mEndAt.map(endAt -> new Pair<>(startAt, endAt))).first().flatMap(startEndAt -> {
      if (mIsOnNext && startEndAt.second == Config.DEFAULT_END_AT) {
        return Observable.error(new NoDataFoundException());
      }
      return Observable.just(startEndAt);
    });
  }

  private FirecacheReference<T> startAt(BehaviorSubject<Double> startAt) {
    mStartAt = startAt;
    return this;
  }

  private Observable<Wrapper<T>> syncDataAfterHavingCache(List<Wrapper<T>> caches) {
    return Observable.just(null).flatMap(o -> {
      Wrapper<T> firstCache = FirechatUtils.getFirstItem(caches);
      Wrapper<T> lastCache = FirechatUtils.getLastItem(caches);

      Observable<Wrapper<T>> syncObservable = null;
      if (firstCache == null || lastCache == null) {
        syncObservable = FirechatUtils.observeSingleValueEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            mStartAt.getValue(),
            mEndAt.getValue(),
            mLimitToLast
        ));
      } else if (mIsOnNext) {
        if ((mLimitToLast == Config.DEFAULT_LIMIT_TO_LAST || caches.size() < mLimitToLast)) {
          syncObservable = FirechatUtils.observeSingleValueEvent(mClass, FirechatUtils.getQuery(
              mRef.orderByPriority(),
              mStartAt.getValue(),
              firstCache.priority - 1,
              mLimitToLast - caches.size()
          ));
        } else {
          syncObservable = Observable.just(null);
        }
      } else {
        syncObservable = FirechatUtils.observeSingleValueEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            lastCache.priority + 1,
            mEndAt.getValue(),
            Config.DEFAULT_LIMIT_TO_LAST
        ));
      }

      return syncObservable
          .onErrorReturn(throwable -> null)
          .filter(wrapper -> wrapper != null)
          .toList()
          .flatMap(syncs -> Observable.just(syncs)
              .flatMapIterable(wrappers -> wrappers)
              .mergeWith(createListenerIfNecessary(caches, syncs)));
    });
  }
}
