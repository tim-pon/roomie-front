package com.example.roomie.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.R

class SettingsAdapter(private var heading: Array<String>, private var description: Array<String>) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    inner class SettingsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var headline: TextView = view.findViewById<TextView>(R.id.description)
        var description: TextView = view.findViewById<TextView>(R.id.amount)
        var icon: ImageView = view.findViewById<ImageView>(R.id.icon)

        init {
            view.setOnClickListener {
                when (adapterPosition) {
                    0 -> view.findNavController().navigate(R.id.settingUsernameFragment)
                    1 -> view.findNavController().navigate(R.id.settingPasswordFragment)
                    2 -> view.findNavController().navigate(R.id.settingFlatInfoFragment)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_settings, parent, false)
        return SettingsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.headline.text = heading[position]
        holder.description.text = description[position]
        when (position) {
            0 -> holder.icon.setImageResource(R.drawable.ic_user)
            1 -> holder.icon.setImageResource(R.drawable.ic_lock)
            2 -> holder.icon.setImageResource(R.drawable.ic_qr_code)
        }
    }

    override fun getItemCount(): Int {
        //return itemsList.size
        return 3
    }
}