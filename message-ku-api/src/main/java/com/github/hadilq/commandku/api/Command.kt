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
 * A [Command] is a base interface for any reactive packages, which can be a request
 * or a result.
 */
interface Command

/**
 * A wrapper for [Command], which is useful while trying to request and the no callback
 * registered to receive it.
 */
sealed class CommandResult<C : Command>

/**
 * A callback is available/registered and the result command is [command].
 */
class Available<C : Command>(val command: C) : CommandResult<C>()

/**
 * No callback is available/registered.
 */
class NotAvailable<C : Command> : CommandResult<C>()

/**
 * They key to match up requests and results.
 */
@JvmInline
value class CommandKey(
  val key: Long,
)

/**
 * Keep the request [command] next to its [key].
 */
class CommandBall<C : Command>(
  val key: CommandKey,
  val command: C,
  val commandClass: KClass<C>
) {

  companion object {

    inline operator fun <reified C : Command> invoke(
      key: CommandKey,
      command: C,
    ) = CommandBall(key, command, C::class)
  }
}

/**
 * Callback to receive the requests and results.
 */
interface CommandCallback<C : Command> {
  suspend fun invoke(commandBall: CommandBall<C>)
}
