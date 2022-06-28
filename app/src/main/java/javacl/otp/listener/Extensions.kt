package javacl.otp.listener

import java.util.regex.Pattern

fun toOtp(message: String): String? {
    val otpPattern = Pattern.compile("(|^)\\d{5}")
    val matcher = otpPattern.matcher(message)
    return if (matcher.find()) matcher.group(0) else null
}
