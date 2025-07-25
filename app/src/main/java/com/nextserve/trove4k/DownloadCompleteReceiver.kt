package com.nextserve.trove4k

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId != -1L) {
                // You could query the DownloadManager here to get more details
                // about the download (e.g., file path, status).
                // For simplicity, we'll just show a generic "Download Complete" toast.

                val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager?.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download complete!", Toast.LENGTH_SHORT).show()
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(reasonIndex)
                        Toast.makeText(context, "Download failed! Reason: $reason", Toast.LENGTH_LONG).show()
                    }
                    cursor.close()
                } else {
                    Toast.makeText(context, "Download complete (ID: $downloadId)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}