package com.nextserve.trove4k

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.io.File

class WallpaperDetailDialogFragment : DialogFragment() {

    private var wallpaperTitle: String? = null
    private var wallpaperImageUrl: String? = null

    // ActivityResultLauncher for requesting permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, proceed with download
            startDownload()
        } else {
            // Permission denied
            Toast.makeText(context, "Storage permission denied. Cannot download wallpaper.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val ARG_WALLPAPER_TITLE = "wallpaper_title"
        private const val ARG_WALLPAPER_IMAGE_URL = "wallpaper_image_url"

        fun newInstance(title: String, imageUrl: String): WallpaperDetailDialogFragment {
            val fragment = WallpaperDetailDialogFragment()
            val args = Bundle().apply {
                putString(ARG_WALLPAPER_TITLE, title)
                putString(ARG_WALLPAPER_IMAGE_URL, imageUrl)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        arguments?.let {
            wallpaperTitle = it.getString(ARG_WALLPAPER_TITLE)
            wallpaperImageUrl = it.getString(ARG_WALLPAPER_IMAGE_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_wallpaper_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailImageView: ImageView = view.findViewById(R.id.detailImageView)
        val detailTitleTextView: TextView = view.findViewById(R.id.detailTitleTextView)
        val downloadButton: MaterialButton = view.findViewById(R.id.downloadButton)
        val shareButton: MaterialButton = view.findViewById(R.id.shareButton)
        val closeButton: ImageButton = view.findViewById(R.id.closeButton)

        detailTitleTextView.text = wallpaperTitle

        if (!wallpaperImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(wallpaperImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(detailImageView)
        }

        downloadButton.setOnClickListener {
            checkAndRequestPermissionAndDownload()
        }

        shareButton.setOnClickListener {
            if (!wallpaperImageUrl.isNullOrEmpty()) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome wallpaper from Trove 4K!")
                    putExtra(Intent.EXTRA_TEXT, "Get this 4K wallpaper: $wallpaperImageUrl")
                }
                startActivity(Intent.createChooser(shareIntent, "Share Wallpaper"))
            } else {
                Toast.makeText(context, "Cannot share: Image URL not available.", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun checkAndRequestPermissionAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) and above: Scoped Storage.
            // WRITE_EXTERNAL_STORAGE is deprecated. DownloadManager saves to Downloads/Pictures.
            // No explicit runtime permission needed for saving to public directories via DownloadManager.
            startDownload()
        } else {
            // Android 9 (API 28) and below: Need WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, proceed with download
                startDownload()
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun startDownload() {
        if (wallpaperImageUrl.isNullOrEmpty() || wallpaperTitle.isNullOrEmpty()) {
            Toast.makeText(context, "Cannot download: Wallpaper details missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            Toast.makeText(context, "Download service not available.", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "${wallpaperTitle?.replace(" ", "_")}_Trove4K.png" // Sanitize title for filename
        val request = DownloadManager.Request(Uri.parse(wallpaperImageUrl)).apply {
            setTitle(wallpaperTitle)
            setDescription("Downloading wallpaper from Trove 4K")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true) // Allow download over mobile data
            setAllowedOverRoaming(true) // Allow download over roaming

            // Set destination directory
            // For Android 10 (API 29) and above, this saves to /storage/emulated/0/Download/
            // For older Android, it saves to /storage/emulated/0/Download/
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            // Alternatively, for Pictures directory: Environment.DIRECTORY_PICTURES
        }

        try {
            downloadManager.enqueue(request)
            Toast.makeText(context, "Download started: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}