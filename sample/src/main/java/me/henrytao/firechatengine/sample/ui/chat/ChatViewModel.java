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

package me.henrytao.firechatengine.sample.ui.chat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import android.databinding.ObservableField;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.henrytao.firechatengine.sample.data.model.ChatMessage;
import me.henrytao.firechatengine.sample.ui.base.BaseViewModel;
import me.henrytao.firechatengine.sample.util.Logger;
import me.henrytao.mvvmlifecycle.rx.UnsubscribeLifeCycle;
import rx.Observable;

/**
 * Created by henrytao on 7/1/16.
 */
public class ChatViewModel extends BaseViewModel<ChatViewModel.State> {

  private final List<ChatMessage> mData;

  private final Logger mLogger;

  private final DatabaseReference mMessagesRef;

  public ObservableField<String> message = new ObservableField<>();

  public ChatViewModel() {
    mData = new ArrayList<>();
    mLogger = Logger.newInstance(Logger.LogLevel.VERBOSE);
    mMessagesRef = FirebaseDatabase.getInstance().getReference().child("messages");

    DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
    offsetRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onCancelled(DatabaseError error) {
        System.err.println("Listener was cancelled");
      }

      @Override
      public void onDataChange(DataSnapshot snapshot) {
        double offset = snapshot.getValue(Double.class);
        double estimatedServerTimeMs = System.currentTimeMillis() + offset;
        mLogger.d("custom lock time: %f - %f - %d", offset, estimatedServerTimeMs, System.currentTimeMillis());
      }
    });

    manageSubscription(Observable.timer(500, TimeUnit.MILLISECONDS).subscribe(aLong -> {
      //mMessagesRef.orderByPriority().limitToLast(10).addValueEventListener(new ValueEventListener() {
      //  @Override
      //  public void onCancelled(DatabaseError databaseError) {
      //
      //  }
      //
      //  @Override
      //  public void onDataChange(DataSnapshot dataSnapshot) {
      //    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
      //      mData.add(snapshot.getValue(ChatMessage.class));
      //      mLogger.d("custom data: %s - %s", snapshot.getPriority(), mData.get(mData.size() - 1).getMessage());
      //    }
      //    setState(State.LOADED_MESSAGE);
      //  }
      //});
      mMessagesRef.orderByPriority().addChildEventListener(new ChildEventListener() {

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
          ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
          mData.add(message);
          mLogger.d("custom onChildAdded: %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s);
          setState(State.LOADED_MESSAGE);
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
    }), UnsubscribeLifeCycle.DESTROY);
  }

  public List<ChatMessage> getData() {
    return mData;
  }

  public void sendMessage() {
    String message = this.message.get();
    if (message != null && message.length() > 0) {
      ChatMessage chatMessage = new ChatMessage(message);
      DatabaseReference tmp = mMessagesRef.push();
      tmp.setValue(chatMessage, ServerValue.TIMESTAMP, (databaseError, databaseReference) -> {
        Log.d("custom setValue", String.format("%s - %s", databaseError, databaseReference));
      });
      this.message.set(null);
    }
  }

  public enum State {
    ADDED_MESSAGE,
    LOADED_MESSAGE
  }
}