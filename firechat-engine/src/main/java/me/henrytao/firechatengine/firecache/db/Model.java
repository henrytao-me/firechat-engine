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

package me.henrytao.firechatengine.firecache.db;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;

import me.henrytao.firechatengine.utils.firechat.Wrapper;

/**
 * Created by henrytao on 7/12/16.
 */
@Table(database = Database.class, name = Model.NAME, indexGroups = {
    @IndexGroup(number = 1, name = "primary")
})
public class Model extends BaseModel {

  public static final String NAME = "cache";

  private static Kryo sKryo;

  private static Kryo getKryoInstance() {
    if (sKryo == null) {
      synchronized (Model.class) {
        Kryo kryo = sKryo;
        if (kryo == null) {
          kryo = new Kryo();
          kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
          sKryo = kryo;
        }
      }
    }
    return sKryo;
  }

  private static <T> Blob toBlob(Wrapper<T> wrapper) {
    Kryo kryo = getKryoInstance();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Output output = new Output(stream);
    kryo.writeObject(output, wrapper.data);
    output.close();
    return new Blob(stream.toByteArray());
  }

  @Column
  Blob data;

  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String key;

  @Index(indexGroups = 1)
  @Column
  double priority;

  @Column
  String ref;

  public Model() {
  }

  protected Model(String ref, String key, double priority, Blob data) {
    this.ref = ref;
    this.key = key;
    this.priority = priority;
    this.data = data;
  }

  public <T> Model(Wrapper<T> wrapper) {
    this(wrapper.ref, wrapper.key, wrapper.priority, toBlob(wrapper));
  }

  public <T> Model(long id, Wrapper<T> wrapper) {
    this(wrapper.ref, wrapper.key, wrapper.priority, toBlob(wrapper));
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public double getPriority() {
    return priority;
  }

  public String getRef() {
    return ref;
  }

  public <T> T getValue(Class<T> tClass) throws Exception {
    Kryo kryo = getKryoInstance();
    Input input = new Input(data.getBlob());
    T value = kryo.readObject(input, tClass);
    input.close();
    return value;
  }
}
