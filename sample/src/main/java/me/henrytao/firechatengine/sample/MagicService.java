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

package me.henrytao.firechatengine.sample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import me.henrytao.firechatengine.sample.util.Ln;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by henrytao on 7/18/16.
 */
public class MagicService extends Service {

  public static void start(Context context) {
    Intent intent = new Intent(context, MagicService.class);
    context.startService(intent);
  }

  private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Ln.d("%s | %s", getClass().getSimpleName(), "onCreate");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Ln.d("%s | %s", getClass().getSimpleName(), "onDestroy");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Ln.d("%s | %s | %s", getClass().getSimpleName(), "onStartCommand", intent != null ? intent.getAction() : null);
    if (!mCompositeSubscription.hasSubscriptions()) {
      mCompositeSubscription.add(Observable
          .interval(2000, TimeUnit.MILLISECONDS)
          .subscribe(aLong -> {
            Ln.d("%s | %s | %d", getClass().getSimpleName(), "onStartCommand", App.getMagicInstance().key);
          }));
    }
    return START_STICKY;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
    mCompositeSubscription.clear();

    Ln.d("%s | %s", getClass().getSimpleName(), "onTaskRemoved");

    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(this, MagicService.class);
    intent.setAction("From alarm");
    PendingIntent sender = PendingIntent.getService(this, 0, intent, 0);
    alarmManager.set(AlarmManager.RTC_WAKEUP, 10000, sender);
  }
}
