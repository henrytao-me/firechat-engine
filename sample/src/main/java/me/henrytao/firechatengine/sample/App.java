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

import android.app.Application;

import java.util.Date;

import me.henrytao.firechatengine.Firechat;
import me.henrytao.firechatengine.sample.util.Ln;

/**
 * Created by henrytao on 6/17/16.
 */
public class App extends Application {

  private static Magic sMagicInstance;

  public static Magic getMagicInstance() {
    if (sMagicInstance == null) {
      synchronized (Magic.class) {
        if (sMagicInstance == null) {
          sMagicInstance = new Magic();
        }
      }
    }
    return sMagicInstance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Firechat.init(this);

    MagicService.start(this);
    Ln.d("%s | %s | %d", getClass().getSimpleName(), "onCreate", App.getMagicInstance().key);
  }

  public static class Magic {

    public final long key;

    public Magic() {
      key = new Date().getTime();
    }
  }
}
