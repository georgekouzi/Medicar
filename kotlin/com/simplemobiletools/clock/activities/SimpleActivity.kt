package com.simplemobiletools.clock.activities
import com.simplemobiletools.clock.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity

open class SimpleActivity : BaseSimpleActivity() {


    override fun getAppIconIDs() = arrayListOf(
            R.mipmap.ic_launcher_red,
            R.color.BAC_BLUE_WHITE

    )

    override fun getAppLauncherName() = getString(R.string.app_launcher_name)
}
