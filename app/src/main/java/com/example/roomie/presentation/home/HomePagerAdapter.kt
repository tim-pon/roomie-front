package com.example.roomie.presentation.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.roomie.core.Constants
import com.example.roomie.presentation.home.activity.ActivitiesFragment
import com.example.roomie.presentation.home.userinfo.UserinfosFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        Constants.HOME_OVERVIEW_INDEX to { UserinfosFragment() },
        Constants.HOME_NEWS_INDEX to { ActivitiesFragment() }
    )


    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw  IndexOutOfBoundsException()
    }
}