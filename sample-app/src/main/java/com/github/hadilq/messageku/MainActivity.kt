package com.github.hadilq.messageku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.hadilq.messageku.api.Available
import com.github.hadilq.messageku.api.Command
import com.github.hadilq.messageku.api.CommandCallbackImpl
import com.github.hadilq.messageku.api.CommandExecutor
import com.github.hadilq.messageku.api.CommandHook
import com.github.hadilq.messageku.api.CommandRegister
import com.github.hadilq.messageku.api.CommandResult
import com.github.hadilq.messageku.api.CommandResultShooter
import com.github.hadilq.messageku.api.exe
import com.github.hadilq.messageku.impl.CommandExecutorImpl
import com.github.hadilq.messageku.impl.MessageKU

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val broker = MessageKU()
    CH(broker).hookUp(broker)

    val executor = CommandExecutorImpl(broker, broker)

    suspend fun CommandExecutor.runCommand(request: RequestCommand): CommandResult<ResultCommand> =
      exe(request)

    lifecycleScope.launchWhenResumed {
      when (val result = executor.runCommand(RequestCommand("Are you there?"))) {
        is Available<*> -> assert(result.command == ResultCommand("Yes! Of course!"))
      }
    }
  }

}

data class RequestCommand(val request: String) : Command
data class ResultCommand(val result: String) : Command

class CH(
  private val commandShooter: CommandResultShooter,
) : CommandHook {

  override fun hookUp(commandRegister: CommandRegister) {
    commandRegister.register(RequestCommand::class,
      CommandCallbackImpl(commandShooter) {
        ResultCommand("Yes! Of course!")
      })
  }
}
