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
    Cache cache = sInstance;
    if (cache == null) {
      synchronized (Cache.class) {
        cache = sInstance;
        if (cache == null) {
          cache = sInstance = new Cache();
        }
      }
    }
    return cache;
  }

  public Observable<DataSnapshot> get(DatabaseReference reference) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onError(subscriber, new NoDataFoundException());
    });
  }

  public Observable<List<DataSnapshot>> get(DatabaseReference reference, double startAt, double endAt, int limitToLast) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onError(subscriber, new NoDataFoundException());
    });
  }

  public Observable<DataSnapshot> set(DatabaseReference reference, DataSnapshot dataSnapshot) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onNextAndComplete(subscriber, dataSnapshot);
    });
  }

  public Observable<DataSnapshot> set(DatabaseReference reference, String key, DataSnapshot dataSnapshot) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onNextAndComplete(subscriber, dataSnapshot);
    });
  }
}
