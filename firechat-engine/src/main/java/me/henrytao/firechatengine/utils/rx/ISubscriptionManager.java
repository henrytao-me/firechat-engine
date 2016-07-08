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

package me.henrytao.firechatengine.utils.rx;

import rx.Subscription;

/**
 * Created by henrytao on 11/13/15.
 */
public interface ISubscriptionManager {

  void manageSubscription(Subscription subscription);

  void manageSubscription(Subscription subscription, UnsubscribeLifeCycle unsubscribeLifeCycle);

  void manageSubscription(String id, Subscription subscription);

  void manageSubscription(String id, Subscription subscription, UnsubscribeLifeCycle unsubscribeLifeCycle);

  void unsubscribe();

  void unsubscribe(UnsubscribeLifeCycle unsubscribeLifeCycle);

  void unsubscribe(String id);
}
