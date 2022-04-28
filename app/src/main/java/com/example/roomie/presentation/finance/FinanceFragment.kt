package com.example.roomie.presentation.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.databinding.FragmentFinanceBinding
import com.google.android.material.tabs.TabLayoutMediator

class FinanceFragment : Fragment() {

    private lateinit var binding : FragmentFinanceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFinanceBinding.inflate(inflater, container, false)
        val tabLayout = binding.tablayoutFinance
        val viewPager = binding.viewpagerFinance
        viewPager.adapter = FinancePagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        return binding.root
    }

    private fun getTabTitle(position: Int) : String? {
        return when (position) {
            Constants.FINANCE_OVERVIEW_INDEX      -> getString(R.string.tab_item_finance_overview_label)
            Constants.FINANCE_STATISTICS_INDEX    -> getString(R.string.tab_item_finance_statistics_label)
            Constants.FINANCE_GRAPH_INDEX         -> getString(R.string.tab_item_finance_graph_label)
            Constants.FINANCE_ARCHIVE_INDEX       -> getString(R.string.tab_item_finance_archive_label)
            else                                  -> null
        }
    }
}