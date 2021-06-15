/**
 * Copyright 2021 Hadi Lashkari Ghouchani

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hadilq.messageku.api

import kotlin.reflect.KClass

/**
 * Broker for registration to receive the result.
 */
interface CommandResultRegister {

  /**
   * Register to receive the result, which matches [key], in [callback]. After receiving the
   * result, [callback] will be disposed.
   */
  fun <C : Command> register(
      commandClass: KClass<C>,
      key: CommandKey,
      callback: CommandResultCallback<C>,
  )
}
