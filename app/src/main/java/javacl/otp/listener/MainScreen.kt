package javacl.otp.listener

import android.app.Activity
import android.content.IntentFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.auth.api.phone.SmsRetriever

@Composable
fun MainScreen() {

    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.filter { filter -> filter.isDigit() } },
            label = { Text("Otp code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    it.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)?.let { message ->
                        toOtp(message)?.let { otp ->
                            text = otp
                        }
                    }
                }
            }
        }
    )

    val context = LocalContext.current

    SmsRetriever.getClient(context).startSmsUserConsent(null)

    var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_START) {

                smsBroadcastReceiver = SmsBroadcastReceiver {
                    activityResultLauncher.launch(it)
                }

                val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
                context.registerReceiver(smsBroadcastReceiver, intentFilter)

            } else if (event == Lifecycle.Event.ON_STOP) {
                context.unregisterReceiver(smsBroadcastReceiver)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
