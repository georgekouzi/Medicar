package com.simplemobiletools.clock.fragments
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.activities.SimpleActivity
import com.simplemobiletools.clock.adapters.AlarmsAdapter
import com.simplemobiletools.clock.dialogs.ChangeAlarmSortDialog
import com.simplemobiletools.clock.dialogs.EditAlarmDialog
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.interfaces.ToggleAlarmInterface
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_CREATED
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.dialog_edit_alarm.view.*
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class AlarmFragment : Fragment(), ToggleAlarmInterface {




    private var alarms = ArrayList<Alarm>()
    private var currentEditAlarmDialog: EditAlarmDialog? = null

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        activity?.config?.useEnglish =true
        view = inflater.inflate(R.layout.fragment_alarm, container, false) as ViewGroup

        return view
    }

    override fun onResume() {
        super.onResume()
        activity?.config?.useEnglish =true
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#03A9F4")))
        (activity as AppCompatActivity).window.statusBarColor = Color.parseColor("#03A9F4")
        setupViews()
        (view.alarms_list.adapter as AlarmsAdapter)

    }

    override fun onPause() {
        super.onPause()
    }

    fun showSortingDialog() {
        ChangeAlarmSortDialog(activity as SimpleActivity) {
            setupAlarms()
        }
    }


    private fun setupViews() {
        view.apply {
//            requireContext().updateTextColors(alarm_fragment)
            alarm_fab.setOnClickListener {
                val newAlarm = context.createNewAlarm(DEFAULT_ALARM_MINUTES, 0)
                newAlarm.isEnabled = true
                newAlarm.days = getTomorrowBit()
                openEditAlarm(newAlarm)
            }
        }

        setupAlarms()
    }

    private fun setupAlarms() {
        alarms = context?.dbHelper?.getAlarms() ?: return

        when (requireContext().config.alarmSort) {
            SORT_BY_ALARM_TIME -> alarms.sortBy { it.timeInMinutes }
            SORT_BY_DATE_CREATED -> alarms.sortBy { it.id }
            SORT_BY_DATE_AND_TIME -> alarms.sortWith(compareBy<Alarm> {
                requireContext().firstDayOrder(it.days)
            }.thenBy {
                it.timeInMinutes
            })
        }

        if (context?.getNextAlarm()?.isEmpty() == true) {
            alarms.forEach {
                if (it.days == TODAY_BIT && it.isEnabled && it.timeInMinutes <= getCurrentDayMinutes()) {
                    it.isEnabled = false
                    ensureBackgroundThread {
                        context?.dbHelper?.updateAlarmEnabledState(it.id, false)
                    }
                }
            }
        }

        val currAdapter = view.alarms_list.adapter
        if (currAdapter == null) {
            AlarmsAdapter(activity as SimpleActivity, alarms, this, view.alarms_list) {
                openEditAlarm(it as Alarm)
            }.apply {
                view.alarms_list.adapter = this
            }
        } else {
            (currAdapter as AlarmsAdapter).updateItems(alarms)
        }
    }

    private fun openEditAlarm(alarm: Alarm) {
        currentEditAlarmDialog = EditAlarmDialog(activity as SimpleActivity, alarm) {
            alarm.id = it
            currentEditAlarmDialog = null
            setupAlarms()
            checkAlarmState(alarm)
        }
    }

    override fun alarmToggled(id: Int, isEnabled: Boolean) {
        if (requireContext().dbHelper.updateAlarmEnabledState(id, isEnabled)) {
            val alarm = alarms.firstOrNull { it.id == id } ?: return
            alarm.isEnabled = isEnabled
            checkAlarmState(alarm)
        } else {
            requireActivity().toast(R.string.unknown_error_occurred)
        }
    }

    private fun checkAlarmState(alarm: Alarm) {
        if (alarm.isEnabled) {
            context?.scheduleNextAlarm(alarm, true)
        } else {
            context?.cancelAlarmClock(alarm)
        }
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {
        currentEditAlarmDialog?.updateSelectedAlarmSound(alarmSound)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val URI = data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, URI)
                        var saveImageToInternalStorage = saveImageRoInternalStorage(selectedImageBitmap)

                        Log.e("Save image ", "path :: $saveImageToInternalStorage")

//                        imagePill?.setImageBitmap(selectedImageBitmap)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (requestCode == CAMERA) {
                val pic: Bitmap = data!!.extras!!.get("data") as Bitmap
                var saveImageToInternalStorage = saveImageRoInternalStorage(pic)
                Log.e("Save image from camera ", "path :: $saveImageToInternalStorage")
//                imagePill?.setImageBitmap(pic)
                view.vvv.setImageBitmap(pic)


            }
        }


    }

    private fun saveImageRoInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(activity?.applicationContext)
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
