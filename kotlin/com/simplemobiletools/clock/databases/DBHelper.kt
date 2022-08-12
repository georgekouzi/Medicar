package com.simplemobiletools.clock.databases
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.simplemobiletools.clock.extensions.cancelAlarmClock
import com.simplemobiletools.clock.extensions.createNewAlarm
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getStringValue
import com.simplemobiletools.commons.helpers.*
import java.util.*

class DBHelper private constructor(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val ALARMS_TABLE_NAME = "contacts"  // wrong table name, ignore it
    private val COL_ID = "id"
    private val COL_TIME_IN_MINUTES = "time_in_minutes"
    private val COL_DAYS = "days"
    private val COL_IS_ENABLED = "is_enabled"
    private val COL_VIBRATE = "vibrate"
    private val COL_SOUND_TITLE = "sound_title"
    private val COL_SOUND_URI = "sound_uri"
    private val COL_PILL_NAME = "pill_name"
    private val COL_DATE = "date"
    private val COL_PILL_DESCRIPTION = "pill_description"
    private val COL_IMAGE = "image"
    private val COL_SNOOZE = "snoozeTime"


    private val mDb = writableDatabase

    companion object {
        private const val DB_VERSION = 1
        const val DB_NAME = "alarms.db"
        var dbInstance: DBHelper? = null

        fun newInstance(context: Context): DBHelper {
            if (dbInstance == null)
                dbInstance = DBHelper(context)

            return dbInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $ALARMS_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_TIME_IN_MINUTES INTEGER, $COL_DAYS INTEGER, " +
                "$COL_IS_ENABLED INTEGER, $COL_VIBRATE INTEGER, $COL_SOUND_TITLE TEXT, $COL_SOUND_URI TEXT, $COL_PILL_NAME TEXT, $COL_PILL_DESCRIPTION TEXT, $COL_IMAGE TEXT, $COL_DATE TEXT, $COL_SNOOZE INTEGER)"
        )
        insertInitialAlarms(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun insertInitialAlarms(db: SQLiteDatabase) {
        val weekDays = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT
        val weekDaysAlarm = context.createNewAlarm(420, weekDays)
        insertAlarm(weekDaysAlarm, db)

        val weekEnd = SATURDAY_BIT or SUNDAY_BIT
        val weekEndAlarm = context.createNewAlarm(540, weekEnd)
        insertAlarm(weekEndAlarm, db)
    }

    fun insertAlarm(alarm: com.simplemobiletools.clock.models.Alarm, db: SQLiteDatabase = mDb): Int {
        val values = fillAlarmContentValues(alarm)
        return db.insert(ALARMS_TABLE_NAME, null, values).toInt()
    }

    fun updateAlarm(alarm: com.simplemobiletools.clock.models.Alarm): Boolean {
        val selectionArgs = arrayOf(alarm.id.toString())
        val values = fillAlarmContentValues(alarm)
        val selection = "$COL_ID = ?"
        return mDb.update(ALARMS_TABLE_NAME, values, selection, selectionArgs) == 1
    }

    fun updateAlarmEnabledState(id: Int, isEnabled: Boolean): Boolean {
        val selectionArgs = arrayOf(id.toString())
        val values = ContentValues()
        values.put(COL_IS_ENABLED, isEnabled)
        val selection = "$COL_ID = ?"
        return mDb.update(ALARMS_TABLE_NAME, values, selection, selectionArgs) == 1
    }

    fun deleteAlarms(alarms: ArrayList<com.simplemobiletools.clock.models.Alarm>) {
        alarms.filter { it.isEnabled }.forEach {
            context.cancelAlarmClock(it)
        }

        val args = TextUtils.join(", ", alarms.map { it.id.toString() })
        val selection = "$ALARMS_TABLE_NAME.$COL_ID IN ($args)"
        mDb.delete(ALARMS_TABLE_NAME, selection, null)
    }

    fun getAlarmWithId(id: Int) = getAlarms().firstOrNull { it.id == id }

    fun getAlarmsWithUri(uri: String) = getAlarms().filter { it.soundUri == uri }

    private fun fillAlarmContentValues(alarm: com.simplemobiletools.clock.models.Alarm): ContentValues {
        return ContentValues().apply {
            put(COL_TIME_IN_MINUTES, alarm.timeInMinutes)
            put(COL_DAYS, alarm.days)
            put(COL_IS_ENABLED, alarm.isEnabled)
            put(COL_VIBRATE, alarm.vibrate)
            put(COL_SOUND_TITLE, alarm.soundTitle)
            put(COL_SOUND_URI, alarm.soundUri)
            put(COL_PILL_NAME, alarm.pillName)
            put(COL_PILL_DESCRIPTION, alarm.pillDescription)
            put(COL_IMAGE, alarm.image)
            put(COL_DATE, alarm.date)
            put(COL_SNOOZE, alarm.snoozeTime)

        }
    }

    fun getEnabledAlarms() = getAlarms().filter { it.isEnabled }

    fun getAlarms(): ArrayList<com.simplemobiletools.clock.models.Alarm> {
        val alarms = ArrayList<com.simplemobiletools.clock.models.Alarm>()
        val cols = arrayOf(
            COL_ID,
            COL_TIME_IN_MINUTES,
            COL_DAYS,
            COL_IS_ENABLED,
            COL_VIBRATE,
            COL_SOUND_TITLE,
            COL_SOUND_URI,
            COL_PILL_NAME,
            COL_PILL_DESCRIPTION,
            COL_IMAGE,
            COL_DATE,
            COL_SNOOZE
        )
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(ALARMS_TABLE_NAME, cols, null, null, null, null, null)
            if (cursor?.moveToFirst() == true) {
                do {
                    try {
                        val id = cursor.getIntValue(COL_ID)
                        val timeInMinutes = cursor.getIntValue(COL_TIME_IN_MINUTES)
                        val days = cursor.getIntValue(COL_DAYS)
                        val isEnabled = cursor.getIntValue(COL_IS_ENABLED) == 1
                        val vibrate = cursor.getIntValue(COL_VIBRATE) == 1
                        val soundTitle = cursor.getStringValue(COL_SOUND_TITLE)
                        val soundUri = cursor.getStringValue(COL_SOUND_URI)
                        val pillName = cursor.getStringValue(COL_PILL_NAME)
                        val pillDescription = cursor.getStringValue(COL_PILL_DESCRIPTION)
                        val image = cursor.getStringValue(COL_IMAGE)
                        val date = cursor.getStringValue(COL_DATE)
                        val snoozeTime = cursor.getIntValue(COL_SNOOZE)

                        val alarm = com.simplemobiletools.clock.models.Alarm(
                            id,
                            timeInMinutes,
                            days,
                            isEnabled,
                            vibrate,
                            soundTitle,
                            soundUri,
                            pillName,
                            pillDescription,
                            image,
                            date,
                            snoozeTime
                        )
                        alarms.add(alarm)
                    } catch (e: Exception) {
                        continue
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        return alarms
    }

}
