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

package me.henrytao.firechatengine.utils.firechat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import me.henrytao.firechatengine.internal.exception.DatabaseErrorException;
import me.henrytao.firechatengine.internal.firecache.Config;
import me.henrytao.firechatengine.utils.firechat.Wrapper.Type;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import rx.Observable;
import rx.observers.SerializedSubscriber;
import rx.subscriptions.Subscriptions;

/**
 * Created by henrytao on 7/6/16.
 */
public class FirechatUtils {

  public static Double getPriority(DataSnapshot data) {
    try {
      return (Double) data.getPriority();
    } catch (Exception ignore) {
    }
    return 0d;
  }

  public static Query getQuery(Query query, double startAt, double endAt, int limitToLast) {
    if (startAt != Config.DEFAULT_START_AT) {
      query = query.startAt(startAt);
    }
    if (endAt != Config.DEFAULT_END_AT) {
      query = query.endAt(endAt);
    }
    if (limitToLast != Config.DEFAULT_LIMIT_TO_LAST) {
      query = query.limitToLast(limitToLast);
    }
    return query;
  }

  public static <T> Observable<Wrapper<T>> observeChildEvent(Class<T> tClass, Query query) {
    return Observable.create(s -> {
      SerializedSubscriber<Wrapper<T>> subscriber = new SerializedSubscriber<>(s);
      if (query == null) {
        SubscriptionUtils.onComplete(subscriber);
        return;
      }
      ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onCancelled(DatabaseError databaseError) {
          SubscriptionUtils.onError(subscriber, DatabaseErrorException.create(databaseError));
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
          SubscriptionUtils.onNext(subscriber, Wrapper.create(tClass, dataSnapshot, s, Type.ON_CHILD_ADDED));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
          SubscriptionUtils.onNext(subscriber, Wrapper.create(tClass, dataSnapshot, s, Type.ON_CHILD_CHANGED));
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
          SubscriptionUtils.onNext(subscriber, Wrapper.create(tClass, dataSnapshot, s, Type.ON_CHILD_MOVED));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
          SubscriptionUtils.onNext(subscriber, Wrapper.create(tClass, dataSnapshot, Type.ON_CHILD_REMOVED));
        }
      };
      query.addChildEventListener(listener);
      subscriber.add(Subscriptions.create(() -> query.removeEventListener(listener)));
    });
  }

  public static <T> Observable<Wrapper<T>> observeSingleValueEvent(Class<T> tClass, Query query) {
    return Observable.create(s -> {
      SerializedSubscriber<Wrapper<T>> subscriber = new SerializedSubscriber<>(s);
      if (query == null) {
        SubscriptionUtils.onComplete(subscriber);
        return;
      }
      ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onCancelled(DatabaseError databaseError) {
          SubscriptionUtils.onError(subscriber, DatabaseErrorException.create(databaseError));
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          SubscriptionUtils.onNextAndComplete(subscriber, Wrapper.create(tClass, dataSnapshot));
        }
      };
      query.addListenerForSingleValueEvent(listener);
      subscriber.add(Subscriptions.create(() -> query.removeEventListener(listener)));
    });
  }

  public static <T> Observable<Wrapper<T>> observeValueEvent(Class<T> tClass, Query query) {
    return Observable.create(s -> {
      SerializedSubscriber<Wrapper<T>> subscriber = new SerializedSubscriber<>(s);
      if (query == null) {
        SubscriptionUtils.onComplete(subscriber);
        return;
      }
      ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onCancelled(DatabaseError databaseError) {
          SubscriptionUtils.onError(subscriber, DatabaseErrorException.create(databaseError));
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          SubscriptionUtils.onNext(subscriber, Wrapper.create(tClass, dataSnapshot, Type.ON_CHILD_CHANGED));
        }
      };
      query.addValueEventListener(listener);
      subscriber.add(Subscriptions.create(() -> query.removeEventListener(listener)));
    });
  }
}
