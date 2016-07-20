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

import com.facebook.stetho.Stetho;

import android.annotation.SuppressLint;
import android.app.Application;

import me.henrytao.firechatengine.Firechat;

/**
 * Created by henrytao on 6/17/16.
 */
public class App extends Application {

  @SuppressLint("CommitPrefEdits")
  @Override
  public void onCreate() {
    super.onCreate();
    Stetho.initializeWithDefaults(this);
    Firechat.init(this);

    //SharedPreferences sharedPreferences = getSharedPreferences("test", MODE_PRIVATE);
    //if (!sharedPreferences.getBoolean("init", false)) {
    //
    //  sharedPreferences.edit().putBoolean("init", true).commit();
    //}

    //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    //startService(new Intent(this, FirechatBackgroundService.class));
  }
}
