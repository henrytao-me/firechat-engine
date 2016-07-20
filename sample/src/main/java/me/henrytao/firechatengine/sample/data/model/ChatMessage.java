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

package me.henrytao.firechatengine.sample.data.model;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.henrytao.firechatengine.internal.firecache.BaseModel;

/**
 * Created by henrytao on 7/1/16.
 */
public class ChatMessage implements BaseModel {

  private String mMessage;

  private double mPriority;

  public ChatMessage() {
  }

  public ChatMessage(String message) {
    mMessage = message;
  }

  @Override
  public void setPriority(double priority) {
    mPriority = priority;
  }

  public String getMessage() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return String.format(Locale.US, "%s - %s", mMessage, dateFormat.format(mPriority));
  }

  public void setMessage(String message) {
    mMessage = message;
  }
}
