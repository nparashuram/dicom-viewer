package com.nparashuram.dicomviewer.data

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URI
import java.net.URL
import kotlin.random.Random

class PDicomRepo(
    private val okHttpClient: OkHttpClient,
    private val storageFactory: StorageFactory,
    private val imageLoader: ImageLoader,
) {

    /**
     * Parses the processed-dicom folder and gets a map of all saved pDiCom
     */
    fun loadFromDevice(onStatusUpdate: (String) -> Unit): Map<String, String?> {
        return storageFactory.getStorages()
            .associate { storage ->
                onStatusUpdate("Reading ${storage.location}")
                (storage.getIndex().url ?: "") to storage.location.name
            }.filter { it.key != "" }
    }

    /**
     * Load a specific pDiCom file from disk
     */
    fun load(storageLocation: String): PDicomData {
        val storage = storageFactory.get(storageLocation)
        return storage.getIndex()
    }

    suspend fun loadImage(storageLocation: String, file: String, context: Context): ImageBitmap? {
        val storage = storageFactory.get(storageLocation)
        val src = storage.getImageFile(file)
        val imageRequest = ImageRequest.Builder(context)
            .data(src)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()

        val drawable = imageLoader.execute(imageRequest).drawable ?: ShapeDrawable()
        return drawable.toBitmap().asImageBitmap()
    }

    /**
     * Deletes a location
     */
    fun remove(storageLocation: String): Boolean {
        val storage = storageFactory.get(storageLocation)
        return storage.remove()
    }

    /**
     * Downloads the index file, and then each plane and model from network
     */
    fun download(url: String, onStatusUpdate: StatusUpdateFn): String {
        val storage = storageFactory.get(getRandomName())
        val downloader = NetworkDownloader(okHttpClient, storage)
        downloader.fetchPDicomFiles(url, onStatusUpdate)
        return storage.location.name
    }

}

private fun getRandomName(): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z')
    val folderName = (1..20)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
    return folderName
}
