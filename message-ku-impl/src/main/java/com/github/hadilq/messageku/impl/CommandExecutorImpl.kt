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
package com.github.hadilq.messageku.impl

import com.github.hadilq.messageku.api.Command
import com.github.hadilq.messageku.api.CommandBall
import com.github.hadilq.messageku.api.CommandExecutor
import com.github.hadilq.messageku.api.CommandKey
import com.github.hadilq.messageku.api.CommandResult
import com.github.hadilq.messageku.api.CommandResultBall
import com.github.hadilq.messageku.api.CommandResultCallback
import com.github.hadilq.messageku.api.CommandResultRegister
import com.github.hadilq.messageku.api.CommandShooter
import com.github.hadilq.messageku.api.NotAvailable
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
  ): CommandResult<OUT> = suspendCoroutine { con: Continuation<CommandResult<OUT>> ->
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
  private val con: Continuation<CommandResult<C>>,
) : CommandResultCallback<C> {

  override suspend fun invoke(commandBall: CommandResultBall<C>) {
    con.resume(commandBall.command)
  }
}
