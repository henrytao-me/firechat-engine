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

package me.henrytao.firechatengine.internal.firecache.db;

import me.henrytao.firechatengine.utils.firechat.Wrapper;

/**
 * Created by henrytao on 7/12/16.
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
public class TypeConverter extends com.raizlabs.android.dbflow.converter.TypeConverter<Integer, Wrapper.Type> {

  @Override
  public Integer getDBValue(Wrapper.Type model) {
    return model.toInt();
  }

  @Override
  public Wrapper.Type getModelValue(Integer data) {
    return Wrapper.Type.fromInt(data);
  }
}
