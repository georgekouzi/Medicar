package com.simplemobiletools.clock.helpers
import android.content.Context
import android.util.Log
import com.simplemobiletools.clock.extensions.gson.gson
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    init {
        Log.d("test65445","  com.simplemobiletools.clock.helpers.Config")

    }
    companion object {
        fun newInstance(context: Context) = Config(context)
    }



    var timerMaxReminderSecs: Int
        get() = prefs.getInt(TIMER_MAX_REMINDER_SECS, DEFAULT_MAX_TIMER_REMINDER_SECS)
        set(timerMaxReminderSecs) = prefs.edit().putInt(TIMER_MAX_REMINDER_SECS, timerMaxReminderSecs).apply()

    var alarmSort: Int
        get() = prefs.getInt(ALARMS_SORT_BY, SORT_BY_CREATION_ORDER)
        set(alarmSort) = prefs.edit().putInt(ALARMS_SORT_BY, alarmSort).apply()

    val alarmMaxReminderSecs: Int
        get() = prefs.getInt(ALARM_MAX_REMINDER_SECS, DEFAULT_MAX_ALARM_REMINDER_SECS)

    val increaseVolumeGradually: Boolean
        get() = prefs.getBoolean(INCREASE_VOLUME_GRADUALLY, true)

    var alarmLastConfig: com.simplemobiletools.clock.models.Alarm?
        get() = prefs.getString(ALARM_LAST_CONFIG, null)?.let { lastAlarm ->
            gson.fromJson(lastAlarm, com.simplemobiletools.clock.models.Alarm::class.java)
        }
        set(alarm) = prefs.edit().putString(ALARM_LAST_CONFIG, gson.toJson(alarm)).apply()


}
