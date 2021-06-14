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
import com.github.hadilq.messageku.api.CommandResultBall
import com.github.hadilq.messageku.api.CommandResultCallback
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CommandRegisterImplTest {

  private val operation = CommandOperation()

  @Test
  fun `given_a_callback then_shoot_command`() = runBlocking {
    val commandRegister = CommandRegisterImpl(operation)
    val commandShooter = CommandShooterImpl(operation)
    val commandBall = CommandBall(CommandKey(Random.nextLong()), FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    commandRegister.register(FakeCommand::class, callback)
    commandShooter.shoot(commandBall)

    assertEquals(commandBall, callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_dispose then_shoot_command`() = runBlocking {
    val commandRegister = CommandRegisterImpl(operation)
    val commandShooter = CommandShooterImpl(operation)
    val commandBall = CommandBall(CommandKey(123), FakeCommand())
    val callback = FakeCommandCallback<FakeCommand>()

    val registration = commandRegister.register(FakeCommand::class, callback)
    registration.dispose()
    commandShooter.shoot(commandBall)

    assertNull(callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_result then_shoot_command`() = runBlocking {
    val commandResultRegister = CommandResultRegisterImpl(operation)
    val commandResultShooter = CommandResultShooterImpl(operation)
    val key = Random.nextLong()
    val commandBall = CommandResultBall(CommandKey(key), FakeCommand())
    val callback = FakeCommandResultCallback<FakeCommand>()

    commandResultRegister.register(FakeCommand::class, CommandKey(key), callback)
    commandResultShooter.shoot(commandBall)

    assertEquals(commandBall, callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_result then_shoot_command_twice`() = runBlocking {
    val commandResultRegister = CommandResultRegisterImpl(operation)
    val commandResultShooter = CommandResultShooterImpl(operation)
    val key = Random.nextLong()
    val commandBall = CommandResultBall(CommandKey(key), FakeCommand())
    val callback = FakeCommandResultCallback<FakeCommand>()

    commandResultRegister.register(FakeCommand::class, CommandKey(key), callback)
    commandResultShooter.shoot(commandBall)

    assertEquals(commandBall, callback.commandBall)
    callback.commandBall = null

    commandResultShooter.shoot(commandBall)

    assertNull(callback.commandBall)
  }

  @Test
  fun `given_a_callback_register_result then_shoot_command_with_different_key`() = runBlocking {
    val commandResultRegister = CommandResultRegisterImpl(operation)
    val commandResultShooter = CommandResultShooterImpl(operation)
    val key1 = Random.nextLong()
    val key2 = key1 + 1
    val commandBall = CommandResultBall(CommandKey(key1), FakeCommand())
    val callback = FakeCommandResultCallback<FakeCommand>()

    commandResultRegister.register(FakeCommand::class, CommandKey(key2), callback)
    commandResultShooter.shoot(commandBall)

    assertNull(callback.commandBall)
  }


  class FakeCommand : Command

  class FakeCommandCallback<C : Command> : CommandCallback<C> {

    var commandBall: CommandBall<C>? = null

    override suspend fun invoke(commandBall: CommandBall<C>) {
      this.commandBall = commandBall
    }
  }

  class FakeCommandResultCallback<C : Command> : CommandResultCallback<C> {

    var commandBall: CommandResultBall<C>? = null

    override suspend fun invoke(commandBall: CommandResultBall<C>) {
      this.commandBall = commandBall
    }
  }
}