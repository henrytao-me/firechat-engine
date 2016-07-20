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

import me.henrytao.firechatengine.internal.firecache.BaseModel;

public class Wrapper<T> {

  public static <T> Wrapper<T> clone(Wrapper<T> wrapper) {
    return create(wrapper.mClass, wrapper.ref, wrapper.key, wrapper.data, wrapper.priority);
  }

  public static <T> Wrapper<T> create(Class<T> tClass, DataSnapshot dataSnapshot) {
    return create(tClass, dataSnapshot, Type.ON_CHILD_ADDED);
  }

  public static <T> Wrapper<T> create(Class<T> tClass, String ref, String key, T data, double priority) {
    return new Wrapper<T>(tClass, ref, key, data, priority);
  }

  public static <T> Wrapper<T> create(Class<T> tClass, DataSnapshot dataSnapshot, Type type) {
    return new Wrapper<T>(tClass, dataSnapshot, type);
  }

  public final T data;

  public final String key;

  public final Class<T> mClass;

  public final Double priority;

  public final String ref;

  public final Type type;

  protected Wrapper(Class<T> tClass, String ref, String key, T data, Type type, double priority) {
    mClass = tClass;
    this.ref = ref;
    this.key = key;
    this.data = data;
    this.type = type;
    this.priority = priority;
    if (data instanceof BaseModel) {
      ((BaseModel) data).setPriority(priority);
    }
  }

  protected Wrapper(Class<T> tClass, String ref, String key, T data, double priority) {
    this(tClass, ref, key, data, Type.FROM_CACHE, priority);
  }

  protected Wrapper(Class<T> tClass, DataSnapshot dataSnapshot, Type type) {
    this(tClass, dataSnapshot.getRef().toString(), dataSnapshot.getKey(), dataSnapshot.getValue(tClass), type,
        FirechatUtils.getPriority(dataSnapshot));
  }

  public enum Type {
    FROM_CACHE,
    ON_CHILD_ADDED,
    ON_CHILD_CHANGED,
    ON_CHILD_MOVED,
    ON_CHILD_REMOVED
  }
}