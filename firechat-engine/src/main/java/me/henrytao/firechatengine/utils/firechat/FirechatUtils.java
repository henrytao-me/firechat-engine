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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import me.henrytao.firechatengine.internal.firecache.Config;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import rx.Observable;

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

  //public static Observable<DataSnapshot> observeChildEvent(Query query) {
  //  return Observable.create(subscriber -> {
  //    if (query == null) {
  //      SubscriptionUtils.onComplete(subscriber);
  //      return;
  //    }
  //    query.addChildEventListener(new ChildEventListener() {
  //      @Override
  //      public void onCancelled(DatabaseError databaseError) {
  //        Log.d("custom onCancelled", String.format("%s", databaseError));
  //        SubscriptionUtils.onError(subscriber, databaseError.toException());
  //      }
  //
  //      @Override
  //      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
  //        Log.d("custom onChildAdded", String.format("%s - %s", dataSnapshot, s));
  //        SubscriptionUtils.onNext(subscriber, dataSnapshot);
  //      }
  //
  //      @Override
  //      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
  //        Log.d("custom onChildChanged", String.format("%s - %s", dataSnapshot, s));
  //        SubscriptionUtils.onNext(subscriber, dataSnapshot);
  //      }
  //
  //      @Override
  //      public void onChildMoved(DataSnapshot dataSnapshot, String s) {
  //        Log.d("custom onChildMoved", String.format("%s - %s", dataSnapshot, s));
  //        SubscriptionUtils.onNext(subscriber, dataSnapshot);
  //      }
  //
  //      @Override
  //      public void onChildRemoved(DataSnapshot dataSnapshot) {
  //        // do nothing
  //      }
  //    });
  //  });
  //}


  public static Observable<DataSnapshot> getSingleValueEvent(Query query) {
    return Observable.create(subscriber -> {
      if (query == null) {
        SubscriptionUtils.onComplete(subscriber);
        return;
      }
      query.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onCancelled(DatabaseError databaseError) {
          SubscriptionUtils.onError(subscriber, databaseError.toException());
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          SubscriptionUtils.onNextAndComplete(subscriber, dataSnapshot);
        }
      });
    });
  }
}
