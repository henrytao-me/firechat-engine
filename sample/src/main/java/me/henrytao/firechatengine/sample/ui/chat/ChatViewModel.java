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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import android.databinding.ObservableField;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.henrytao.firechatengine.firecache.FirecacheReference;
import me.henrytao.firechatengine.sample.data.model.ChatMessage;
import me.henrytao.firechatengine.sample.ui.base.BaseViewModel;
import me.henrytao.firechatengine.utils.Logger;
import me.henrytao.mvvmlifecycle.rx.UnsubscribeLifeCycle;

/**
 * Created by henrytao on 7/1/16.
 */
public class ChatViewModel extends BaseViewModel<ChatViewModel.State> {

  private final List<ChatMessage> mData;

  private final Logger mLogger;

  private final DatabaseReference mMessagesRef;

  private final FirecacheReference.Builder<ChatMessage> mRefBuilder;

  public ObservableField<String> message = new ObservableField<>();

  private FirecacheReference<ChatMessage> mRef;

  public ChatViewModel() {
    mData = new ArrayList<>();
    mLogger = Logger.newInstance(Logger.LogLevel.VERBOSE);

    mMessagesRef = FirebaseDatabase.getInstance().getReference().child("messages");

    mRefBuilder = new FirecacheReference.Builder<>(ChatMessage.class, mMessagesRef)
        .filter(wrapper -> {
          mLogger.d("custom filter: %s | %s | %s", wrapper.type, wrapper.key, wrapper.data);
          //return wrapper.type == Wrapper.Type.ON_CHILD_ADDED || wrapper.type == Wrapper.Type.FROM_CACHE;
          return true;
        })
        .limitToLast(5);
  }

  @Override
  public void onCreateView() {
    super.onCreateView();

    //manageSubscription(mRefBuilder.build().addChildEventListener().subscribe(chatMessage -> {
    //}, Throwable::printStackTrace, () -> {
    //}), UnsubscribeLifeCycle.DESTROY_VIEW);

    mRef = mRefBuilder.build();
    manageSubscription(mRef.addChildEventListener().subscribe(this::addData, Throwable::printStackTrace, () -> {
      mLogger.d("done");
    }), UnsubscribeLifeCycle.DESTROY_VIEW);
  }

  public List<ChatMessage> getData() {
    return mData;
  }

  public void next() {
    mRef = mRef.next();
    manageSubscription(mRef.addChildEventListener().subscribe(this::addData, Throwable::printStackTrace, () -> {
      mLogger.d("done next");
    }), UnsubscribeLifeCycle.DESTROY_VIEW);
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

  private void addData(ChatMessage chatMessage) {
    mData.add(chatMessage);
    setState(State.ADDED_MESSAGE);
  }

  public enum State {
    ADDED_MESSAGE,
    LOADED_MESSAGE
  }
}
