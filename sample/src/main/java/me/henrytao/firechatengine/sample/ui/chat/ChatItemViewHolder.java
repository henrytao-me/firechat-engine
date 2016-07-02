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

import android.databinding.DataBindingUtil;
import android.view.ViewGroup;

import me.henrytao.firechatengine.sample.R;
import me.henrytao.firechatengine.sample.data.model.ChatMessage;
import me.henrytao.firechatengine.sample.databinding.ViewHolderChatItemBinding;
import me.henrytao.mvvmlifecycle.MVVMObserver;
import me.henrytao.mvvmlifecycle.recyclerview.RecyclerViewBindingViewHolder;

/**
 * Created by henrytao on 7/1/16.
 */
public class ChatItemViewHolder extends RecyclerViewBindingViewHolder<ChatMessage> {

  private final ViewHolderChatItemBinding mBinding;

  private ChatItemViewModel mViewModel;

  public ChatItemViewHolder(MVVMObserver observer, ViewGroup parent) {
    super(observer, parent, R.layout.view_holder_chat_item);
    mBinding = DataBindingUtil.bind(itemView);
    mBinding.setViewModel(mViewModel);
  }

  @Override
  public void bind(ChatMessage data) {
    mViewModel.bind(data);
  }

  @Override
  public void onInitializeViewModels() {
    mViewModel = new ChatItemViewModel();
    addViewModel(mViewModel);
  }
}
