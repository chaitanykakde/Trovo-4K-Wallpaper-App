package com.nextserve.trove4k

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SHIMMER = 0
        private const val VIEW_TYPE_CATEGORY = 1
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryCard: MaterialCardView = itemView.findViewById(R.id.categoryCard)
        val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImageView)
        val categoryTitleTextView: TextView = itemView.findViewById(R.id.categoryTitleTextView)
        val gradientOverlay: View = itemView.findViewById(R.id.gradientOverlay)

        init {
            categoryCard.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(categories[adapterPosition])
                }
            }
            categoryCard.setOnTouchListener { v, event ->
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

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerViewContainer: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
    }

    override fun getItemViewType(position: Int): Int {
        return if (categories[position].isShimmerPlaceholder) VIEW_TYPE_SHIMMER else VIEW_TYPE_CATEGORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_shimmer, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            CategoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_CATEGORY) {
            val categoryHolder = holder as CategoryViewHolder
            val category = categories[position]

            categoryHolder.categoryTitleTextView.text = category.name
            categoryHolder.gradientOverlay.visibility = View.VISIBLE

            Glide.with(categoryHolder.itemView.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .timeout(15000) // Added timeout for 15 seconds
                .into(categoryHolder.categoryImageView)

            categoryHolder.categoryCard.isClickable = true
            categoryHolder.categoryCard.isFocusable = true
        } else if (holder.itemViewType == VIEW_TYPE_SHIMMER) {
            val shimmerHolder = holder as ShimmerViewHolder
            shimmerHolder.shimmerViewContainer.startShimmer()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == VIEW_TYPE_SHIMMER) {
            (holder as ShimmerViewHolder).shimmerViewContainer.stopShimmer()
        }
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }
}