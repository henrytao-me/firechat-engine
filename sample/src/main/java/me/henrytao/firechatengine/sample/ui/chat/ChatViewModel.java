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

import me.henrytao.firechatengine.internal.firecache.FirecacheReference;
import me.henrytao.firechatengine.sample.data.model.ChatMessage;
import me.henrytao.firechatengine.sample.ui.base.BaseViewModel;
import me.henrytao.firechatengine.sample.util.Logger;
import me.henrytao.firechatengine.utils.firechat.Wrapper;
import me.henrytao.firechatengine.utils.rx.Transformer;
import me.henrytao.mvvmlifecycle.rx.UnsubscribeLifeCycle;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

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

    //DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
    //offsetRef.addValueEventListener(new ValueEventListener() {
    //  @Override
    //  public void onCancelled(DatabaseError error) {
    //    System.err.println("Listener was cancelled");
    //  }
    //
    //  @Override
    //  public void onDataChange(DataSnapshot snapshot) {
    //    double offset = snapshot.getValue(Double.class);
    //    double estimatedServerTimeMs = System.currentTimeMillis() + offset;
    //    mLogger.d("custom lock time: %f - %f - %d", offset, estimatedServerTimeMs, System.currentTimeMillis());
    //  }
    //});

    manageSubscription(Observable.timer(100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {

      //FirechatReference<ChatMessage> ref = new FirechatReference.Builder<>(ChatMessage.class, "messages")
      //    .limitToLast(5)
      //    .build();
      //ref.observe().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(chatMessage -> {
      //  addData(chatMessage);
      //}, Throwable::printStackTrace);

      //ref.next(3).observe().subscribe(chatMessage -> {
      //  addData(chatMessage);
      //});

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

      FirecacheReference<ChatMessage> ref = FirecacheReference.create(ChatMessage.class, mMessagesRef)
          .filter(wrapper -> {
            mLogger.d("custom filter: %s | %s | %s", wrapper.type, wrapper.key, wrapper.data);
            return wrapper.type == Wrapper.Type.ON_CHILD_ADDED || wrapper.type == Wrapper.Type.FROM_CACHE;
          })
          .limitToLast(5);
      ref.addChildEventListener().compose(Transformer.applyNewThreadScheduler()).subscribe(chatMessage -> {
        mLogger.d("custom onChildAdded: %s", chatMessage.getMessage());
        addData(chatMessage);
      });

      //FirecacheReference ref = new FirecacheReference.Builder(mMessagesRef)
      //    .limitToLast(5)
      //    .addChildEventListener(new ChildEventListener() {
      //      @Override
      //      public void onCancelled(DatabaseError databaseError) {
      //        mLogger.d("custom onCancelled: %s", databaseError);
      //      }
      //
      //      @Override
      //      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
      //        mLogger.d("custom onChildAdded: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //        DataSnapshotConverter converter = new DataSnapshotConverter();
      //        byte[] bytes = converter.getDBValue(dataSnapshot);
      //        DataSnapshot result = converter.getModelValue(bytes);
      //        addData(dataSnapshot.getValue(ChatMessage.class));
      //      }
      //
      //      @Override
      //      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
      //        mLogger.d("custom onChildChanged: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //        addData(dataSnapshot.getValue(ChatMessage.class));
      //      }
      //
      //      @Override
      //      public void onChildMoved(DataSnapshot dataSnapshot, String s) {
      //        mLogger.d("custom onChildMoved: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //        addData(dataSnapshot.getValue(ChatMessage.class));
      //      }
      //
      //      @Override
      //      public void onChildRemoved(DataSnapshot dataSnapshot) {
      //        mLogger.d("custom onChildRemoved: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //      }
      //    })
      //    .build();

      //mMessagesRef.orderByPriority().startAt(1.468063342215E12, "-KMEO5Ql63YzYJePsoVZ").limitToFirst(1).addChildEventListener(new ChildEventListener() {
      //  @Override
      //  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
      //    mLogger.d("custom onChildAdded special: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //  }
      //
      //  @Override
      //  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
      //    mLogger.d("custom onChildChanged special: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //  }
      //
      //  @Override
      //  public void onChildRemoved(DataSnapshot dataSnapshot) {
      //
      //  }
      //
      //  @Override
      //  public void onChildMoved(DataSnapshot dataSnapshot, String s) {
      //    mLogger.d("custom onChildMoved special: %s | %d", dataSnapshot, dataSnapshot.getChildrenCount());
      //  }
      //
      //  @Override
      //  public void onCancelled(DatabaseError databaseError) {
      //
      //  }
      //});

      //mMessagesRef.orderByPriority().limitToLast(5).addChildEventListener(new ChildEventListener() {
      //
      //  @Override
      //  public void onCancelled(DatabaseError databaseError) {
      //    mLogger.d("custom onCancelled: %s", databaseError.toString());
      //  }
      //
      //  @Override
      //  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
      //    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
      //    mData.add(message);
      //    mLogger.d("custom onChildAdded: %s - %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s, dataSnapshot.getRef());
      //    setState(State.ADDED_MESSAGE);
      //  }
      //
      //  @Override
      //  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
      //    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
      //    mLogger.d("custom onChildChanged: %s - %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s, dataSnapshot.getRef());
      //  }
      //
      //  @Override
      //  public void onChildMoved(DataSnapshot dataSnapshot, String s) {
      //    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
      //    mLogger.d("custom onChildMoved: %s - %s - %s - %s", dataSnapshot.getPriority(), message.getMessage(), s, dataSnapshot.getRef());
      //  }
      //
      //  @Override
      //  public void onChildRemoved(DataSnapshot dataSnapshot) {
      //    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
      //    mLogger.d("custom onChildMoved: %s - %s", dataSnapshot.getPriority(), message.getMessage());
      //  }
      //});
    }), UnsubscribeLifeCycle.DESTROY);
  }

  public List<ChatMessage> getData() {
    return mData;
  }

  public void sendMessage() {
    //mMessagesRef.child("-KMEO5Ql63YzYJePsoVZ").setPriority(ServerValue.TIMESTAMP);
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
