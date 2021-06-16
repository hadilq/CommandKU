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
import com.github.hadilq.commandku.api.CommandCallbackImpl
import com.github.hadilq.commandku.api.CommandResult
import com.github.hadilq.commandku.api.NotAvailable
import com.github.hadilq.commandku.api.exe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CommandExecutorImplTest {

  private val commandKU = CommandKU()

  @Test
  fun `given_a_command then_available_result_command`() = runBlocking {
    val commandRegister = CommandRegisterImpl(commandKU)
    val commandShooter = CommandShooterImpl(commandKU)
    val commandResultShooter = CommandResultShooterImpl(commandKU)
    val commandResultRegister = CommandResultRegisterImpl(commandKU)
    val executor = CommandExecutorImpl(commandShooter, commandResultRegister)
    val expectedResult = FakeResultCommand()
    commandRegister.register(
      FakeCommand::class,
      CommandCallbackImpl(commandResultShooter) {
        expectedResult
      })

    val result: CommandResult<FakeResultCommand> = executor.exe(FakeCommand())

    assertTrue(result is Available<*>)
    val command = (result as Available<FakeResultCommand>).command
    assertEquals(expectedResult, command)
  }

  @Test
  fun `given_a_command then_not_available_result_command`() = runBlocking {
    val commandShooter = CommandShooterImpl(commandKU)
    val commandResultRegister = CommandResultRegisterImpl(commandKU)
    val executor = CommandExecutorImpl(commandShooter, commandResultRegister)

    val result: CommandResult<FakeResultCommand> = executor.exe(FakeCommand())

    assertTrue(result is NotAvailable<*>)
  }

  class FakeCommand : Command
  class FakeResultCommand : Command
}