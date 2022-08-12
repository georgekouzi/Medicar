package com.simplemobiletools.clock.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.*
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.clock.models.Alarm
import com.simplemobiletools.clock.network.ApiManager
import kotlinx.android.synthetic.main.activity_proof_of_taking.*

import kotlinx.coroutines.*
import rx.schedulers.Schedulers.test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProofOfTakingActivity : AppCompatActivity() {
    private var clickButton: FrameLayout? = null
    private var textMessageWhenTheTimeToTakeThePill: TextView? = null
    private var imagePill: AppCompatImageView? = null
    private var tv_namePill: TextView? = null
    private var tv_descriptionPill: TextView? = null//    private var flag :Boolean = false
    private var alarm_id: Int = -1
    private var alarm: Alarm? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proof_of_taking)
        window.statusBarColor = Color.parseColor("#03A9F4")
        alarm_id = intent.getIntExtra(ALARM_ID, -1)

        if (alarm_id != -1) {
            alarm = dbHelper.getAlarmWithId(alarm_id) ?: return
        }

        clickButton = findViewById(R.id.click_fl)
        textMessageWhenTheTimeToTakeThePill = findViewById(R.id.date_pill_take_text)
        imagePill = findViewById(R.id.iv_place_image_detail)
        tv_namePill = findViewById(R.id.name_pill_detail)
        tv_descriptionPill = findViewById(R.id.description_pill_detail)
        setDetail()
        closeReminderAndActivity()
        userBoxCheckProcess()
    }

    private fun userBoxCheckProcess() {

        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        when {
            preferences.getBoolean(PILL_BOX_PROOF, true) && preferences.getBoolean(IMAGE_PROOF, true) -> ifOpenTheBoxAndTakeThePillAndImage()
            preferences.getBoolean(PILL_BOX_PROOF, true) -> ifOpenTheBoxAndTakeThePill()
            preferences.getBoolean(IMAGE_PROOF, true) -> ifTakePhotoFromCamera()
        }


    }

    private fun ifOpenTheBoxAndTakeThePillAndImage() {
        GlobalScope.launch {
            val flag = withContext(Dispatchers.Default) { getResultFromBluetooth() }
            if (flag) {
                runOnUiThread {
                    textMessageWhenTheTimeToTakeThePill?.text = "Please press the red button to take a picture of the pill"
                    clickButton?.setOnClickListener {
                        PhotoFromCamera()
                    }
                }
            }
        }
    }


    private fun ifTakePhotoFromCamera() {
        textMessageWhenTheTimeToTakeThePill?.text = "Please press the red button to take a picture of the pill"
        clickButton?.setOnClickListener {
            PhotoFromCamera()
        }
    }

    private fun ifOpenTheBoxAndTakeThePill() {
        GlobalScope.launch {
            val flag = withContext(Dispatchers.Default) { getResultFromBluetooth() }
            if (flag) {
                runOnUiThread {
                    clickButton?.setBackgroundResource(R.drawable.green_btn_removebg_preview)
                    textMessageWhenTheTimeToTakeThePill?.text = "Please press the green button to finish the process"
                    clickButton?.setOnClickListener {
                        finish()
                    }

                }
            }
        }
    }

    private fun closeReminderAndActivity() {
        updateTimeSnoozeInData(alarm_id, 0)
        ///test snooz time
        getHideAlarmPendingIntent(alarm_id).send()
        alarm?.let { cancelAlarmClock(it) }

    }

    private fun setDetail() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        tv_namePill?.text = "Pill Name :${alarm?.pillName}"
        tv_descriptionPill?.text = "Pill Description : ${alarm?.pillDescription}"
        imagePill?.setImageURI(Uri.parse(alarm?.image))
        imagePill?.scaleType = ImageView.ScaleType.CENTER_CROP
        date_pill_detail.text = "Reminder Date : ${current.format(formatter)}\nReminder time :  " +
            "${getFormattedTime(alarm?.timeInMinutes?.times(60) ?: return, false, true)}" +
            "\nReminder day : ${getAlarmSelectedDaysString(alarm!!.days).split(",")[0]}"

    }


    // sagy and inbal function
    private suspend fun getResultFromBluetooth(): Boolean {

        //TODO -> get the signal from the box ,if the customer take out the pill then change the openBoxPill flag to true:
        // WE USE RETROFIT lib  -> see network package
        // var openBoxPill = ApiManager.create().box().success
        // you need in ApiManager to change the url in function create and the model PillBoxModel var
        // return openBoxPill

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("a")

        when (current.format(formatter)) {
            AM -> {
                while (!ApiManager.create().boxOne().state){
                    delay(100L)
                }
                ApiManager.create().boxOneCloseLed()

            }
            PM -> {
                while (!ApiManager.create().boxTwo().state) {
                    delay(100L)
                }
                ApiManager.create().boxTwoCloseLed()

            }
        }

        return true


    }


    //dolev function
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA) {
            var isPill = false

            // this is the pic for dolev task
            val pic: Bitmap = data!!.extras!!.get("data") as Bitmap
//
            //TODO ->  here you need to writ code to get result from your model and put it in isPill var
            //  ispill = result from your model

//
            //TODO -> if we get true from the model - [ if(ispill) ] we finish the activity -->  closeReminderAndActivity()
            if (true) {
                clickButton?.setBackgroundResource(R.drawable.green_btn_removebg_preview)
                textMessageWhenTheTimeToTakeThePill?.text = "Please press the green button to finish the process"
                clickButton?.setOnClickListener {
                    finish()
                }

            }
            // else we call the function --> ifTakePhotoFromCamera() to restart the proses
            else {
                ifTakePhotoFromCamera()
            }


        }


    }


    private fun PhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val CameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(CameraIntent, CAMERA)
                }


            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermission()

            }
        }).onSameThread().check()


    }


    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this).setMessage("You turned off permission required for this feature. It can be enabled under the Applications setting ")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }

            }.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()

            }.show()

    }

}
