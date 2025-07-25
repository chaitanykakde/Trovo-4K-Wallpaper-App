package com.nextserve.trove4k

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class WallpaperAdapter(
    private val wallpapers: List<Wallpaper>,
    private val onItemClick: (Wallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

    inner class WallpaperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wallpaperCard: MaterialCardView = itemView.findViewById(R.id.wallpaperCard)
        val wallpaperImageView: ImageView = itemView.findViewById(R.id.wallpaperImageView)
        val wallpaperTitleTextView: TextView = itemView.findViewById(R.id.wallpaperTitleTextView)

        init {
            wallpaperCard.setOnClickListener {
                onItemClick(wallpapers[adapterPosition])
            }
            wallpaperCard.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).start()
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                        v.performClick()
                    }
                }
                false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)
        return WallpaperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        val wallpaper = wallpapers[position]
        holder.wallpaperTitleTextView.text = wallpaper.title

        Glide.with(holder.itemView.context)
            .load(wallpaper.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .timeout(30000) // Added timeout for 15 seconds
            .into(holder.wallpaperImageView)
    }

    override fun getItemCount(): Int = wallpapers.size
}