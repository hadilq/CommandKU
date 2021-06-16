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
package com.github.hadilq.commandku.api

import kotlin.reflect.KClass

/**
 * This is the entrance of this implementation. The main difference between this implementation
 * and other event-bus/message-queue libraries is that here we took advantage of Kotlin
 * coroutine to have a request-result pair of messages, but others just sent a message and
 * response is another message that needs to be match to make sense out of it. Therefore here,
 * every request has its own result pair, which [CommandExecutor] can match and filter them out.
 */
interface CommandExecutor {

  /**
   * The [input] is the request [Command] and the output is the result [Command] of [execute]
   * method. [inputClass] is the type of [input] and [expectedOut] is the type of result.
   */
  suspend fun <IN : Command, OUT : Command> execute(
    input: IN,
    inputClass: KClass<IN>,
    expectedOut: KClass<OUT>
  ): CommandResponse<OUT>
}

/**
 * An extension function to make calling [CommandExecutor.execute] easier.
 */
suspend inline
fun <reified IN : Command, reified OUT : Command> CommandExecutor.exe(
  input: IN
): CommandResponse<OUT> =
  execute(input, IN::class, OUT::class)
