package com.example.roomie.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.core.Constants.FORCE_FETCH
import com.example.roomie.core.Status
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.data.repository.FlatRepository
import com.example.roomie.databinding.FragmentHomeBinding
import com.example.roomie.presentation.CustomSnackbar
import com.example.roomie.presentation.MainActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    @Inject lateinit var flatRepository: FlatRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val tabLayout = binding.tablayoutHome
        val viewPager = binding.viewpagerHome
        viewPager.adapter = HomePagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        return binding.root
    }

    private fun getTabTitle(position: Int) : String? {
        return when (position) {
            Constants.HOME_OVERVIEW_INDEX -> getString(R.string.tab_item_home_overview_label)
            Constants.HOME_NEWS_INDEX    -> getString(R.string.tab_item_home_news_label)
            else                         -> null
        }
    }

    /**
     * Initial loading of flats and users on app start.
     */
    private fun loadFlatWithUsers() {
        val flatId = FlatStorage.getFlatId()

        flatRepository.getFlatInfo(flatId, FORCE_FETCH).observe(viewLifecycleOwner){
            if (it.status == Status.SUCCESS)
                flatRepository.getUsersByFlatId(flatId, FORCE_FETCH).observe(viewLifecycleOwner){}
        }

        (activity as MainActivity?)?.reloadTrigger = false
    }
}