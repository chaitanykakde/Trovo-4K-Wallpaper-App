package com.nextserve.trove4k

data class Category(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isShimmerPlaceholder: Boolean = false // New property
)