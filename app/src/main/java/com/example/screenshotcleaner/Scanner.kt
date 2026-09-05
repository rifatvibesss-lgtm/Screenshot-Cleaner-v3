package com.example.screenshotcleaner

import android.content.ContentResolver
import android.provider.MediaStore
import java.io.BufferedInputStream
import java.security.MessageDigest
import kotlin.math.abs

class Scanner(private val resolver: ContentResolver) {
    fun scanScreenshots(onProgress: (Int) -> Unit = {}): List<Shot> {
        val result = mutableListOf<Shot>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ? OR LOWER(${MediaStore.Images.Media.DISPLAY_NAME}) LIKE ?"
        val args = arrayOf("%Screenshots%", "%screenshot%")
        resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, args,
            "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { c ->
            val iId=c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val iName=c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val iSize=c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val iDate=c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val iPath=c.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            while(c.moveToNext()) {
                val id=c.getLong(iId)
                result += Shot(id, android.content.ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id),
                    c.getString(iName) ?: "Screenshot", c.getLong(iSize), c.getLong(iDate), c.getString(iPath) ?: "")
                if(result.size % 50 == 0) onProgress(result.size)
            }
        }
        return result
    }

    fun exactHash(shot: Shot): String? = runCatching {
        val md=MessageDigest.getInstance("MD5")
        resolver.openInputStream(shot.uri)?.use { input ->
            val b=ByteArray(32*1024); var n=input.read(b)
            while(n>0){ md.update(b,0,n); n=input.read(b) }
        } ?: return null
        md.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    fun visualHash(shot: Shot): Long? = runCatching {
        val opts=android.graphics.BitmapFactory.Options().apply { inSampleSize=8; inPreferredConfig=android.graphics.Bitmap.Config.RGB_565 }
        val bmp=resolver.openInputStream(shot.uri)?.use { android.graphics.BitmapFactory.decodeStream(it,null,opts) } ?: return null
        val w=8; val h=8; val small=android.graphics.Bitmap.createScaledBitmap(bmp,w,h,true)
        var sum=0L; val px=IntArray(64); small.getPixels(px,0,w,0,0,w,h)
        val gray=IntArray(64){ p -> (android.graphics.Color.red(p)*299 + android.graphics.Color.green(p)*587 + android.graphics.Color.blue(p)*114)/1000 }
        gray.forEach{sum+=it}
        val avg=sum/64
        var hash=0L
        gray.forEachIndexed{idx,v -> if(v>=avg) hash=hash or (1L shl idx)}
        bmp.recycle(); if(small!==bmp) small.recycle(); hash
    }.getOrNull()

    fun similarity(a: Long,b: Long): Int = 64- java.lang.Long.bitCount(a xor b)
}
