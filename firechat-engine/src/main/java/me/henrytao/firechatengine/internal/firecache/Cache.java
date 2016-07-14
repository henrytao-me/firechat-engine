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

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import android.content.Context;

import java.util.List;

import me.henrytao.firechatengine.internal.exception.NoDataFoundException;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import rx.Observable;

/**
 * Created by henrytao on 7/9/16.
 */
public class Cache {

  private static Cache sInstance;

  public static Cache getInstance() {
    if (sInstance == null) {
      throw new IllegalStateException("Cache has not been initialized properly");
    }
    return sInstance;
  }

  public static void init(Context context) {
    context = context.getApplicationContext();
    if (sInstance == null) {
      synchronized (Cache.class) {
        if (sInstance == null) {
          sInstance = new Cache(context);
        }
      }
    }
  }

  private final Context mContext;

  public Cache(Context context) {
    mContext = context.getApplicationContext();
    FlowManager.init(new FlowConfig.Builder(mContext).build());
  }

  public <T> Observable<CacheSnapshot<T>> get(Class<T> tClass, DatabaseReference ref) {
    return Observable.create(subscriber -> {
      // TODO: check BaseModel
      SubscriptionUtils.onError(subscriber, new NoDataFoundException());
    });
  }

  public <T> Observable<List<CacheSnapshot<T>>> get(Class<T> tClass, DatabaseReference ref, double startAt, double endAt, int limitToLast) {
    return Observable.create(subscriber -> {
      // TODO: check BaseModel
      SubscriptionUtils.onError(subscriber, new NoDataFoundException());
    });
  }

  public <T> Observable<Void> set(Class<T> tClass, DatabaseReference ref, DataSnapshot dataSnapshot) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onNextAndComplete(subscriber);
    });
  }

  public <T> Observable<Void> set(Class<T> tClass, DatabaseReference ref, String key, DataSnapshot dataSnapshot) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onNextAndComplete(subscriber);
    });
  }

  public static class CacheSnapshot<T> {

    public final T data;

    public final double priority;

    public CacheSnapshot(T data, double priority) {
      this.priority = priority;
      this.data = data;
    }
  }

  //public <T> Observable<T> get(Class<T> tClass, DatabaseReference ref) {
  //  return Observable.create(subscriber -> {
  //    // TODO: check BaseModel
  //    SubscriptionUtils.onError(subscriber, new NoDataFoundException());
  //  });
  //}
  //
  //public <T> Observable<List<T>> get(Class<T> tClass, DatabaseReference ref, double startAt, double endAt, int limitToLast) {
  //  return Observable.create(subscriber -> {
  //    // TODO: check BaseModel
  //    SubscriptionUtils.onError(subscriber, new NoDataFoundException());
  //  });
  //}
  //
  //public <T> Observable<T> set(Class<T> tClass, DatabaseReference ref, DataSnapshot dataSnapshot) {
  //  return Observable.create(subscriber -> {
  //    T object = dataSnapshot.getValue(tClass);
  //    if (object instanceof BaseModel) {
  //      ((BaseModel) object).setPriority(FirechatUtils.getPriority(dataSnapshot));
  //    }
  //    SubscriptionUtils.onNextAndComplete(subscriber, object);
  //  });
  //}
  //
  //public <T> Observable<T> set(Class<T> tClass, DatabaseReference ref, String key, DataSnapshot dataSnapshot) {
  //  return Observable.create(subscriber -> {
  //    T object = dataSnapshot.getValue(tClass);
  //    if (object instanceof BaseModel) {
  //      ((BaseModel) object).setPriority(FirechatUtils.getPriority(dataSnapshot));
  //    }
  //    SubscriptionUtils.onNextAndComplete(subscriber, object);
  //  });
  //}
}
