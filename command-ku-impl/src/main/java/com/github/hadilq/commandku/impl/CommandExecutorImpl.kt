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
package com.github.hadilq.commandku.impl

import com.github.hadilq.commandku.api.Available
import com.github.hadilq.commandku.api.Command
import com.github.hadilq.commandku.api.CommandBall
import com.github.hadilq.commandku.api.CommandCallback
import com.github.hadilq.commandku.api.CommandExecutor
import com.github.hadilq.commandku.api.CommandKey
import com.github.hadilq.commandku.api.CommandResponse
import com.github.hadilq.commandku.api.CommandResultRegister
import com.github.hadilq.commandku.api.CommandShooter
import com.github.hadilq.commandku.api.NotAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
import kotlin.reflect.KClass

class CommandExecutorImpl constructor(
  private val commandShooter: CommandShooter,
  private val commandResultRegister: CommandResultRegister,
) : CommandExecutor {

  private val random = Random(Random.nextLong())

  @Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
  override suspend fun <IN : Command, OUT : Command> execute(
    input: IN,
    inputClass: KClass<IN>,
    expectedOut: KClass<OUT>,
  ): CommandResponse<OUT> = suspendCoroutine { con: Continuation<CommandResponse<OUT>> ->
    val newCommandKey = getNewCommandKey()
    commandResultRegister.register(expectedOut, newCommandKey, CommandCallbackImpl(con))
    CoroutineScope(con.context).launch {
      if (!commandShooter.shoot(CommandBall(newCommandKey, input, inputClass))) {
        con.resume(NotAvailable())
      }
    }
  }

  private fun getNewCommandKey(): CommandKey = CommandKey(random.nextLong())
}

private class CommandCallbackImpl<C : Command>(
  private val con: Continuation<CommandResponse<C>>,
) : CommandCallback<C> {

  override suspend fun invoke(commandBall: CommandBall<C>) {
    con.resume(Available(commandBall.command))
  }
}
