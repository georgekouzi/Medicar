package com.simplemobiletools.clock.adapters
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.simplemobiletools.clock.fragments.AlarmFragment
import com.simplemobiletools.clock.helpers.TABS_COUNT
import com.simplemobiletools.commons.models.AlarmSound

class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private val fragments = HashMap<Int, Fragment>()
//    private val pillReminder : Fragment

    override fun getItem(position: Int): Fragment {
        Log.d("test12","kkk")

        val fragment = getFragment(position)
        fragments[position] = fragment
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        fragments.remove(position)
        super.destroyItem(container, position, item)
    }

    override fun getCount() = TABS_COUNT

    private fun getFragment(position: Int) =
        when (position) {
        0 -> AlarmFragment()
        else -> throw RuntimeException("Trying to fetch unknown fragment id $position")
    }

    fun showAlarmSortDialog() {
        Log.d("test11", ((fragments[0] as? AlarmFragment).toString()))
        (fragments[0] as? AlarmFragment)?.showSortingDialog()
    }


    fun updateAlarmTabAlarmSound(alarmSound: AlarmSound) {
        (fragments[0] as? AlarmFragment)?.updateAlarmSound(alarmSound)
    }


}
