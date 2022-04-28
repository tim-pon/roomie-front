package com.example.roomie.presentation.flat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.databinding.FragmentFlatBinding
import com.example.roomie.presentation.MainActivity
import com.google.android.material.tabs.TabLayoutMediator

class FlatFragment : Fragment() {

    private lateinit var binding: FragmentFlatBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFlatBinding.inflate(inflater, container, false)
        navController = findNavController()
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        viewPager.adapter = FlatPageAdapter(this)

        /**
         *
         */
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Flat"

        /**
         * observe flatid for conditional navigation
         */
        FlatStorage.observe(viewLifecycleOwner, Observer {
            if (FlatStorage.getFlatId() != 0) {
                navController.navigate(R.id.navigation_home)
                MainActivity.navView.visibility = View.VISIBLE
            }
        })
    }

    /**
     * tabs for navigation between join and create flat
     */
    private fun getTabTitle(position: Int) : String? {
        return when (position) {
            Constants.FLAT_JOIN_INDEX    -> getString(R.string.join_flat)
            Constants.FLAT_CREATE_INDEX -> getString(R.string.create_flat)
            else                         -> null
        }
    }
}