package com.nextserve.trove4k

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var errorTextView: TextView

    private val categoriesList = mutableListOf<Category>() // Data source for the adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    Toast.makeText(this, "Search clicked!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_settings -> {
                    Toast.makeText(this, "Settings clicked!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        errorTextView = findViewById(R.id.errorTextView)

        val layoutManager = GridLayoutManager(this, 2)
        categoriesRecyclerView.layoutManager = layoutManager

        // Initialize adapter with an empty mutable list
        categoryAdapter = CategoryAdapter(categoriesList) { category ->
            val intent = Intent(this@HomeActivity, WallpaperListActivity::class.java).apply {
                putExtra("categoryName", category.name)
            }
            startActivity(intent)
        }
        categoriesRecyclerView.adapter = categoryAdapter

        fetchCategoriesFromFirebase()
    }

    private fun fetchCategoriesFromFirebase() {
        errorTextView.visibility = View.GONE
        categoriesRecyclerView.visibility = View.VISIBLE // Ensure RecyclerView is visible

        // Populate with shimmer placeholder items initially
        val numberOfShimmerPlaceholders = 6 // Adjust this number to fill your screen
        val shimmerItems = List(numberOfShimmerPlaceholders) {
            Category(id = "", name = "", imageUrl = "", isShimmerPlaceholder = true)
        }
        categoryAdapter.updateCategories(shimmerItems)


        val database = FirebaseDatabase.getInstance()
        val categoriesRef = database.getReference("categories_data")

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedCategories = mutableListOf<Category>()
                for (categorySnapshot in snapshot.children) {
                    val id = categorySnapshot.key ?: ""
                    val name = categorySnapshot.child("name").getValue(String::class.java)
                    val thumbnailUrl = categorySnapshot.child("thumbnailUrl").getValue(String::class.java)

                    if (name != null && thumbnailUrl != null) {
                        fetchedCategories.add(Category(id, name, thumbnailUrl, isShimmerPlaceholder = false))
                    }
                }

                if (fetchedCategories.isEmpty()) {
                    errorTextView.text = "No categories found."
                    errorTextView.visibility = View.VISIBLE
                    categoriesRecyclerView.visibility = View.GONE
                } else {
                    // Update adapter with real data, replacing shimmer placeholders
                    categoryAdapter.updateCategories(fetchedCategories)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                errorTextView.text = "Failed to load categories: ${error.message}"
                errorTextView.visibility = View.VISIBLE
                categoriesRecyclerView.visibility = View.GONE
                Toast.makeText(this@HomeActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}