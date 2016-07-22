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

package me.henrytao.firechatengine.sample.service;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.henrytao.firechatengine.sample.data.model.ChatMessage;

/**
 * Created by henrytao on 7/2/16.
 */
public class FirechatBackgroundService extends Service {

  private final List<ChatMessage> mData;

  private final Logger mLogger;

  private final DatabaseReference mMessagesRef;

  public FirechatBackgroundService() {
    mData = new ArrayList<>();
    mLogger = new Logger("firechat-service", Logger.LogLevel.VERBOSE);
    mMessagesRef = FirebaseDatabase.getInstance().getReference().child("messages");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mMessagesRef.orderByPriority().addChildEventListener(new ChildEventListener() {

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }

      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
        mData.add(message);
        mLogger.d("custom onChildAdded: %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s);
      }

      @Override
      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
        mLogger.d("custom onChildChanged: %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s);
      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
        mLogger.d("custom onChildMoved: %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s);
      }

      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {
        ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
        mLogger.d("custom onChildMoved: %s - %s", dataSnapshot.getPriority(), message.getMessage());
      }
    });
  }
}
