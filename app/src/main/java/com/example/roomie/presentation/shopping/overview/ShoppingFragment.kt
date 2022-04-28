package com.example.roomie.presentation.shopping.overview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.databinding.FragmentShoppingBinding
import com.example.roomie.domain.model.ShoppingList
import com.example.roomie.presentation.CustomSnackbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ShoppingFragment : Fragment() {

    private lateinit var binding : FragmentShoppingBinding
    private val viewModel: ShoppingListsViewModel by viewModels()
    private var adapter = ShoppingListAdapter(::updateShoppingList)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ShoppingFragment.viewModel
            recyclerViewShoppingLists.adapter = initAdapter()
            swiperefresh.setOnRefreshListener { this@ShoppingFragment.viewModel.refresh(true)}
            btnAddList.setOnClickListener { createListDialog() }
        }

        val itemTouchHelper = ItemTouchHelper(SwipeToDelete(requireContext(), ::deleteShoppingList, ::createTransaction))
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewShoppingLists)
    }

    /**
     * Subscribes shoppingslists livedata to ui
     * @return adapter with list of shoppinglists
     */
    private fun initAdapter(): ShoppingListAdapter {

        viewModel.shoppingLists.observe(viewLifecycleOwner) { shoppingLists ->
            binding.isEmpty = shoppingLists.data.isNullOrEmpty() && shoppingLists.status !== Status.LOADING
            binding.btnEmpty.setOnClickListener { createListDialog() }
            adapter.submitList(shoppingLists.data)

            binding.swiperefresh.isRefreshing = shoppingLists.status == Status.LOADING
            if (shoppingLists.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), shoppingLists.code)
        }
        return adapter
    }


    private fun createListDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.dialog_add_shopping_list)

        val cancel = bottomSheetDialog.findViewById<ImageButton>(R.id.btn_cancel)!!
        val safe   = bottomSheetDialog.findViewById<MaterialButton>(R.id.btn_save)!!
        val name   = bottomSheetDialog.findViewById<TextInputLayout>(R.id.textinput_name)!!

        cancel.setOnClickListener { bottomSheetDialog.dismiss() }

        safe.setOnClickListener {
            // Response handling
            name.editText?.addTextChangedListener(initTextWatcher(name))

            if (validInput(name)){
                viewModel.createShoppingList(name.editText?.text.toString()).observe(viewLifecycleOwner) {
                    if (it.status !== Status.LOADING)
                        bottomSheetDialog.dismiss()
                    else if (it.status == Status.ERROR)
                        CustomSnackbar.defaultError(requireView(), it.code)
                }
            }

        }

        bottomSheetDialog.show()
    }


    private fun updateShoppingList(shoppingList: ShoppingList) {
        val bottomSheetDialog = BottomSheetDialog(this.requireContext())
        bottomSheetDialog.setContentView(R.layout.dialog_add_shopping_list)

        val cancel = bottomSheetDialog.findViewById<ImageButton>(R.id.btn_cancel)!!
        val safe   = bottomSheetDialog.findViewById<MaterialButton>(R.id.btn_save)!!
        val name   = bottomSheetDialog.findViewById<TextInputLayout>(R.id.textinput_name)!!

        name.editText?.setText(shoppingList.name)
        cancel.setOnClickListener { bottomSheetDialog.dismiss() }

        safe.setOnClickListener {

            name.editText?.addTextChangedListener(initTextWatcher(name))

            if (validInput(name)) {
                shoppingList.name = name.editText?.text.toString()

                // Response handling
                viewModel.setShoppingList(shoppingList).observe(viewLifecycleOwner) {
                    if (it.status !== Status.LOADING)
                        bottomSheetDialog.dismiss()
                    else if (it.status == Status.ERROR)
                        CustomSnackbar.defaultError(requireView(), it.code)
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun initTextWatcher(input: TextInputLayout) =
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, ) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                input.error = if (s.isNullOrBlank()) getString(R.string.empty_shopping_name)
                else null
            }
        }

    private fun validInput(input: TextInputLayout) =
        if (input.editText?.text.toString().isBlank()) {
            input.error = getString(R.string.empty_shopping_name)
            false
        } else true


    private fun deleteShoppingList(adapterPosition: Int) {

        fun delete(shoppingList: ShoppingList) {
            viewModel.deleteShoppingList(shoppingList).observe(viewLifecycleOwner) {
                if (it.status == Status.ERROR)
                    CustomSnackbar.defaultError(requireView(),it.code)
            }
        }

        // get shoppinglist from adapter position
        val shoppingList = adapter.currentList[adapterPosition]

        if (shoppingList.items <= 0)
            delete(shoppingList)
        else
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_delete_title,
                    getString(R.string.dialog_delete_shoppinglist_title)))
                .setMessage(R.string.dialog_delete_shoppinglist_message)
                .setPositiveButton(R.string.dialog_delete_yes) { _, _ -> delete(shoppingList)}
                .setNegativeButton(R.string.dialog_delete_no) { _, _ -> /** handled by onDismiss*/ }
                .setOnDismissListener { adapter.notifyItemChanged(adapterPosition) }
                .show()
    }

    private fun createTransaction(adapterPosition: Int) {

        adapter.notifyItemChanged(adapterPosition)
        // get shoppinglist from adapter position
        val shoppingList = adapter.currentList[adapterPosition]

        val direction = ShoppingFragmentDirections
            .actionNavigationShoppingToTransactionDialog(FlatStorage.getFlatId(), -1, shoppingList.name)
        findNavController().navigate(direction)
    }
}