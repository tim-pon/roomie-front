package com.example.roomie.presentation.shopping.article_detail

import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import com.example.roomie.R
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentArticleBinding
import com.example.roomie.presentation.CustomSnackbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentArticleBinding
    private val viewModel: ArticleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val header = binding.dialogHeader
        header.btnCancel.setOnClickListener { dismiss() }
        header.btnSave.setOnClickListener { saveArticle() }

        binding.btnCategory.setOnClickListener { view -> categoryPopup(view) }
        binding.btnUnit.setOnClickListener { view -> unitPopup(view) }
        setPopUpButtonText()

    }

    private fun saveArticle() {
        // validate input

        listOf(binding.btnCategory, binding.btnUnit).forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    it.error = if (s.isNullOrBlank()) "Error"
                               else null
                }
            })
        }

        binding.textinputName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.textinputName.error = if (s.isNullOrBlank()) getString(R.string.empty_article_name)
                    else null
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        if (valid()) {
            viewModel.setArticle().observe(viewLifecycleOwner) {
                if (it.status !== Status.LOADING)
                    dismiss()
                if (it.status == Status.ERROR)
                    CustomSnackbar.defaultError(requireView(), it.code)
            }
        }
    }

    private fun valid(): Boolean {
        var valid = true
        if (binding.textinputName.editText?.text.toString().isBlank()) {
            binding.textinputName.error = "Gib eine Bezeichnung ein"
            valid = false
        }
        if (binding.btnCategory.text.toString().isBlank()) {
            binding.btnCategory.error = "Wähle eine Kategorie aus"
            valid = false
        }
        if (binding.btnUnit.text.toString().isBlank()) {
            binding.btnUnit.error = "Wähle eine Einheit aus"
            valid = false
        }

        return valid
    }

    private fun categoryPopup(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.option_menu_category, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when(menuItem.itemId) {
                R.id.menu_item_fruit -> {
                    binding.btnCategory.text = getString(R.string.cat_fruit)
                    viewModel.article.value!!.data!!.category = "FRUIT"
                }
                R.id.menu_item_vegetable -> {
                    binding.btnCategory.text = getString(R.string.cat_vegetable)
                    viewModel.article.value!!.data!!.category = "VEGETABLE"
                }
                R.id.menu_item_drinks -> {
                    binding.btnCategory.text = getString(R.string.cat_drink)
                    viewModel.article.value!!.data!!.category = "DRINK"
                }
                R.id.menu_item_snacks -> {
                    binding.btnCategory.text = getString(R.string.cat_snack)
                    viewModel.article.value!!.data!!.category = "SNACK"
                }
                R.id.menu_item_meat -> {
                    binding.btnCategory.text = getString(R.string.cat_meat)
                    viewModel.article.value!!.data!!.category = "MEAT"
                }
                R.id.menu_item_cheese -> {
                    binding.btnCategory.text = getString(R.string.cat_cheese)
                    viewModel.article.value!!.data!!.category = "CHEESE"
                }
                R.id.menu_item_other -> {
                    binding.btnCategory.text = getString(R.string.cat_other)
                    viewModel.article.value!!.data!!.category = "OTHER"
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popupMenu.show()
    }

    private fun unitPopup(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.option_menu_unit, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when(menuItem.itemId) {
                R.id.menu_item_gramm -> {
                    binding.btnUnit.text = getString(R.string.unit_gram)
                    viewModel.article.value!!.data!!.unit = "GRAMM"
                }
                R.id.menu_item_kilo -> {
                    binding.btnUnit.text = getString(R.string.unit_kilo)
                    viewModel.article.value!!.data!!.unit = "KILOGRAMM"
                }
                R.id.menu_item_litre -> {
                    binding.btnUnit.text = getString(R.string.unit_litre)
                    viewModel.article.value!!.data!!.unit = "LITRE"
                }
                R.id.menu_item_millilitre -> {
                    binding.btnUnit.text = getString(R.string.unit_millilitre)
                    viewModel.article.value!!.data!!.unit = "MILLILITRE"
                }
                R.id.menu_item_milligramm -> {
                    binding.btnUnit.text = getString(R.string.unit_milligram)
                    viewModel.article.value!!.data!!.unit = "MILLIGRAMM"
                }
                R.id.menu_item_piece -> {
                    binding.btnUnit.text = getString(R.string.unit_piece)
                    viewModel.article.value!!.data!!.unit = "PIECE"
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popupMenu.show()
    }

    private fun setPopUpButtonText() {
        viewModel.article.observe(viewLifecycleOwner) { article ->
            if (article.status ==  Status.SUCCESS) {
                binding.btnUnit.text = when (viewModel.article.value!!.data!!.unit!!) {
                    "GRAMM" -> getString(R.string.unit_gram)
                    "KILOGRAMM" -> getString(R.string.unit_kilo)
                    "LITRE" -> getString(R.string.unit_litre)
                    "MILLILITRE" -> getString(R.string.unit_millilitre)
                    "MILLIGRAMM" -> getString(R.string.unit_milligram)
                    "PIECE" -> getString(R.string.unit_piece)
                    else -> ""
                }
                binding.btnCategory.text = when (viewModel.article.value!!.data!!.category!!) {
                    "FRUIT" -> getString(R.string.cat_fruit)
                    "VEGETABLE" -> getString(R.string.cat_vegetable)
                    "DRINK" -> getString(R.string.cat_drink)
                    "SNACK" -> getString(R.string.cat_snack)
                    "MEAT" -> getString(R.string.cat_meat)
                    "CHEESE" -> getString(R.string.cat_cheese)
                    "OTHER" -> getString(R.string.cat_other)
                    else -> ""
                }
            }
            if (article.status == Status.ERROR)
                CustomSnackbar.defaultError(requireView(), article.code)
        }
    }

}