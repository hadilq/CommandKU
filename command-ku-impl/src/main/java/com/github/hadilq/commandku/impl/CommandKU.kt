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

import com.github.hadilq.commandku.api.Command
import com.github.hadilq.commandku.api.CommandBall
import com.github.hadilq.commandku.api.CommandCallback
import com.github.hadilq.commandku.api.CommandKey
import com.github.hadilq.commandku.api.CommandRegister
import com.github.hadilq.commandku.api.CommandResultRegister
import com.github.hadilq.commandku.api.CommandResultShooter
import com.github.hadilq.commandku.api.CommandShooter
import com.github.hadilq.commandku.api.Registration
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass


class CommandRegisterImpl constructor(
  private val commandKU: CommandKU,
) : CommandRegister by commandKU

class CommandResultRegisterImpl constructor(
  private val commandKU: CommandKU,
) : CommandResultRegister by commandKU

class CommandShooterImpl constructor(
  private val commandKU: CommandKU,
) : CommandShooter by commandKU

class CommandResultShooterImpl constructor(
  private val commandKU: CommandKU,
) : CommandResultShooter by commandKU

/**
 * This is the core broker of this library and it's assumed to be a singleton.
 */
class CommandKU : CommandRegister, CommandResultRegister,
  CommandShooter, CommandResultShooter {

  private val mutex = Mutex()

  private val store = mutableMapOf<KClass<out Command>, MutableSet<Cmd>>()

  override fun <C : Command> register(
    commandClass: KClass<C>,
    callback: CommandCallback<C>,
  ): Registration = runBlocking {
    mutex.withLock { internalRegister(commandClass, callback) }
  }

  override fun <C : Command> register(
    commandClass: KClass<C>,
    key: CommandKey,
    callback: CommandCallback<C>
  ) = runBlocking {
    mutex.withLock { internalRegister(commandClass, key, callback) }
  }

  suspend fun <C : Command> cancel(callback: CommandCallback<C>) {
    mutex.withLock { internalCancel(callback) }
  }

  override suspend fun <C : Command> shoot(commandBall: CommandBall<C>): Boolean =
    mutex.withLock { internalShoot(commandBall) }
      ?.run { invoke(commandBall); true } ?: false

  override suspend fun <C : Command> shootResult(commandBall: CommandBall<C>) {
    mutex.withLock { internalShootResult(commandBall) }?.apply { invoke(commandBall) }
  }

  private fun <C : Command> internalRegister(
    commandClass: KClass<C>,
    callback: CommandCallback<C>,
  ): Registration {
    val element = RequestCmd(callback)
    store[commandClass] = store[commandClass]?.apply { add(element) } ?: mutableSetOf(element)
    return RegistrationImpl(this, callback)
  }

  private fun <C : Command> internalRegister(
    commandClass: KClass<C>,
    key: CommandKey,
    callback: CommandCallback<C>
  ) {
    val element = ResultCmd(key, callback)
    store[commandClass] = store[commandClass]?.apply { add(element) } ?: mutableSetOf(element)
  }

  private fun <C : Command> internalCancel(callback: CommandCallback<C>) {
    store.values.forEach { set ->
      set.firstOrNull { cmd ->
        when (cmd) {
          is RequestCmd<*> -> {
            cmd.callback === callback
          }
          is ResultCmd<*> -> {
            cmd.callback === callback
          }
        }
      }?.also {
        set.remove(it)
        return
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun <C : Command> internalShoot(
    commandBall: CommandBall<C>,
  ): CommandCallback<C>? = store[commandBall.commandClass]?.asSequence()
    ?.filterIsInstance<RequestCmd<*>>()
    ?.firstOrNull()
    ?.let { cmd -> (cmd.callback as CommandCallback<C>) }

  @Suppress("UNCHECKED_CAST")
  private fun <C : Command> internalShootResult(
    commandBall: CommandBall<C>,
  ): CommandCallback<C>? = store[commandBall.commandClass]?.asSequence()
    ?.filterIsInstance<ResultCmd<*>>()
    ?.firstOrNull { cmd -> cmd.key == commandBall.key }
    ?.let { cmd ->
      internalCancel(cmd.callback)
      cmd.callback as CommandCallback<C>
    }
}

private sealed class Cmd

private class RequestCmd<C : Command>(
  val callback: CommandCallback<C>,
) : Cmd()

private class ResultCmd<C : Command>(
  val key: CommandKey,
  val callback: CommandCallback<C>,
) : Cmd()

private class RegistrationImpl<C : Command>(
  private val cancelHandle: CommandKU,
  private val callback: CommandCallback<C>,
) : Registration {

  override suspend fun cancel() {
    cancelHandle.cancel(callback)
  }
}
