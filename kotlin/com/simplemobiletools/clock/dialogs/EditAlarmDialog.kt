package com.simplemobiletools.clock.dialogs
import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.PillImage
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.SelectAlarmSoundDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.dialog_edit_alarm.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class EditAlarmDialog(val activity: SimpleActivity, val alarm: Alarm, val callback: (alarmId: Int) -> Unit) : View.OnClickListener {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_edit_alarm, null)
    private val textColor = activity.getProperTextColor()


    init {
        myActivity = activity
        myView = view
        myAlarm = alarm

        restoreLastAlarm()
        updateAlarmTime()

        view.apply {
            edit_alarm_time.setOnClickListener {
                TimePickerDialog(
                    context,
                    R.style.TimePickerTheme,
                    timeSetListener,
                    alarm.timeInMinutes / 60,
                    alarm.timeInMinutes % 60,
                    DateFormat.is24HourFormat(activity)
                ).show()
            }

//            edit_alarm_sound.colorCompoundDrawable(textColor)
            edit_alarm_sound.text = alarm.soundTitle


            edit_alarm_sound.setOnClickListener {
                SelectAlarmSoundDialog(activity, alarm.soundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID, RingtoneManager.TYPE_ALARM, true,
                    onAlarmPicked = {
                        if (it != null) {
                            updateSelectedAlarmSound(it)
                        }
                    }, onAlarmSoundDeleted = {
                        if (alarm.soundUri == it.uri) {
                            val defaultAlarm = context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
                            updateSelectedAlarmSound(defaultAlarm)
                        }
                        activity.checkAlarmsWithDeletedSoundUri(it.uri)
                    })
            }

//            edit_alarm_vibrate_icon.setColorFilter(textColor)
            edit_alarm_vibrate.isChecked = alarm.vibrate
            edit_alarm_vibrate_holder.setOnClickListener {
                edit_alarm_vibrate.toggle()
                alarm.vibrate = edit_alarm_vibrate.isChecked
            }


            val dayLetters = activity.resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
            val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
                dayIndexes.moveLastItemToFront()


            dayIndexes.forEach {

                val pow = Math.pow(2.0, it.toDouble()).toInt()
                val day = activity.layoutInflater.inflate(R.layout.alarm_day, edit_alarm_days_holder, false) as TextView
                day.text = dayLetters[it]

                val isDayChecked = alarm.days > 0 && alarm.days and pow != 0
                day.background = getProperDayDrawable(isDayChecked)

                day.setTextColor(Color.parseColor("#FF000000"))

                day.setOnClickListener {
                    if (alarm.days < 0) {
                        alarm.days = 0
                    }

                    val selectDay = alarm.days and pow == 0
                    if (selectDay) {
                        alarm.days = alarm.days.addBit(pow)
                    } else {
                        alarm.days = alarm.days.removeBit(pow)
                    }
                    day.background = getProperDayDrawable(selectDay)
                    day.setTextColor(Color.parseColor("#FF000000"))
                    checkDaylessAlarm()
                }

                edit_alarm_days_holder.addView(day)
            }
            vvv.setOnClickListener(this@EditAlarmDialog)


        }

        AlertDialog.Builder(activity,R.style.AlertDialog)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {

//                this.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#03A9F4")))
setOnShowListener (DialogInterface.OnShowListener {
    getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#03A9F4"))
    getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#03A9F4"))

})



                activity.setupDialogStuff(view, this) {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (!activity.config.wasAlarmWarningShown) {
                            ConfirmationDialog(activity, messageId = R.string.alarm_warning, positive = R.string.ok, negative = 0) {
                                activity.config.wasAlarmWarningShown = true
                                it.performClick()
                            }

                            return@setOnClickListener
                        }

                        if (alarm.days <= 0) {
                            alarm.days = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                                TODAY_BIT
                            } else {
                                TOMORROW_BIT
                            }
                        }

                        alarm.pillName = view.edit_pill_name.value
                        alarm.pillDescription = view.edit_pill_description.value

                        alarm.isEnabled = true

                        alarm.date = getDate(alarm.days, alarm.timeInMinutes)


                        var alarmId = alarm.id
                        if (alarm.id == 0) {
                            alarmId = activity.dbHelper.insertAlarm(alarm)
                            if (alarmId == -1) {
                                activity.toast(R.string.unknown_error_occurred)
                            }

                        } else {
                            if (!activity.dbHelper.updateAlarm(alarm)) {
                                activity.toast(R.string.unknown_error_occurred)
                            }

                        }

                        activity.config.alarmLastConfig = alarm
                        callback(alarmId)
                        dismiss()
                    }
                }
            }
    }

    private fun restoreLastAlarm() {
        if (alarm.id == 0) {
            activity.config.alarmLastConfig?.let { lastConfig ->
                alarm.pillName = lastConfig.pillName
                alarm.days = lastConfig.days
                alarm.soundTitle = lastConfig.soundTitle
                alarm.soundUri = lastConfig.soundUri
                alarm.timeInMinutes = lastConfig.timeInMinutes
                alarm.vibrate = lastConfig.vibrate
            }
        }
    }

    private val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        alarm.timeInMinutes = hourOfDay * 60 + minute
        updateAlarmTime()
    }

    private fun updateAlarmTime() {
        view.edit_alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)
        checkDaylessAlarm()
    }

    private fun checkDaylessAlarm() {
        if (alarm.days <= 0) {
            val textId = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                R.string.today
            } else {
                R.string.tomorrow
            }

            view.edit_alarm_dayless_label.text = "(${activity.getString(textId)})"
        }
        view.edit_alarm_dayless_label.beVisibleIf(alarm.days <= 0)
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = activity.resources.getDrawable(drawableId)
//        drawable.applyColorFilter(textColor)
        return drawable
    }

    fun updateSelectedAlarmSound(alarmSound: AlarmSound) {
        alarm.soundTitle = alarmSound.title
        alarm.soundUri = alarmSound.uri
        view.edit_alarm_sound.text = alarmSound.title
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.vvv -> {
                val pictureDialog = AlertDialog.Builder(activity)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems =
                    arrayOf("Select photo from Gallery", "Capture photo from camera")

                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> {
                            Intent(activity, PillImage::class.java).apply {
                                putExtra(GALLERY_OR_CAMERA, GALLERY)
                                activity.startActivity(this)
                            }

                        }

                        1 -> {
                            Intent(activity, PillImage::class.java).apply {
                                putExtra(GALLERY_OR_CAMERA, CAMERA)
                                activity.startActivity(this)
                            }
                        }
                    }


                }.show()

            }

        }
    }


    fun photoFromCamera() {
        Dexter.withContext(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val CameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    activity.startActivityForResult(CameraIntent, CAMERA)
                }


            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermission()

            }
        }).onSameThread().check()
    }

    fun choosePhotoFromGallery() {
        Dexter.withContext(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activity.startActivityForResult(galleryIntent, GALLERY)
                }

            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermission()

            }
        }).onSameThread().check()


    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(activity).setMessage("You turned off permission required for this feature. It can be enabled under the Applications setting ")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }

            }.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()

            }.show()

    }


    companion object {
        var myActivity: SimpleActivity? = null
        var myView: View? = null
        var myAlarm: com.simplemobiletools.clock.models.Alarm? = null

        fun activityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GALLERY) {
                    if (data != null) {
                        val URI = data.data
                        try {
                            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(myActivity?.contentResolver, URI)
                            var saveImageToInternalStorage = saveImageRoInternalStorage(selectedImageBitmap)
                            myView?.vvv?.setImageBitmap(selectedImageBitmap)
                            myAlarm?.image = saveImageToInternalStorage.toString()

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } else if (requestCode == CAMERA) {
                    val pic: Bitmap = data!!.extras!!.get("data") as Bitmap
                    var saveImageToInternalStorage = saveImageRoInternalStorage(pic)
                    myAlarm?.image = saveImageToInternalStorage.toString()
                    myView?.vvv?.setImageBitmap(pic)


                }
            }


        }

        fun saveImageRoInternalStorage(bitmap: Bitmap): Uri {
            val wrapper = ContextWrapper(myActivity?.applicationContext)
            var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
            file = File(file, "${UUID.randomUUID()}.jpg")

            try {
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
            return Uri.parse(file.absolutePath)


        }


    }


}


