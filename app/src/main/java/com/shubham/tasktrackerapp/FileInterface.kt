package com.shubham.tasktrackerapp

import android.net.Uri

interface FileInterface {
    fun getFileNameFromUri(uri : Uri) : String
}