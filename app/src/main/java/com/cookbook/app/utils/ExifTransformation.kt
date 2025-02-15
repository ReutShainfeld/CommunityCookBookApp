package com.cookbook.app.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import com.squareup.picasso.Transformation
import java.io.IOException
import java.io.InputStream
import java.net.URL

class ExifTransformation(private val imageUrl: String) : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        try {
            val inputStream: InputStream = URL(imageUrl).openStream()
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return source
            }

            val rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            if (rotatedBitmap != source) {
                source.recycle()
            }
            return rotatedBitmap

        } catch (e: IOException) {
            e.printStackTrace()
            return source
        }
    }

    override fun key(): String {
        return "exifTransformation"
    }
}
