package com.simplemobiletools.clock.activities
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.adapters.ViewPagerAdapter
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.extensions.getNextAlarm
import com.simplemobiletools.clock.extensions.rescheduleEnabledAlarms
import com.simplemobiletools.clock.helpers.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setting()
        setContentView(R.layout.activity_main)
        initFragments()

        if (getNextAlarm().isEmpty()) {
            ensureBackgroundThread {
                rescheduleEnabledAlarms()
            }
        }
    }

    private fun setting() {

        config.useEnglish = true
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#03A9F4")))
        window.statusBarColor = Color.parseColor("#03A9F4")
        checkForLoginScreen()
        Permissions()

    }


    private fun checkForLoginScreen() {
        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        val firstTime = preferences.getBoolean(FIRST_TIME, true)
        if (firstTime) {
            Intent(this, RegisterActivity::class.java).apply {
                startActivity(this)
            }

        }
    }


    override fun onResume() {
        super.onResume()

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }

    override fun onPause() {
        super.onPause()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        config.lastUsedViewPagerPage = view_pager.currentItem
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //toolbar option
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.emergency -> launchEmergencyCall()

            R.id.profile -> launchProfile()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun launchEmergencyCall() {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "101"))
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.extras?.containsKey(OPEN_TAB) == true) {
            val tabToOpen = intent.getIntExtra(OPEN_TAB, TAB_CLOCK)
            view_pager.setCurrentItem(tabToOpen, false)
        }
        super.onNewIntent(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_AUDIO_FILE_INTENT_ID && resultCode == RESULT_OK && resultData != null) {
            storeNewAlarmSound(resultData)
        }
    }

    private fun storeNewAlarmSound(resultData: Intent) {
        val newAlarmSound = storeNewYourAlarmSound(resultData)
        getViewPagerAdapter()?.updateAlarmTabAlarmSound(newAlarmSound)
    }


    private fun getViewPagerAdapter() = view_pager.adapter as? ViewPagerAdapter

    private fun initFragments() {
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        view_pager.adapter = viewPagerAdapter

        val tabToOpen = intent.getIntExtra(OPEN_TAB, config.lastUsedViewPagerPage)
        intent.removeExtra(OPEN_TAB)

        view_pager.offscreenPageLimit = TABS_COUNT - 1
        view_pager.currentItem = tabToOpen
    }


    private fun launchProfile() {
        startActivity(Intent(applicationContext, UserProfileActivity::class.java))
    }

    fun Permissions() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.INTERNET
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

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
