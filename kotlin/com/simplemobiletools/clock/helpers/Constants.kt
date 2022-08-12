package com.simplemobiletools.clock.helpers
import com.simplemobiletools.clock.enum.ApiChoice
import com.simplemobiletools.commons.helpers.DAY_MINUTES
import com.simplemobiletools.commons.helpers.DAY_SECONDS
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.pow

// shared com.simplemobiletools.clock.extensions.getPreferences
const val SELECTED_TIME_ZONES = "selected_time_zones"
const val EDITED_TIME_ZONE_TITLES = "edited_time_zone_titles"
const val TIMER_SECONDS = "timer_seconds"
const val TIMER_VIBRATE = "timer_vibrate"
const val TIMER_SOUND_URI = "timer_sound_uri"
const val TIMER_SOUND_TITLE = "timer_sound_title"
const val TIMER_CHANNEL_ID = "timer_channel_id"
const val TIMER_LABEL = "timer_label"
const val TIMER_MAX_REMINDER_SECS = "timer_max_reminder_secs"
const val ALARM_MAX_REMINDER_SECS = "alarm_max_reminder_secs"
const val ALARM_LAST_CONFIG = "alarm_last_config"
const val TIMER_LAST_CONFIG = "timer_last_config"
const val INCREASE_VOLUME_GRADUALLY = "increase_volume_gradually"
const val ALARMS_SORT_BY = "alarms_sort_by"
const val STOPWATCH_LAPS_SORT_BY = "stopwatch_laps_sort_by"
const val snooze=60

const val CAMERA=2
const val GALLERY=3
const val IMAGE_DIRECTORY="com.simplemobiletools.clock.activities.PillImage"
const val TABS_COUNT = 1
const val GALLERY_OR_CAMERA="myPillImage"

const val USER_NAME="userName"
const val USER_AGE="userAge"
const val USER_EMAIL="userEmail"
const val USER_PHONE="userPhone"



const val USER_CONTACT_NAME="userContactName"
const val USER_CONTACT_AGE="userContactAge"
const val USER_CONTACT_EMAIL="userContactEmail"
const val USER_CONTACT_PHONE="userContactPhone"


const val IMAGE_PROOF="imageProof"
const val PILL_BOX_PROOF="pillBoxProof"

const val USER="user"
const val CONTACT="userContactName"
const val FIRST_TIME ="firstTime"



const val CAMERA_PERMISSION_CODE=1
const val EDITED_TIME_ZONE_SEPARATOR = ":"
const val ALARM_ID = "alarm_id"
const val DEFAULT_ALARM_MINUTES = 480
const val DEFAULT_MAX_ALARM_REMINDER_SECS = 60
const val DEFAULT_MAX_TIMER_REMINDER_SECS = 60

const val PICK_AUDIO_FILE_INTENT_ID = 9994
const val REMINDER_ACTIVITY_INTENT_ID = 9995
const val PROOF_ACTIVITY_INTENT_ID = 10002
const val  APP_EMAIL ="medicareforlifeapp@gmail.com"
const val OPEN_ALARMS_TAB_INTENT_ID = 9996
const val OPEN_STOPWATCH_TAB_INTENT_ID = 9993
const val OPEN_APP_INTENT_ID = 9998
const val ALARM_NOTIF_ID = 9998
const val TIMER_RUNNING_NOTIF_ID = 10000
const val STOPWATCH_RUNNING_NOTIF_ID = 10001

const val OPEN_TAB = "open_tab"
const val TAB_CLOCK = 0
const val TAB_ALARM = 1
const val TIMER_ID = "timer_id"
const val INVALID_TIMER_ID = -1

// stopwatch sorting
const val SORT_BY_LAP = 1
const val SORT_BY_LAP_TIME = 2
const val SORT_BY_TOTAL_TIME = 4

// alarm sorting
const val SORT_BY_CREATION_ORDER = 0
const val SORT_BY_ALARM_TIME = 1
const val SORT_BY_DATE_AND_TIME = 2

const val TODAY_BIT = -1
const val TOMORROW_BIT = -2

const val AM="AM"
const val PM="PM"

const val BASE_URL = "http://192.168.1.23:8000/"




fun getPassedSeconds(): Int {
    val calendar = Calendar.getInstance()
    val isDaylightSavingActive = TimeZone.getDefault().inDaylightTime(Date())
    var offset = calendar.timeZone.rawOffset
    if (isDaylightSavingActive) {
        offset += TimeZone.getDefault().dstSavings
    }
    return ((calendar.timeInMillis + offset) / 1000).toInt()
}

fun formatTime(showSeconds: Boolean, use24HourFormat: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}

fun getTomorrowBit(): Int {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_WEEK, 1)
    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return 2.0.pow(dayOfWeek).toInt()
}

fun getCurrentDayMinutes(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

fun CurrentFormatDate(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    return current.format(formatter)
}

fun formatDateByMinutes(timeInMinutes: Int): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val day = timeInMinutes * 60 / DAY_SECONDS
    val period = Period.of(0, 0, day)
    val modifiedDate = current.plus(period)
    return modifiedDate.format(formatter)
}

fun getDate(days: Int,timeInMinutes: Int):String {
    val currentTimeInMinutes = getCurrentDayMinutes()

    when (days) {
        TODAY_BIT -> {
            val triggerInMinutes = timeInMinutes - currentTimeInMinutes
            return formatDateByMinutes(triggerInMinutes)

        }
        TOMORROW_BIT -> {
            val triggerInMinutes = timeInMinutes - currentTimeInMinutes + DAY_MINUTES
            return formatDateByMinutes(triggerInMinutes)

        }
        else -> {
            var triggerInMinutes =0
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            for (i in 0..7) {
                val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
                val isCorrectDay = days and 2.0.pow(currentDay).toInt() != 0

                if (isCorrectDay && (timeInMinutes > currentTimeInMinutes || i > 0)) {
                    triggerInMinutes = timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)

                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            return formatDateByMinutes(triggerInMinutes)


        }
    }
}


