package com.simplemobiletools.clock.extensions
import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager.STREAM_ALARM
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat

import com.simplemobiletools.clock.activities.ProofOfTakingActivity
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SplashActivity
import com.simplemobiletools.clock.databases.DBHelper
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.receivers.AlarmReceiver
import com.simplemobiletools.clock.receivers.HideAlarmReceiver
import com.simplemobiletools.clock.services.SnoozeService
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import java.util.*
import kotlin.math.pow

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.createNewAlarm(timeInMinutes: Int, weekDays: Int): Alarm {
    val defaultAlarmSound = getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
    return com.simplemobiletools.clock.models.Alarm(
        0, timeInMinutes, weekDays,
        isEnabled = false,
        vibrate = false,
        soundTitle = defaultAlarmSound.title,
        soundUri = defaultAlarmSound.uri,
        pillName = "",
        pillDescription = "",
        image = "",
        date = CurrentFormatDate(),
        snoozeTime = 0


    )
}


fun Context.scheduleNextAlarm(alarm: com.simplemobiletools.clock.models.Alarm, showToast: Boolean) {
    Log.d("test65445","  com.simplemobiletools.clock.extensions.scheduleNextAlarm")

    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    val currentTimeInMinutes = getCurrentDayMinutes()

    if (alarm.days == TODAY_BIT) {
        val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes
        setupAlarmClock(alarm, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))

        if (showToast) {
            showRemainingTimeMessage(triggerInMinutes)
        }
    } else if (alarm.days == TOMORROW_BIT) {
        val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes + DAY_MINUTES
        setupAlarmClock(alarm, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))

        if (showToast) {
            showRemainingTimeMessage(triggerInMinutes)
        }
    } else {
        for (i in 0..7) {
            val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val isCorrectDay = alarm.days and 2.0.pow(currentDay).toInt() != 0
            if (isCorrectDay && (alarm.timeInMinutes > currentTimeInMinutes || i > 0)) {
                val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)
                setupAlarmClock(alarm, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))

                if (showToast) {
                    showRemainingTimeMessage(triggerInMinutes)
                }
                break
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }
}

fun Context.showRemainingTimeMessage(totalMinutes: Int) {
    val fullString = String.format(getString(R.string.time_remaining), formatMinutesToTimeString(totalMinutes))
    toast(fullString, Toast.LENGTH_LONG)
}

fun Context.setupAlarmClock(alarm: com.simplemobiletools.clock.models.Alarm, triggerInSeconds: Int) {
    Log.d("test65445","com.simplemobiletools.clock.extensions.setupAlarmClock $triggerInSeconds")
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetMS = System.currentTimeMillis() + triggerInSeconds * 1000
    AlarmManagerCompat.setAlarmClock(alarmManager, targetMS, getOpenAlarmTabIntent(), getAlarmIntent(alarm))
}

fun Context.updateTimeSnoozeInData(alarm_id: Int, snoozTime: Int) {
    var alarm = dbHelper.getAlarmWithId(alarm_id) ?: return
    alarm.snoozeTime = snoozTime

    dbHelper.updateAlarm(alarm)
}
// fun Context.sendEmail(recipient: String, subject: String, message: String): PendingIntent? {
//    /*ACTION_SEND action to launch an email client installed on your Android device.*/
//    val mIntent = Intent(Intent.ACTION_SEND)
//    /*To send an email you need to specify mailto: as URI using setData() method
//    and data type will be to text/plain using setType() method*/
//    mIntent.data = Uri.parse("mailto:")
//    mIntent.type = "text/plain"
//    // put recipient email in intent
//    /* recipient is put as array because you may wanna send email to multiple emails
//       so enter comma(,) separated emails, it will be stored in array*/
//    mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
//    //put the Subject in the intent
//    mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
//    //put the message in the intent
//    mIntent.putExtra(Intent.EXTRA_TEXT, message)
//
//
//     return PendingIntent.getActivity(this,com.simplemobiletools.clock.helpers.OPEN_APP_INTENT_ID,mIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//}

