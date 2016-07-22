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

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.henrytao.firechatengine.config.Constants;
import me.henrytao.firechatengine.exception.NoDataFoundException;
import me.henrytao.firechatengine.firecache.db.Model;
import me.henrytao.firechatengine.firecache.db.Model_Table;
import me.henrytao.firechatengine.utils.firechat.Wrapper;
import me.henrytao.firechatengine.utils.rx.SubscriptionUtils;
import rx.Observable;

/**
 * Created by henrytao on 7/9/16.
 */
public class Cache {

  private static Cache sInstance;

  public static Cache getInstance() {
    if (sInstance == null) {
      throw new IllegalStateException("Model has not been initialized properly");
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

  public <T> Observable<Wrapper<T>> get(Class<T> tClass, String ref) {
    return Observable.create(subscriber -> {
      SubscriptionUtils.onError(subscriber, new NoDataFoundException());
    });
  }

  public <T> Observable<List<Wrapper<T>>> getList(Class<T> tClass, String ref, double startAt, double endAt, int limitToLast) {
    return Observable.create(subscriber -> {
      Where<Model> query = SQLite
          .select(
              Model_Table.ref,
              Model_Table.key,
              Model_Table.data,
              Model_Table.priority)
          .from(Model.class)
          .where(Model_Table.ref.like(String.format(Locale.US, "%s/%%", ref)));

      if (startAt != Constants.DEFAULT_END_AT) {
        query = query.and(Model_Table.priority.greaterThanOrEq(startAt));
      }
      if (endAt != Constants.DEFAULT_END_AT) {
        query = query.and(Model_Table.priority.lessThanOrEq(endAt));
      }
      query = query.orderBy(Model_Table.priority, false);
      if (limitToLast != Constants.DEFAULT_LIMIT_TO_LAST) {
        query = query.limit(limitToLast);
      }

      List<Model> caches = query.queryList();
      List<Wrapper<T>> wrappers = new ArrayList<>();
      int i = caches.size();
      while (--i >= 0) {
        try {
          Model model = caches.get(i);
          wrappers.add(Wrapper.create(tClass, model.getRef(), model.getKey(), model.getValue(tClass), model.getPriority()));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (wrappers.size() == 0) {
        SubscriptionUtils.onError(subscriber, new NoDataFoundException());
      } else {
        SubscriptionUtils.onNextAndComplete(subscriber, wrappers);
      }
    });
  }

  public <T> Observable<Void> set(Wrapper<T> wrapper) {
    return Observable.create(subscriber -> {
      List<Model> caches = SQLite
          .select()
          .from(Model.class)
          .where(Model_Table.ref.eq(wrapper.ref))
          .orderBy(Model_Table.priority, false)
          .limit(1)
          .queryList();
      Model cache = caches.size() > 0 ? caches.get(0) : null;

      if (cache != null) {
        if (wrapper.priority > cache.getPriority()) {
          Model model = new Model(caches.get(0).getId(), wrapper);
          model.update();
        } else {
          // do nothing
        }
      } else {
        Model model = new Model(wrapper);
        model.save();
      }
      SubscriptionUtils.onNextAndComplete(subscriber);
    });
  }
}
