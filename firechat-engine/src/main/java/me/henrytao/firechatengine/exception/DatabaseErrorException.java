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

package me.henrytao.firechatengine.exception;

import com.google.firebase.database.DatabaseError;

public class DatabaseErrorException extends RuntimeException {

  public static DatabaseErrorException create(DatabaseError error) {
    return new DatabaseErrorException(error);
  }

  private final DatabaseError mError;

  protected DatabaseErrorException(DatabaseError error) {
    mError = error;
  }

  public DatabaseError getException() {
    return mError;
  }
}