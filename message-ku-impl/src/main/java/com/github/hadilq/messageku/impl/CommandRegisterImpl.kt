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
import com.github.hadilq.messageku.api.CommandCallback
import com.github.hadilq.messageku.api.CommandKey
import com.github.hadilq.messageku.api.CommandRegister
import com.github.hadilq.messageku.api.CommandResultBall
import com.github.hadilq.messageku.api.CommandResultCallback
import com.github.hadilq.messageku.api.CommandResultRegister
import com.github.hadilq.messageku.api.CommandResultShooter
import com.github.hadilq.messageku.api.CommandShooter
import com.github.hadilq.messageku.api.Registration
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KClass


class CommandRegisterImpl constructor(
  private val operation: CommandOperation,
) : CommandRegister by operation

class CommandResultRegisterImpl constructor(
  private val operation: CommandOperation,
) : CommandResultRegister by operation

class CommandShooterImpl constructor(
  private val operation: CommandOperation,
) : CommandShooter by operation

class CommandResultShooterImpl constructor(
  private val operation: CommandOperation,
) : CommandResultShooter by operation

class CommandOperation : CommandRegister, CommandResultRegister,
  CommandShooter, CommandResultShooter {

  private val mutex = Mutex()

  private val store = mutableMapOf<KClass<out Command>, MutableSet<Cmd>>()

  override fun <C : Command> register(
    commandClass: KClass<C>,
    callback: CommandCallback<C>,
  ): Registration {
    val element = RequestCmd(callback)
    store[commandClass] = store[commandClass]?.apply { add(element) } ?: mutableSetOf(element)
    return RegistrationImpl(this, callback)
  }

  override fun <C : Command> register(
    commandClass: KClass<C>,
    key: CommandKey,
    callback: CommandResultCallback<C>
  ) {
    val element = ResultCmd(key, callback)
    store[commandClass] = store[commandClass]?.apply { add(element) } ?: mutableSetOf(element)
  }

  suspend fun <C : Command> dispose(callback: CommandCallback<C>) {
    store.values.forEach { set ->
      set.firstOrNull { cmd ->
        when (cmd) {
          is RequestCmd<*> -> {
            cmd.callback === callback
          }
          else -> false
        }
      }?.also {
        set.remove(it)
        return
      }
    }
  }

  private suspend fun <C : Command> disposeResult(callback: CommandResultCallback<C>) {
    store.values.forEach { set ->
      set.firstOrNull { cmd ->
        when (cmd) {
          is ResultCmd<*> -> {
            cmd.callback === callback
          }
          else -> false
        }
      }?.also {
        set.remove(it)
        return
      }
    }
  }

  @Suppress("UNCHECKED_CAST", "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
  override suspend fun <C : Command> shoot(commandBall: CommandBall<C>): Boolean {
    var found = false
    store[commandBall.commandClass]?.forEach { cmd ->
      if (cmd is RequestCmd<*>) {
        (cmd.callback as CommandCallback<C>).invoke(commandBall)
        found = true
      }
    }
    return found
  }

  @Suppress("UNCHECKED_CAST", "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
  override suspend fun <C : Command> shoot(commandBall: CommandResultBall<C>) {
    store[commandBall.commandClass]?.forEach { cmd ->
      if (cmd is ResultCmd<*> && cmd.key == commandBall.key) {
        (cmd.callback as CommandResultCallback<C>).invoke(commandBall)
        disposeResult(cmd.callback)
      }
    }
  }
}

private sealed class Cmd

private class RequestCmd<C : Command>(
  val callback: CommandCallback<C>,
) : Cmd()

private class ResultCmd<C : Command>(
  val key: CommandKey,
  val callback: CommandResultCallback<C>,
) : Cmd()

private class RegistrationImpl<C : Command>(
  private val disposer: CommandOperation,
  private val callback: CommandCallback<C>,
) : Registration {

  override suspend fun dispose() {
    disposer.dispose(callback)
  }
}
