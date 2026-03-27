package com.genesys.v1.codebase.presenter.components.wallpaperpreview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.genesys.v1.codebase.R
import com.wave.livewallpaper.vfx.VfxParticle

data class TouchVfxOption(
    val id: String,
    val name: String,
    val vfx: VfxParticle,
    val iconRes: Int
)

class TouchVfxAdapter(
    private val options: List<TouchVfxOption>,
    private var selectedId: String,
    private val onSelected: (TouchVfxOption) -> Unit
) : RecyclerView.Adapter<TouchVfxAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.vfxContainer)
        val icon: ImageView = view.findViewById(R.id.ivVfxIcon)
        val name: TextView = view.findViewById(R.id.tvVfxName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_touch_vfx, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.name.text = option.name
        holder.icon.setImageResource(option.iconRes)
        
        val isSelected = option.id == selectedId
        if (isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_play_button) // Use a different color for selected
            holder.icon.setColorFilter(Color.WHITE)
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_circle_button)
            holder.icon.setColorFilter(Color.parseColor("#80FFFFFF")) // Semi-transparent
        }

        holder.itemView.setOnClickListener {
            if (selectedId != option.id) {
                val oldIndex = options.indexOfFirst { it.id == selectedId }
                selectedId = option.id
                notifyItemChanged(oldIndex)
                notifyItemChanged(position)
                onSelected(option)
            }
        }
    }

    override fun getItemCount() = options.size
}
