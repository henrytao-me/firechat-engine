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

package me.henrytao.firechatengine.core;

import me.henrytao.firechatengine.core.model.Message;
import me.henrytao.firechatengine.core.model.Room;
import rx.Observable;

/**
 * Created by henrytao on 7/2/16.
 */
public interface Firechat {

  <TM extends Message> Observable<TM> createMessage(TM message, String roomKey);

  <TR extends Room> Observable<TR> createRoom(TR room);

  <TM extends Message> Observable<TM> observeMessages(Class<TM> messageClass, String roomKey);

  <TR extends Room> Observable<TR> observeRoom(Class<TR> roomClass, String roomKey);

  <TR extends Room> Observable<TR> observeRooms(Class<TR> roomClass);
}
