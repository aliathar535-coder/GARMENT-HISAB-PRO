package com.garment.hisabpro

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object PhotoManager {

    /**
     * चुनी हुई फोटो को ऐप के निजी फ़ोल्डर में कॉपी करता है।
     * हिसाब में फोटो का private file path सेव होता है।
     */
    fun copyUrisToPrivateStorage(context: Context, uris: List<Uri>): List<String> {
        val folder = File(context.filesDir, "garment_photos")

        if (!folder.exists() && !folder.mkdirs()) {
            throw IllegalStateException("फोटो फ़ोल्डर नहीं बन पाया")
        }

        val saved = mutableListOf<String>()

        uris.forEach { uri ->
            val mime = context.contentResolver.getType(uri) ?: ""

            if (!mime.startsWith("image/")) return@forEach

            val extension = when (mime.lowercase()) {
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                "image/gif" -> ".gif"
                else -> ".jpg"
            }

            val target = File(
                folder,
                "photo_${System.currentTimeMillis()}_${saved.size}$extension"
            )

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }

                saved.add(target.absolutePath)
            } ?: throw IllegalStateException("चुनी हुई फोटो पढ़ी नहीं जा सकी")
        }

        return saved
    }

    fun deletePhoto(path: String) {
        try {
            File(path).takeIf { it.exists() }?.delete()
        } catch (_: Exception) {
        }
    }
}
