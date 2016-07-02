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

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.Map;

import me.henrytao.firechatengine.sample.R;
import me.henrytao.firechatengine.sample.databinding.ActivityChatBinding;
import me.henrytao.firechatengine.sample.ui.base.BaseActivity;
import me.henrytao.mvvmlifecycle.recyclerview.RecyclerViewBindingAdapter;
import me.henrytao.mvvmlifecycle.rx.UnsubscribeLifeCycle;

/**
 * Created by henrytao on 7/1/16.
 */
public class ChatActivity extends BaseActivity {

  public static Intent newIntent(Activity activity) {
    return new Intent(activity, ChatActivity.class);
  }

  private RecyclerView.Adapter mAdapter;

  private ActivityChatBinding mBinding;

  private ChatViewModel mViewModel;

  @Override
  public void onInitializeViewModels() {
    mViewModel = new ChatViewModel();
    addViewModel(mViewModel);
  }

  @Override
  public void onSetContentView(Bundle savedInstanceState) {
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
    mBinding.setViewModel(mViewModel);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setSupportActionBar(mBinding.toolbar);

    mAdapter = new RecyclerViewBindingAdapter<>(ChatItemViewHolder.class, this, mViewModel.getData());
    mBinding.list.setAdapter(mAdapter);
    mBinding.list.setLayoutManager(new LinearLayoutManager(this));

    manageSubscription(mViewModel.getState().subscribe(state -> onStateChanged(state.getName(), state.getData())),
        UnsubscribeLifeCycle.DESTROY);
  }

  private void onStateChanged(ChatViewModel.State name, Map<String, Object> data) {
    switch (name) {
      case ADDED_MESSAGE:
        break;
      case LOADED_MESSAGE:
        mAdapter.notifyDataSetChanged();
        break;
    }
  }
}
