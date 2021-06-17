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
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CommandKUTest {

  private val commandKU = CommandKU()

  @Test
  fun `given_a_callback then_shoot_command`() = runBlocking {
    val commandRegister = CommandRegisterImpl(commandKU)
    val commandShooter = CommandShooterImpl(commandKU)
    val commandBall = CommandBall(Random.nextLong(), FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    commandRegister.register(FakeCommand::class, callback)
    commandShooter.shoot(commandBall)

    assertEquals(commandBall, callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_dispose then_shoot_command`() = runBlocking {
    val commandRegister = CommandRegisterImpl(commandKU)
    val commandShooter = CommandShooterImpl(commandKU)
    val commandBall = CommandBall(123L, FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    val registration = commandRegister.register(FakeCommand::class, callback)
    registration.cancel()
    commandShooter.shoot(commandBall)

    assertNull(callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_result then_shoot_command`() = runBlocking {
    val commandResultRegister = CommandResultRegisterImpl(commandKU)
    val commandResultShooter = CommandResultShooterImpl(commandKU)
    val key = Random.nextLong()
    val commandBall = CommandBall(key, FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    commandResultRegister.register(FakeCommand::class, key, callback)
    commandResultShooter.shootResult(commandBall)

    assertEquals(commandBall, callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_result then_shoot_command_twice`() = runBlocking {
    val commandResultRegister = CommandResultRegisterImpl(commandKU)
    val commandResultShooter = CommandResultShooterImpl(commandKU)
    val key = Random.nextLong()
    val commandBall = CommandBall(key, FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    commandResultRegister.register(FakeCommand::class, key, callback)
    commandResultShooter.shootResult(commandBall)

    assertEquals(commandBall, callback.commandBall)
    callback.commandBall = null

    commandResultShooter.shootResult(commandBall)

    assertNull(callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_result then_shoot_command_with_different_key`() = runBlocking {
    val commandResultRegister = CommandResultRegisterImpl(commandKU)
    val commandResultShooter = CommandResultShooterImpl(commandKU)
    val key1 = Random.nextLong()
    val key2 = key1 + 1
    val commandBall = CommandBall(key1, FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    commandResultRegister.register(FakeCommand::class, key2, callback)
    commandResultShooter.shootResult(commandBall)

    assertNull(callback.commandBall)
  }


  class FakeCommand : Command

  class FakeCommandCallback<C : Command> : CommandCallback<C> {

    var commandBall: CommandBall<C>? = null

    override suspend fun invoke(commandBall: CommandBall<C>) {
      this.commandBall = commandBall
    }
  }
}