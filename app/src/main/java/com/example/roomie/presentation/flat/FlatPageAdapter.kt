package com.example.roomie.presentation.flat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.roomie.core.Constants

/**
 * adapter for tab view
 */
class FlatPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        Constants.FLAT_JOIN_INDEX to { JoinFlatFragment() },
        Constants.FLAT_CREATE_INDEX to { CreateFlatFragment() }
    )


    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw  IndexOutOfBoundsException()
    }
}