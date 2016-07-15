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

public class Wrapper<T> {

  public static <T> Wrapper<T> create(Class<T> tClass, DataSnapshot dataSnapshot, String previousKey, Type type) {
    return new Wrapper<T>(tClass, dataSnapshot, previousKey, type);
  }

  public static <T> Wrapper<T> create(Class<T> tClass, DataSnapshot dataSnapshot, Type type) {
    return create(tClass, dataSnapshot, null, type);
  }

  public static <T> Wrapper<T> create(Class<T> tClass, DataSnapshot dataSnapshot) {
    return create(tClass, dataSnapshot, Type.ON_CHILD_ADDED);
  }

  public static <T> Wrapper<T> create(Class<T> tClass, T data, double priority, String previousKey) {
    return new Wrapper<T>(tClass, data, priority, previousKey);
  }

  public final T data;

  public final String previousKey;

  public final Double priority;

  public final Type type;

  private final Class<T> mClass;

  protected Wrapper(Class<T> tClass, DataSnapshot dataSnapshot, String previousKey, Type type) {
    mClass = tClass;
    this.data = dataSnapshot.getValue(tClass);
    this.previousKey = previousKey;
    this.type = type;
    this.priority = FirechatUtils.getPriority(dataSnapshot);
  }

  protected Wrapper(Class<T> tClass, T data, double priority, String previousKey) {
    mClass = tClass;
    this.data = data;
    this.previousKey = previousKey;
    this.type = Type.FROM_CACHE;
    this.priority = priority;
  }

  public enum Type {
    FROM_CACHE,
    ON_CHILD_ADDED,
    ON_CHILD_CHANGED,
    ON_CHILD_MOVED,
    ON_CHILD_REMOVED
  }
}