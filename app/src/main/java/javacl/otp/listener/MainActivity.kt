package javacl.otp.listener

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.auth.api.phone.SmsRetriever
import javacl.otp.listener.ui.theme.OtplistenerTheme
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OtplistenerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting() {

    val context = LocalContext.current

    SmsRetriever.getClient(context).startSmsUserConsent(null)

    var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    var text by remember { mutableStateOf("") }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    it.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)?.let { message ->
                        getOtpFromMessage(message)?.let { otp ->
                            text = otp
                        }
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.CenterVertically)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.filter { filter -> filter.isDigit() } },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = { Text("Otp code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_START) {

                smsBroadcastReceiver = SmsBroadcastReceiver()

                smsBroadcastReceiver?.setSmsBroadcastReceiverListener(

                    object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {

                        override fun onSuccess(intent: Intent?) {
                            activityResultLauncher.launch(intent)
                        }

                        override fun onFailure() {}
                    })

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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OtplistenerTheme {
        Greeting()
    }
}

private fun getOtpFromMessage(message: String): String? {
    val otpPattern = Pattern.compile("(|^)\\d{5}")
    val matcher = otpPattern.matcher(message)
    return if (matcher.find()) matcher.group(0) else null
}
