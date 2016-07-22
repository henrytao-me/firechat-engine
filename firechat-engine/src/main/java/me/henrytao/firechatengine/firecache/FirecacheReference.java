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

package me.henrytao.firechatengine.firecache;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import me.henrytao.firechatengine.config.Constants;
import me.henrytao.firechatengine.exception.NoDataFoundException;
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

  private BehaviorSubject<Double> mEndAt = BehaviorSubject.create(Constants.DEFAULT_END_AT);

  private Func1<Wrapper<T>, Boolean> mFilter = wrapper -> wrapper != null;

  private boolean mIsOnNext = false;

  private int mLimitToLast = Constants.DEFAULT_LIMIT_TO_LAST;

  private BehaviorSubject<Double> mNextEndAt = BehaviorSubject.create();

  private boolean mShouldKeepSyncing = true;

  private BehaviorSubject<Double> mStartAt = BehaviorSubject.create(Constants.DEFAULT_START_AT);

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

  private Observable<Wrapper<T>> createListenerIfNecessary(List<Wrapper<T>> syncs) {
    return Observable.just(FirechatUtils.getLastItem(syncs)).flatMap(lastSync -> {
      if (mShouldKeepSyncing) {
        return FirechatUtils.observeChildEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            lastSync != null ? lastSync.priority + 1 : mStartAt.getValue(),
            Constants.DEFAULT_END_AT,
            lastSync != null ? Constants.DEFAULT_LIMIT_TO_LAST : mLimitToLast));
      } else {
        return Observable.create(SubscriptionUtils::onComplete);
      }
    });
  }

  private Query getQuery() {
    Query query = mRef.orderByPriority();
    if (mStartAt.getValue() != Constants.DEFAULT_START_AT) {
      query = query.startAt(mStartAt.getValue());
    }
    if (mEndAt.getValue() != Constants.DEFAULT_END_AT) {
      query = query.endAt(mEndAt.getValue());
    }
    if (mLimitToLast != Constants.DEFAULT_LIMIT_TO_LAST) {
      query = query.limitToLast(mLimitToLast);
    }
    return query;
  }

  private Observable<Pair<Double, Double>> onReady() {
    return mStartAt.flatMap(startAt -> mEndAt.map(endAt -> new Pair<>(startAt, endAt))).first().flatMap(startEndAt -> {
      if (mIsOnNext && startEndAt.second == Constants.DEFAULT_END_AT) {
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

      Observable<List<Wrapper<T>>> syncObservable = null;
      if (mIsOnNext) {
        if (mLimitToLast != Constants.DEFAULT_LIMIT_TO_LAST && caches.size() < mLimitToLast) {
          syncObservable = FirechatUtils.observeSingleValueEvent(mClass, FirechatUtils.getQuery(
              mRef.orderByPriority(),
              mStartAt.getValue(),
              firstCache != null ? firstCache.priority - 1 : mEndAt.getValue(),
              mLimitToLast - caches.size()
          )).toList();
        } else {
          syncObservable = Observable.just(new ArrayList<>());
        }
      } else {
        syncObservable = FirechatUtils.observeSingleValueEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            lastCache != null ? lastCache.priority + 1 : mStartAt.getValue(),
            mEndAt.getValue(),
            lastCache != null ? Constants.DEFAULT_LIMIT_TO_LAST : mLimitToLast
        )).toList();
      }

      return syncObservable.flatMap(syncs -> {
        Observable<List<Wrapper<T>>> observable = Observable.just(syncs);
        List<Wrapper<T>> data = FirechatUtils.merge(caches, syncs);
        if (mIsOnNext) {
          if (data.size() == 0) {
            throw new NoDataFoundException();
          }
          mNextEndAt.onNext(FirechatUtils.getFirstItem(data).priority - 1);
          return observable.flatMapIterable(wrappers -> wrappers);
        } else {
          if (data.size() > 0) {
            mNextEndAt.onNext(FirechatUtils.getFirstItem(data).priority - 1);
          }
          return observable
              .flatMapIterable(wrappers -> wrappers)
              .mergeWith(createListenerIfNecessary(data).map(wrapper -> {
                if (!mIsOnNext && !mNextEndAt.hasValue()) {
                  mNextEndAt.onNext(wrapper.priority - 1);
                }
                return wrapper;
              }));
        }
      });
    });
  }
}
