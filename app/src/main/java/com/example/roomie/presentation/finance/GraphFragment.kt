package com.example.roomie.presentation.finance

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.roomie.R
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.Status
import com.example.roomie.databinding.FragmentFinanceGraphBinding
import com.example.roomie.domain.model.MonthlyExpense
import com.example.roomie.presentation.CustomSnackbar


import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GraphFragment : Fragment() {

    private lateinit var binding : FragmentFinanceGraphBinding
    private val viewModel: GraphViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFinanceGraphBinding.inflate(inflater, container, false)

        val flatId = FlatStorage.getFlatId() ?: return binding.root

        viewModel.getMonthlyExpenses(flatId).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    this.createGraph(it.data!!)
                }
                Status.ERROR -> {
                    CustomSnackbar.showSnackbar(
                        binding.root,
                        getString(R.string.error_smt_wrong_html_code, it.code),
                        CustomSnackbar.SnackbarTime.LONG,
                        CustomSnackbar.SnackbarType.ERROR,
                        CustomSnackbar.SnackbarLayoutType.WITHOUT_BottomNavigationView
                    )
                }
                else -> {}
            }

        }
        return binding.root
    }

    private fun createGraph(expenses: List<MonthlyExpense>) {
        val barChart = binding.barChart
        val textColorPrimary = requireContext().getColorThemeRes(android.R.attr.textColorPrimary)

        barChart.setNoDataText(getString(R.string.graph_empty_text))
        barChart.setNoDataTextColor(textColorPrimary)


        if (expenses.isEmpty()) return

        val entries = expenses.mapIndexed { i, e ->
            BarEntry(i.toFloat(), e.amount)
        }

        val labels = expenses.map { e ->
            e.month
        }

        val primaryColor = requireContext().getColorThemeRes(android.R.attr.colorPrimary)

        val set = BarDataSet(entries, getString(R.string.graph_legend))
        val data = BarData(set)
        set.setDrawValues(true)
        set.valueTextSize = 12f
        set.color = primaryColor
        set.valueTextColor = textColorPrimary

        val formatter = object: ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return labels[value.toInt()]
            }
        }

        val xAxis = barChart.xAxis
        xAxis.labelRotationAngle = 0f
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.textColor = textColorPrimary

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.axisMinimum = 0f
        leftAxis.textColor = textColorPrimary
        leftAxis.gridColor = textColorPrimary

        val rightAxis = barChart.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = 0f
        rightAxis.textColor = textColorPrimary
        rightAxis.gridColor = textColorPrimary

        val legend = barChart.legend
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 12f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.textColor = textColorPrimary

        barChart.setTouchEnabled(true)

        barChart.description = null

        barChart.animateY(1000)

        barChart.data = data
        barChart.invalidate()
        val maxVisible = 6f
        barChart.setVisibleXRangeMaximum(maxVisible)
        if (expenses.size > 6) barChart.moveViewToX(expenses.size - maxVisible)

    }

    @ColorInt
    fun Context.getColorThemeRes(@AttrRes id: Int): Int {
        val resolvedAttr = TypedValue()
        this.theme.resolveAttribute(id, resolvedAttr, true)
        return this.getColor(resolvedAttr.resourceId)
    }
}