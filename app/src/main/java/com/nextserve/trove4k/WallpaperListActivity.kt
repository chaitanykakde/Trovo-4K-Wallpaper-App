package com.nextserve.trove4k

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Ensure this import is present
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperListActivity : AppCompatActivity() {

    private lateinit var wallpapersRecyclerView: RecyclerView
    private lateinit var wallpaperAdapter: WallpaperAdapter
    private lateinit var wallpaperListToolbar: MaterialToolbar
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView // Corrected variable name from varerrorTextView

    private val wallpapersList = mutableListOf<Wallpaper>()

    // Debounce variables
    private var lastClickTime: Long = 0
    private val CLICK_DEBOUNCE_INTERVAL = 500L // milliseconds (e.g., 0.5 seconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper_list)

        wallpaperListToolbar = findViewById(R.id.wallpaperListToolbar)
        setSupportActionBar(wallpaperListToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        wallpaperListToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorTextView = findViewById(R.id.errorTextView) // Corrected initialization
        wallpapersRecyclerView = findViewById(R.id.wallpapersRecyclerView)

        val layoutManager = GridLayoutManager(this, 2)
        wallpapersRecyclerView.layoutManager = layoutManager

        wallpaperAdapter = WallpaperAdapter(wallpapersList) { wallpaper ->
            // Debounce logic here
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > CLICK_DEBOUNCE_INTERVAL) {
                lastClickTime = currentTime
                WallpaperDetailDialogFragment.newInstance(
                    wallpaper.title,
                    wallpaper.imageUrl
                ).show(supportFragmentManager, "WallpaperDetailDialog")
            }
        }
        wallpapersRecyclerView.adapter = wallpaperAdapter

        val categoryName = intent.getStringExtra("categoryName") ?: "Unknown"
        wallpaperListToolbar.title = "$categoryName Wallpapers"

        fetchWallpaperCountAndImages(categoryName)
    }

    private fun fetchWallpaperCountAndImages(categoryName: String) {
        loadingProgressBar.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        wallpapersRecyclerView.visibility = View.GONE

        wallpapersList.clear()
        wallpaperAdapter.notifyDataSetChanged()

        val database = FirebaseDatabase.getInstance()
        val categoryCountRef = database.getReference("categoryCounts").child(categoryName)

        categoryCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loadingProgressBar.visibility = View.GONE
                val imageCount = snapshot.getValue(Int::class.java)

                if (imageCount == null || imageCount <= 0) {
                    errorTextView.text = "No wallpapers found for '$categoryName' or count is zero."
                    errorTextView.visibility = View.VISIBLE
                    Toast.makeText(this@WallpaperListActivity, "No images found for $categoryName.", Toast.LENGTH_LONG).show()
                    return
                }

                val baseUrl = "https://trove-4k-app.onrender.com/Wallpapers/"
                val imagePrefix = categoryName.first().toLowerCase()

                // lifecycleScope is correctly used here
                lifecycleScope.launch {
                    for (i in 1..imageCount) {
                        val imageUrl = "$baseUrl$categoryName/${imagePrefix}${i}image.png"
                        val wallpaperTitle = "$categoryName Image $i"
                        val newWallpaper = Wallpaper("$categoryName-$i", wallpaperTitle, imageUrl)

                        withContext(Dispatchers.Main) {
                            wallpapersList.add(newWallpaper)
                            wallpaperAdapter.notifyItemInserted(wallpapersList.size - 1)
                            if (wallpapersList.size == 1) {
                                wallpapersRecyclerView.visibility = View.VISIBLE
                            }
                        }
                        delay(50)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingProgressBar.visibility = View.GONE
                errorTextView.text = "Failed to load image count: ${error.message}"
                errorTextView.visibility = View.VISIBLE
                Toast.makeText(this@WallpaperListActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}