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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.raizlabs.android.dbflow.converter.TypeConverter;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;

/**
 * Created by henrytao on 7/12/16.
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
public class DataSnapshotConverter extends TypeConverter<byte[], DataSnapshot> {

  private final Kryo mKryo;

  public DataSnapshotConverter() {
    mKryo = new Kryo();
    mKryo.register(DataSnapshot.class);
    mKryo.register(DatabaseReference.class);
    mKryo.register(Object.class);
    mKryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
  }

  @Override
  public byte[] getDBValue(DataSnapshot model) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Output output = new Output(stream);
    mKryo.writeObject(output, model);
    output.close();
    return stream.toByteArray();
  }

  @Override
  public DataSnapshot getModelValue(byte[] data) {
    Input input = new Input(data);
    DataSnapshot snapshot = mKryo.readObjectOrNull(input, DataSnapshot.class);
    input.close();
    return snapshot;
  }
}
