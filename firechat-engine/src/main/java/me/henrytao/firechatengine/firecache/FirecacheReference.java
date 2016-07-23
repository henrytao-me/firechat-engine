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

  private final Cache mCache;

  private final Class<T> mClass;

  private final Func1<Wrapper<T>, Boolean> mFilter;

  private final boolean mIsOnNext;

  private final boolean mKeepSyncing;

  private final int mLimitToLast;

  private final DatabaseReference mRef;

  private BehaviorSubject<Double> mEndAt = BehaviorSubject.create(Constants.DEFAULT_END_AT);

  private BehaviorSubject<Double> mNextEndAt = BehaviorSubject.create();

  private BehaviorSubject<Double> mStartAt = BehaviorSubject.create(Constants.DEFAULT_START_AT);

  protected FirecacheReference(Class<T> tClass, DatabaseReference ref, double startAt, double endAt,
      int limitToLast, Func1<Wrapper<T>, Boolean> filter, boolean keepSyncing, boolean isOnNext) {
    mClass = tClass;
    mRef = ref;
    mLimitToLast = limitToLast;
    mFilter = filter;
    mKeepSyncing = keepSyncing;
    mIsOnNext = isOnNext;

    mStartAt.onNext(startAt);
    mEndAt.onNext(endAt);

    mCache = Cache.getInstance();
  }

  protected FirecacheReference(Class<T> tClass, DatabaseReference ref, BehaviorSubject<Double> startAt, BehaviorSubject<Double> endAt,
      int limitToLast, Func1<Wrapper<T>, Boolean> filter, boolean keepSyncing, boolean isOnNext) {
    mClass = tClass;
    mRef = ref;
    mLimitToLast = limitToLast;
    mFilter = filter;
    mKeepSyncing = keepSyncing;
    mIsOnNext = isOnNext;

    mStartAt = startAt;
    mEndAt = endAt;

    mCache = Cache.getInstance();
  }

  public Observable<T> addChildEventListener() {
    Observable<T> observable = onReady()
        .flatMap(startEndAt -> mCache
            .getList(mClass, mRef.toString(), startEndAt.first, startEndAt.second, mLimitToLast)
            .onErrorReturn(throwable -> new ArrayList<>()))
        .flatMap(caches -> Observable
            .just(caches)
            .flatMapIterable(wrappers -> wrappers)
            .mergeWith(syncDataAfterHavingCache(caches))
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

  public FirecacheReference<T> next() {
    return next(mLimitToLast);
  }

  public FirecacheReference<T> next(int limitToLast) {
    return new FirecacheReference<T>(mClass, mRef, mStartAt, mNextEndAt, limitToLast, mFilter, false, true);
  }

  private Observable<Wrapper<T>> createListenerIfNecessary(List<Wrapper<T>> syncs) {
    return Observable.just(FirechatUtils.getLastItem(syncs)).flatMap(lastSync -> {
      if (mKeepSyncing) {
        return FirechatUtils.observeChildEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            lastSync != null ? lastSync.priority + 1 : mStartAt.getValue(),
            Constants.DEFAULT_END_AT,
            lastSync != null ? Constants.DEFAULT_LIMIT_TO_LAST : mLimitToLast))
            .flatMap(wrapper -> mCache
                .set(wrapper.type == Wrapper.Type.ON_CHILD_ADDED ? Wrapper.clone(wrapper, false) : wrapper)
                .map(aVoid -> wrapper));
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

  private void onNext(Wrapper<T> wrapper) {
    if (!mNextEndAt.hasValue() && wrapper != null) {
      mNextEndAt.onNext(wrapper.priority - 1);
    }
  }

  private Observable<Pair<Double, Double>> onReady() {
    return mStartAt.flatMap(startAt -> mEndAt.map(endAt -> new Pair<>(startAt, endAt))).first();
  }

  private Observable<Wrapper<T>> syncDataAfterHavingCache(List<Wrapper<T>> caches) {
    return Observable.just(null).flatMap(o -> {
      Wrapper<T> firstCache = FirechatUtils.getFirstItem(caches);
      Wrapper<T> lastCache = FirechatUtils.getLastItem(caches);

      onNext(firstCache);

      Observable<List<Wrapper<T>>> syncObservable = null;
      if (mIsOnNext && caches.size() == 0) {
        syncObservable = FirechatUtils.observeSingleValueEvent(mClass, FirechatUtils.getQuery(
            mRef.orderByPriority(),
            mStartAt.getValue(),
            firstCache != null ? firstCache.priority - 1 : mEndAt.getValue(),
            mLimitToLast - caches.size()
        )).toList();
      } else {
        syncObservable = Observable.just(new ArrayList<>());
      }

      return syncObservable.flatMap(syncs -> {
        Observable<List<Wrapper<T>>> observable = Observable.just(syncs);
        List<Wrapper<T>> data = FirechatUtils.merge(caches, syncs);
        return observable
            .flatMapIterable(wrappers -> wrappers)
            .flatMap(wrapper -> mCache.set(wrapper).map(aVoid -> wrapper))
            .mergeWith(createListenerIfNecessary(data))
            .map(wrapper -> {
              onNext(wrapper);
              return wrapper;
            });
      });
    });
  }

  public static class Builder<T> {

    private final Class<T> mClass;

    private final DatabaseReference mRef;

    private double mEndAt = Constants.DEFAULT_END_AT;

    private Func1<Wrapper<T>, Boolean> mFilter = wrapper -> wrapper != null;

    private boolean mKeepSyncing = true;

    private int mLimitToLast = Constants.DEFAULT_LIMIT_TO_LAST;

    private double mStartAt = Constants.DEFAULT_START_AT;

    public Builder(Class<T> tClass, DatabaseReference ref) {
      mClass = tClass;
      mRef = ref;
    }

    public FirecacheReference<T> build() {
      return new FirecacheReference<>(mClass, mRef, mStartAt, mEndAt, mLimitToLast, mFilter, mKeepSyncing, false);
    }

    public Builder<T> endAt(double endAt) {
      mEndAt = endAt;
      return this;
    }

    public Builder<T> filter(Func1<Wrapper<T>, Boolean> filter) {
      mFilter = filter;
      return this;
    }

    public Builder<T> keepSyncing(boolean keepSyncing) {
      mKeepSyncing = keepSyncing;
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