fun Context.getOpenAlarmTabIntent(): PendingIntent {
    val intent = getLaunchIntent() ?: Intent(this, SplashActivity::class.java)
    intent.putExtra(OPEN_TAB, TAB_ALARM)
    return PendingIntent.getActivity(this, OPEN_ALARMS_TAB_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}


fun Context.getAlarmIntent(alarm: com.simplemobiletools.clock.models.Alarm): PendingIntent {
    val intent = Intent(this, AlarmReceiver::class.java)
    intent.putExtra(ALARM_ID, alarm.id)
    return PendingIntent.getBroadcast(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.cancelAlarmClock(alarm: com.simplemobiletools.clock.models.Alarm) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getAlarmIntent(alarm))
}

fun Context.hideNotification(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}

fun Context.hideTimerNotification(timerId: Int) = hideNotification(timerId)


fun Context.getFormattedTime(passedSeconds: Int, showSeconds: Boolean, makeAmPmSmaller: Boolean): SpannableString {
    val use24HourFormat = DateFormat.is24HourFormat(this)
    val hours = (passedSeconds / 3600) % 24
    val minutes = (passedSeconds / 60) % 60
    val seconds = passedSeconds % 60

    return if (use24HourFormat) {
        val formattedTime = formatTime(showSeconds, use24HourFormat, hours, minutes, seconds)
        SpannableString(formattedTime)
    } else {
        val formattedTime = formatTo12HourFormat(showSeconds, hours, minutes, seconds)
        val spannableTime = SpannableString(formattedTime)
        val amPmMultiplier = if (makeAmPmSmaller) 0.4f else 1f
        spannableTime.setSpan(RelativeSizeSpan(amPmMultiplier), spannableTime.length - 3, spannableTime.length, 0)
        spannableTime
    }
}

fun Context.formatTo12HourFormat(showSeconds: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val appendable = getString(if (hours >= 12) R.string.p_m else R.string.a_m)
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}

fun Context.getNextAlarm(): String {
    val milliseconds = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: return ""
    val calendar = Calendar.getInstance()
    val isDaylightSavingActive = TimeZone.getDefault().inDaylightTime(Date())
    var offset = calendar.timeZone.rawOffset
    if (isDaylightSavingActive) {
        offset += TimeZone.getDefault().dstSavings
    }

    calendar.timeInMillis = milliseconds
    val dayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val dayOfWeek = resources.getStringArray(R.array.week_days_short)[dayOfWeekIndex]
    val formatted = getFormattedTime(((milliseconds + offset) / 1000L).toInt(), false, false)
    return "$dayOfWeek $formatted"
}

fun Context.rescheduleEnabledAlarms() {
    dbHelper.getEnabledAlarms().forEach {
        if (it.days != TODAY_BIT || it.timeInMinutes > getCurrentDayMinutes()) {
            scheduleNextAlarm(it, false)
        }
    }
}

fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn

fun Context.showAlarmNotification(alarm: com.simplemobiletools.clock.models.Alarm) {
    Log.d("test65445"," Context - com.simplemobiletools.clock.extensions.showAlarmNotification")
    val pendingIntent = getOpenAlarmTabIntent()
    val notification = getAlarmNotification(pendingIntent, alarm)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    try {
        notificationManager.notify(alarm.id, notification)
    } catch (e: Exception) {
        showErrorToast(e)
    }

    if (alarm.days > 0) {
        Log.d("test65445","  alarm.days > 0")

        scheduleNextAlarm(alarm, false)
    }
}

fun Context.getProofOfTakingActivity(alarm_id: Int): PendingIntent {
    val intent = Intent(this, ProofOfTakingActivity::class.java)
    intent.putExtra(ALARM_ID, alarm_id)
    return PendingIntent.getActivity(this, alarm_id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}


fun Context.getHideAlarmPendingIntent(alarm_id: Int): PendingIntent {
    val intent = Intent(this, HideAlarmReceiver::class.java)
    intent.putExtra(ALARM_ID, alarm_id)
    return PendingIntent.getBroadcast(this, alarm_id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getAlarmNotification(pendingIntent: PendingIntent, alarm: com.simplemobiletools.clock.models.Alarm): Notification {
    val soundUri = alarm.soundUri
    if (soundUri != SILENT) {
        grantReadUriPermission(soundUri)
    }
    val preferences = getSharedPreferences(USER, AppCompatActivity.MODE_PRIVATE)
    var name = preferences.getString(USER_NAME, "")
    val channelId = "simple_alarm_channel_$soundUri"
    val label = "${getString(R.string.app_launcher_name)} reminder : Hello $name ,"


    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setLegacyStreamType(STREAM_ALARM)
        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
        .build()

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val importance = NotificationManager.IMPORTANCE_HIGH
    NotificationChannel(channelId, label, importance).apply {
        setBypassDnd(true)
        enableLights(true)
//        lightColor = getProperPrimaryColor()
        enableVibration(alarm.vibrate)
        setSound(Uri.parse(soundUri), audioAttributes)
        notificationManager.createNotificationChannel(this)
    }

    val dismissIntent = getHideAlarmPendingIntent(alarm.id)
    val builder = NotificationCompat.Builder(this)
        .setContentTitle(label)

        .setContentText(
            "it's time (${getFormattedTime(getPassedSeconds(), false, false)}) " +
                "to take the medicine: ${alarm.pillName}"
        )
        .setSmallIcon(R.drawable.ic_icon_note)
        .setContentIntent(pendingIntent)
        .setPriority(Notification.PRIORITY_HIGH)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true)
        .setChannelId(channelId)
        .addAction(R.drawable.ic_cross_vector, "proof", getProofOfTakingActivity(alarm.id))
        .setDeleteIntent(dismissIntent)

    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (soundUri != SILENT) {
        builder.setSound(Uri.parse(soundUri), STREAM_ALARM)
    }

    if (alarm.vibrate) {
        val vibrateArray = LongArray(2) { 500 }
        builder.setVibrate(vibrateArray)
    }

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}

fun Context.getSnoozePendingIntent(alarm: com.simplemobiletools.clock.models.Alarm): PendingIntent {
    val snoozeClass = SnoozeService::class.java
    val intent = Intent(this, snoozeClass).setAction("Snooze")
    Log.d("test6544", "innn")
    Log.d("test65445"," Context - com.simplemobiletools.clock.extensions.getSnoozePendingIntent")

    intent.putExtra(ALARM_ID, alarm.id)
    return PendingIntent.getService(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getReminderActivityIntent(): PendingIntent {
    val intent = Intent(this, ProofOfTakingActivity::class.java)
    return PendingIntent.getActivity(this, REMINDER_ACTIVITY_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}


fun Context.checkAlarmsWithDeletedSoundUri(uri: String) {
    val defaultAlarmSound = getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
    dbHelper.getAlarmsWithUri(uri).forEach {
        it.soundTitle = defaultAlarmSound.title
        it.soundUri = defaultAlarmSound.uri
        dbHelper.updateAlarm(it)
    }
}

fun Context.getAlarmSelectedDaysString(bitMask: Int): String {
    return when (bitMask) {
        TODAY_BIT -> getString(R.string.today)
        TOMORROW_BIT -> getString(R.string.tomorrow)
        EVERY_DAY_BIT -> getString(R.string.every_day)
        else -> getSelectedDaysString(bitMask)
    }
}

fun Context.firstDayOrder(bitMask: Int): Int {
    if (bitMask == TODAY_BIT) return -2
    if (bitMask == TOMORROW_BIT) return -1

    val dayBits = arrayListOf(MONDAY_BIT, TUESDAY_BIT, WEDNESDAY_BIT, THURSDAY_BIT, FRIDAY_BIT, SATURDAY_BIT, SUNDAY_BIT)

    dayBits.moveLastItemToFront()

    dayBits.forEach { bit ->
        if (bitMask and bit != 0) {
            return 0
        }
    }

    return bitMask
}

fun Context.updateWidgets() {
    updateDigitalWidgets()
    updateAnalogueWidgets()
}

fun Context.updateDigitalWidgets() {
    val component = ComponentName(applicationContext, MyDigitalTimeWidgetProvider::class.java)
    val widgetIds = AppWidgetManager.getInstance(applicationContext)?.getAppWidgetIds(component) ?: return
    if (widgetIds.isNotEmpty()) {
        val ids = intArrayOf(R.xml.widget_digital_clock_info)
        Intent(applicationContext, MyDigitalTimeWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(this)
        }
    }
}

fun Context.updateAnalogueWidgets() {
    val component = ComponentName(applicationContext, MyAnalogueTimeWidgetProvider::class.java)
    val widgetIds = AppWidgetManager.getInstance(applicationContext)?.getAppWidgetIds(component) ?: return
    if (widgetIds.isNotEmpty()) {
        val ids = intArrayOf(R.xml.widget_analogue_clock_info)
        Intent(applicationContext, MyAnalogueTimeWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(this)
        }
    }
}
