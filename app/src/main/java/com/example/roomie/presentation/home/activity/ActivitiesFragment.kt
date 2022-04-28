package com.example.roomie.presentation.home.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentHomeActivityBinding
import com.example.roomie.presentation.CustomSnackbar
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivitiesFragment : Fragment() {

    private lateinit var binding : FragmentHomeActivityBinding
    private val viewModel: ActivitiesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MaterialDivider to separate content
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = 80
        divider.dividerInsetEnd = 50

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ActivitiesFragment.viewModel
            recyclerViewActivities.adapter = initAdapter()
            recyclerViewActivities.addItemDecoration(divider)
            swipeRefreshActivities.setOnRefreshListener { this@ActivitiesFragment.viewModel.refresh(true) }
            this@ActivitiesFragment.viewModel.refresh(true)
        }
    }

    /**
     * Subscribes activities livedata to ui
     * @return adapter with list of activities sorted by descending time
     */
    private fun initAdapter(): ActivityAdapter {
        val activityAdapter = ActivityAdapter(
            requireContext()
        )
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            activityAdapter.submitList(activities.data?.sortedByDescending { it.createdOn })
            binding.swipeRefreshActivities.isRefreshing = activities.status == Status.LOADING

            if (activities.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), activities.code)
        }
        return activityAdapter
    }
}