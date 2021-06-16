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
 * Broker for registration to receive requests.
 */
interface CommandRegister {

  /**
   * Register [callback] to receive requests.
   */
  fun <C : Command> register(
    commandClass: KClass<C>,
    callback: CommandCallback<C>,
  ): Registration
}

/**
 * Registration contract.
 */
interface Registration {

  /**
   * Dispose the registration contract.
   */
  suspend fun dispose()
}

/**
 * A handy implementation for [CommandCallback]. It can take care of request key,
 * [CommandBall.key], to be match with result key, [CommandBall.key].
 */
class CommandCallbackImpl<IN : Command, OUT : Command>(
  private val commandShooter: CommandResultShooter,
  private val commandResultClass: KClass<OUT>,
  private val result: suspend (IN) -> OUT,
) : CommandCallback<IN> {

  override suspend fun invoke(commandBall: CommandBall<IN>) {
    commandShooter.shootResult(
      CommandBall(
        commandBall.key,
        result(commandBall.command),
        commandResultClass
      )
    )
  }

  companion object {

    inline operator fun <IN : Command, reified OUT : Command> invoke(
      commandShooter: CommandResultShooter,
      noinline result: suspend (IN) -> OUT,
    ) = CommandCallbackImpl(commandShooter, OUT::class, result)
  }
}
