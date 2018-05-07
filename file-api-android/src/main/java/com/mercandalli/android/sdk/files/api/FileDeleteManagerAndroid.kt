package com.mercandalli.android.sdk.files.api

import com.mercandalli.sdk.files.api.FileDeleteManager

class FileDeleteManagerAndroid(
        private val mediaScanner: MediaScanner
) : FileDeleteManager {

    override fun delete(path: String) {
        val ioFile = java.io.File(path)
        ioFile.delete()
        mediaScanner.refresh(path)
    }
}