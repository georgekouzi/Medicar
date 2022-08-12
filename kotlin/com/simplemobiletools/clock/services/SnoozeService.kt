package com.simplemobiletools.clock.services
import android.app.IntentService
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import co.nedim.maildroidx.callback
import com.simplemobiletools.clock.extensions.dbHelper
import com.simplemobiletools.clock.extensions.setupAlarmClock
import com.simplemobiletools.clock.extensions.updateTimeSnoozeInData
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        val id = intent!!.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        sendSmS(alarm)
        dbHelper.updateAlarm(alarm)
//        sendEmail(alarm)
        Log.d("test65445","  snozzzService")

        setupAlarmClock(alarm, MINUTE_SECONDS)
        Log.d("test65445","  after setupAlarmClock")

    }

    private fun sendEmail(alarm: com.simplemobiletools.clock.models.Alarm) {
        val preferences = getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)
        var name = preferences.getString(USER_NAME,"")
        var email= preferences.getString(USER_EMAIL,"").toString()

        var subject ="Reminder to take medicine for $name."
        var message = "Hello $name, this is a reminder to take the medicine: ${alarm.pillName}, this is reminder number  ${alarm.snoozeTime} .  "

        sendEmailMaildroidX(email,subject,message)
//        Log.d("test6544",message)

        if(alarm.snoozeTime>1){

            var nameC = preferences.getString(USER_CONTACT_NAME,"")
            var emailC= preferences.getString(USER_CONTACT_EMAIL,"").toString()

            var managerContact ="Hello $nameC, $name did not take the medicine: ${alarm.pillName}.\n" +
                "He has already received ${alarm.snoozeTime} reminders"

            sendEmailMaildroidX(emailC,subject,message)
//            Log.d("test6544",managerContact)

        }
    }




   private fun sendEmailMaildroidX(recipient: String, subject: String, message: String){


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
                Log.d("MaildroidX",  "SUCCESS")
            }
            onFail {
                Log.d("MaildroidX",  "FAIL")
            }
        }
    }

    private fun sendSmS(alarm: com.simplemobiletools.clock.models.Alarm) {

        val preferences = getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)

        var name = preferences.getString(USER_NAME,"")
        val phone = preferences.getString(USER_PHONE,"")

        val smsManager: SmsManager = SmsManager.getDefault()
//        paracetamol
        updateTimeSnoozeInData(alarm.id ,++alarm.snoozeTime )

        var message = "Hello $name, this is a reminder to take the medicine: ${alarm.pillName}, this is reminder number  ${alarm.snoozeTime} .  "
//        Log.d("test6544",message)


        smsManager.sendTextMessage(phone, null, message, null, null)

        if( alarm.snoozeTime>1){

            var nameC = preferences.getString(USER_CONTACT_NAME,"")
            val phoneC = preferences.getString(USER_CONTACT_PHONE,"")

            var messageContact ="Hello $nameC, $name did not take the medicine: ${alarm.pillName}.\n" +
                "He has already received ${alarm.snoozeTime} reminders"
            smsManager.sendTextMessage(phoneC, null, messageContact, null, null)
//            Log.d("test6544",messageContact)

        }

    }


}
