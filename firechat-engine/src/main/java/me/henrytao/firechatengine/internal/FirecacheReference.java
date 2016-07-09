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

package me.henrytao.firechatengine.internal;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by henrytao on 7/9/16.
 */
public class FirecacheReference {

  protected FirecacheReference(DatabaseReference ref) {

  }

  public void reload() {

  }

  public void removeEventListener(ValueEventListener valueEventListener) {

  }

  public void removeEventListener(ChildEventListener childEventListener) {

  }

  public void removeEventListeners() {

  }

  public static class Builder {

    private final DatabaseReference mRef;

    public Builder(DatabaseReference ref) {
      mRef = ref;
    }

    public Builder addChildEventListener(ChildEventListener childEventListener) {
      return this;
    }

    public Builder addListenerForSingleValueEvent(ValueEventListener valueEventListener) {
      return this;
    }

    public Builder addValueEventListener(ValueEventListener valueEventListener) {
      return this;
    }

    public FirecacheReference build() {
      return new FirecacheReference(mRef);
    }
  }
}
