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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.henrytao.firechatengine.sample.util.Ln;

/**
 * Created by henrytao on 7/18/16.
 */
public class MagicReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Ln.d("%s | %s", getClass().getSimpleName(), "onReceive");
    MagicService.start(context);
  }
}
