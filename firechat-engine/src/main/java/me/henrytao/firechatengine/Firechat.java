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

package me.henrytao.firechatengine;

import android.content.Context;

import me.henrytao.firechatengine.core.model.Message;
import me.henrytao.firechatengine.core.model.Room;
import me.henrytao.firechatengine.internal.firecache.Cache;
import rx.Observable;

/**
 * Created by henrytao on 7/2/16.
 */
public class Firechat implements me.henrytao.firechatengine.core.Firechat {

  public static Firechat getInstance() {
    Firechat firechat = new Firechat();
    return firechat;
  }

  public static void init(Context context) {
    Cache.init(context);
  }

  @Override
  public <TM extends Message> Observable<TM> createMessage(TM message, String roomKey) {
    return null;
  }

  @Override
  public <T extends Room> Observable<T> createRoom(T room) {
    return null;
  }

  @Override
  public <TM extends Message> Observable<TM> observeMessages(Class<TM> messageClass, String roomKey) {
    return null;
  }

  @Override
  public <TR extends Room> Observable<TR> observeRoom(Class<TR> roomClass, String roomKey) {
    return null;
  }

  @Override
  public <TR extends Room> Observable<TR> observeRooms(Class<TR> roomClass) {
    return null;
  }

  public static class ChatMessage implements Message {

  }

  public static class ChatRoom implements Room {

  }
}
