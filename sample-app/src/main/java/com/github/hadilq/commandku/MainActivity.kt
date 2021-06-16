package com.github.hadilq.commandku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.hadilq.commandku.api.Available
import com.github.hadilq.commandku.api.Command
import com.github.hadilq.commandku.api.CommandCallbackImpl
import com.github.hadilq.commandku.api.CommandExecutor
import com.github.hadilq.commandku.api.CommandResponse
import com.github.hadilq.commandku.api.Registration
import com.github.hadilq.commandku.api.exe
import com.github.hadilq.commandku.impl.CommandExecutorImpl
import com.github.hadilq.commandku.impl.CommandKU
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

  private var registration: Registration? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val broker = CommandKU()

    registration = broker.register(RequestCommand::class,
      CommandCallbackImpl(broker) {
        ResultCommand("Yes! Of course!")
      })

    val executor = CommandExecutorImpl(broker, broker)

    suspend fun CommandExecutor.runCommand(request: String): CommandResponse<ResultCommand> =
      exe(RequestCommand(request))

    lifecycleScope.launchWhenResumed {
      when (val result = executor.runCommand("Are you there?")) {
        is Available<*> -> assert(result.command == ResultCommand("Yes! Of course!"))
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    runBlocking {
      registration?.cancel()
    }
  }
}

data class RequestCommand(val request: String) : Command
data class ResultCommand(val result: String) : Command
