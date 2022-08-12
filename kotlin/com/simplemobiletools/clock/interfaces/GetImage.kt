package com.simplemobiletools.clock.interfaces
import android.graphics.Bitmap
import android.net.Uri

fun interface GetImage {
    fun result(bipMap: Bitmap, uri: Uri)
}
