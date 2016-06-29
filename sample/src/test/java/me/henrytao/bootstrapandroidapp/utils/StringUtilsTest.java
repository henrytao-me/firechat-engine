/*
 * Copyright 2015 "Henry Tao <hi@henrytao.me>"
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

package me.henrytao.bootstrapandroidapp.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import me.henrytao.bootstrapandroidapp.BuildConfig;
import me.henrytao.bootstrapandroidapp.RobolectricGradleTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by henrytao on 11/1/15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = RobolectricGradleTestRunner.SDK)
public class StringUtilsTest {

  @Test
  public void test() throws Exception {
    assertThat("sweet", equalTo("sweet"));
  }
}
