package javacl.otp.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

internal class SmsBroadcastReceiver(private val onSuccess: (Intent?) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status?
            when (smsRetrieverStatus?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val messageIntent =
                        extras?.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    onSuccess.invoke(messageIntent)
                }
            }
        }
    }
}
