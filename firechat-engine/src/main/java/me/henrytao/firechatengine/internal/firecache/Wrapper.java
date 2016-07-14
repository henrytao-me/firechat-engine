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

package me.henrytao.firechatengine.internal.firecache;

import com.google.firebase.database.DataSnapshot;

public class Wrapper {

  public static Wrapper create(DataSnapshot dataSnapshot, String previousKey, Type type) {
    return new Wrapper(dataSnapshot, previousKey, type);
  }

  public static Wrapper create(DataSnapshot dataSnapshot, Type type) {
    return create(dataSnapshot, null, type);
  }

  public static Wrapper create(DataSnapshot dataSnapshot) {
    return create(dataSnapshot, Type.ON_CHILD_ADDED);
  }

  public final DataSnapshot dataSnapshot;

  public final String previousKey;

  public final Type type;

  protected Wrapper(DataSnapshot dataSnapshot, String previousKey, Type type) {
    this.dataSnapshot = dataSnapshot;
    this.previousKey = previousKey;
    this.type = type;
  }

  public enum Type {
    FROM_CACHE,
    ON_CHILD_ADDED,
    ON_CHILD_CHANGED,
    ON_CHILD_MOVED,
    ON_CHILD_REMOVED
  }
}