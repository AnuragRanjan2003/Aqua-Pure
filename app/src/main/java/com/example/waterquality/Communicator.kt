package com.example.waterquality

import android.net.Uri

interface Communicator {

    fun passUri(url: String,uri: Uri)

    fun passBackWithUrl(url : String)
}