package com.simplemobiletools.clock.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.PowerManager
import android.telephony.SmsManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import co.nedim.maildroidx.callback
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.ProofOfTakingActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.network.ApiManager
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val alarm = context.dbHelper.getAlarmWithId(id) ?: return

        GlobalScope.launch {
            withContext(Dispatchers.Default) { openLedBox() }
        }



        if (context.isScreenOn()) {
            context.showAlarmNotification(alarm)
            sendSmS(alarm, context)
            context.dbHelper.updateAlarm(alarm)
//          sendEmail(alarm,context)
            context.setupAlarmClock(alarm, MINUTE_SECONDS)

        } else {

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            if (notificationManager.getNotificationChannel("Alarm") == null) {
                NotificationChannel("Alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                    setBypassDnd(true)
                    setSound(Uri.parse(alarm.soundUri), audioAttributes)
                    notificationManager.createNotificationChannel(this)
                }
            }

            val reminderActivityIntent = Intent(context, ProofOfTakingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ALARM_ID, id)
            }
            val preferences = context.getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)
            val name = preferences.getString(USER_NAME, "")
            val label = "${context.getString(R.string.app_launcher_name)} reminder : Hello $name ,"

//////////////////////
            val pendingIntent = PendingIntent.getActivity(context, 0, reminderActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val builder = NotificationCompat.Builder(context, "Alarm")
                .setSmallIcon(R.drawable.ic_icon_note)
                .setContentTitle(label).setContentText(
                    "it's time (${context.getFormattedTime(getPassedSeconds(), false, false)}) " +
                        "to take the medicine: ${alarm.pillName}"
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)

            try {
                notificationManager.notify(ALARM_NOTIF_ID, builder.build())

                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "simpleClock:showAlarmLock")
                wakeLock.acquire(10000)
            } catch (e: Exception) {
                context.showErrorToast(e)
            }



            sendSmS(alarm, context)
            context.dbHelper.updateAlarm(alarm)
//          sendEmail(alarm,context)
            Log.d("test65445", "  snozzzService")
            context.setupAlarmClock(alarm, MINUTE_SECONDS)

        }
    }

    private suspend fun openLedBox() {

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("a")

        when (current.format(formatter)) {
            AM -> {
                ApiManager.create().boxOneOpenLed()
            }
            PM -> {
                ApiManager.create().boxTwoOpenLed()
            }
        }
    }

    private fun sendEmail(alarm: com.simplemobiletools.clock.models.Alarm, context: Context) {
        val preferences = context.getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)
        val name = preferences.getString(USER_NAME, "")
        val email = preferences.getString(USER_EMAIL, "").toString()

        val subject = "Reminder to take medicine for $name."
        val message = "Hello $name, this is a reminder to take the medicine: ${alarm.pillName}, this is reminder number  ${alarm.snoozeTime} .  "

        sendEmailMaildroidX(email, subject, message)

        if (alarm.snoozeTime > 1) {

            val nameC = preferences.getString(USER_CONTACT_NAME, "")
            val emailC = preferences.getString(USER_CONTACT_EMAIL, "").toString()

            var managerContact = "Hello $nameC, $name did not take the medicine: ${alarm.pillName}.\n" +
                "He has already received ${alarm.snoozeTime} reminders"

            sendEmailMaildroidX(emailC, subject, message)

        }
    }


    private fun sendEmailMaildroidX(recipient: String, subject: String, message: String) {


        MaildroidX.Builder()
            .smtp("smtp.mailtrap.io")
            .smtpUsername("medicare")
            .smtpPassword("3untpx3p")
            .port("2525")
            .type(MaildroidXType.HTML)
            .to(recipient)
            .from(APP_EMAIL)
            .subject(subject)
            .body(message)
            .attachment("path_to_file/file.txt")
            .callback {
                timeOut(3000)
                onSuccess {
                    Log.d("MaildroidX", "SUCCESS")
                }
                onFail {
                    Log.d("MaildroidX", "FAIL")
                }
            }
    }

    private fun sendSmS(alarm: Alarm, context: Context) {

        val preferences = context.getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)
        val name = preferences.getString(USER_NAME, "")
        val phone = preferences.getString(USER_PHONE, "")

        val smsManager: SmsManager = SmsManager.getDefault()

        context.updateTimeSnoozeInData(alarm.id, ++alarm.snoozeTime)

        val message = "Hello $name, this is a reminder to take the medicine: ${alarm.pillName}, this is reminder number  ${alarm.snoozeTime} .  "


        smsManager.sendTextMessage(phone, null, message, null, null)

        if (alarm.snoozeTime > 1) {

            val nameC = preferences.getString(USER_CONTACT_NAME, "")
            val phoneC = preferences.getString(USER_CONTACT_PHONE, "")

            val messageContact = "Hello $nameC, $name did not take the medicine: ${alarm.pillName}.\n" +
                "He has already received ${alarm.snoozeTime} reminders"
            smsManager.sendTextMessage(phoneC, null, messageContact, null, null)

        }

    }


}
