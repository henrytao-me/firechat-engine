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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by henrytao on 7/4/16.
 */
public class JobExecutor implements Executor {

  private static final int CORE_POOL_SIZE = 5;

  private static final long KEEP_ALIVE_TIME = 10;

  private static final int MAXIMUM_POOL_SIZE = 10;

  private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

  private static JobExecutor sInstance;

  public static JobExecutor getInstance() {
    JobExecutor jobExecutor = sInstance;
    if (jobExecutor == null) {
      synchronized (JobExecutor.class) {
        jobExecutor = sInstance;
        if (jobExecutor == null) {
          jobExecutor = sInstance = new JobExecutor();
        }
      }
    }
    return jobExecutor;
  }

  private final ThreadFactory mThreadFactory;

  private final ThreadPoolExecutor mThreadPoolExecutor;

  private final BlockingQueue<Runnable> mWorkQueue;

  protected JobExecutor() {
    mWorkQueue = new LinkedBlockingQueue<>();
    mThreadFactory = new ThreadFactory();
    mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, mWorkQueue, mThreadFactory);
  }

  @Override
  public void execute(Runnable command) {
    if (command == null) {
      throw new IllegalArgumentException("Runnable to execute cannot be null");
    }
    mThreadPoolExecutor.execute(command);
  }

  private static class ThreadFactory implements java.util.concurrent.ThreadFactory {

    private static final String THREAD_NAME = "firechat-executor-thread-";

    private int counter = 0;

    @Override
    public Thread newThread(Runnable runnable) {
      return new Thread(runnable, THREAD_NAME + counter);
    }
  }
}
