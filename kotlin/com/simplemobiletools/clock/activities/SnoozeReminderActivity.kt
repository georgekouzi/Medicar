package com.simplemobiletools.clock.activities
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.hideNotification
import com.simplemobiletools.clock.extensions.setupAlarmClock
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.commons.extensions.showPickSecondsDialog
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS

class SnoozeReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("test65445","  com.simplemobiletools.clock.activities.SnoozeReminderActivity")

        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        showPickSecondsDialog(MINUTE_SECONDS, true, cancelCallback = { dialogCancelled() }) {
//            config.snoozeTime = it / MINUTE_SECONDS
            Log.d("test6545",it.toString())
            setupAlarmClock(alarm, MINUTE_SECONDS)
            finishActivity()
        }
    }


    private fun sendSmS() {

        val preferences = getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)

        preferences.getString(USER_NAME,"")
        val phone =preferences.getString(USER_PHONE,"")
        preferences.getString(USER_EMAIL,"")
        preferences.getString(USER_AGE,"")
        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phone, null, "hii", null, null)

    }

    private fun dialogCancelled() {
        finishActivity()
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(0, 0)
    }
}
