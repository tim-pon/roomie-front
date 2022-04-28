package com.example.roomie.presentation.home.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentHomeUserinfoBinding
import com.example.roomie.presentation.CustomSnackbar
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserinfosFragment : Fragment() {

    private lateinit var binding : FragmentHomeUserinfoBinding
    private val viewModel: UserinfosViewModel by viewModels()
    @Inject lateinit var lazyHeader: LazyHeaders

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeUserinfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MaterialDivider to separate content
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = 150
        divider.dividerInsetEnd = 20

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@UserinfosFragment.viewModel
            recyclerViewUserinfos.adapter = initAdapter()
            recyclerViewUserinfos.addItemDecoration(divider)
            swipeRefreshUserinfos.setOnRefreshListener { this@UserinfosFragment.viewModel.refresh(true) }
        }
        viewModel.flatInfo.observe(viewLifecycleOwner){}
    }

    /**
     * Subscribes userinfos livedata to ui
     * @return adapter with list of userinfos sorted by alphabetical order
     */
    private fun initAdapter(): UserinfoAdapter {
        val userinfoAdapter = UserinfoAdapter(requireContext(), lazyHeader)
        viewModel.userinfos.observe(viewLifecycleOwner) {userinfos ->
            userinfoAdapter.submitList(userinfos.data?.sortedBy { it.username})
            binding.swipeRefreshUserinfos.isRefreshing = userinfos.status == Status.LOADING

            if (userinfos.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), userinfos.code)
        }
        return userinfoAdapter
    }
}