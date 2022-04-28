package com.example.roomie.presentation.finance

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.roomie.core.Constants
import com.example.roomie.presentation.finance.archive.ArchiveFragment
import com.example.roomie.presentation.finance.overview.OverviewFragment
import com.example.roomie.presentation.finance.statistics.StatisticsFragment


class FinancePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        Constants.FINANCE_OVERVIEW_INDEX      to { OverviewFragment() },
        Constants.FINANCE_STATISTICS_INDEX    to { StatisticsFragment() },
        Constants.FINANCE_GRAPH_INDEX         to { GraphFragment() },
        Constants.FINANCE_ARCHIVE_INDEX       to { ArchiveFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw  IndexOutOfBoundsException()
    }
}