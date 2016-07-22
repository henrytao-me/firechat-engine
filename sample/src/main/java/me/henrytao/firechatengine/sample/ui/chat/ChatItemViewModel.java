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

import android.databinding.ObservableField;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.henrytao.firechatengine.sample.data.model.ChatMessage;
import me.henrytao.firechatengine.sample.ui.base.BaseViewModel;

/**
 * Created by henrytao on 7/1/16.
 */
public class ChatItemViewModel extends BaseViewModel {

  public ObservableField<String> message = new ObservableField<>();

  public ChatItemViewModel() {

  }

  public void bind(ChatMessage data) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    message.set(String.format(Locale.US, "%s - %s - %s", data.getMessage(), dateFormat.format(data.getPriority()), data.isSent()));
  }
}
