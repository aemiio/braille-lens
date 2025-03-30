package com.aemiio.braillelens.services

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.aemiio.braillelens.BuildConfig
import com.aemiio.braillelens.ui.screens.DetectedBox
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.toString

object SupabaseService {
    private const val TAG = "SupabaseService"

    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY

    // Bucket name for image storage
    private const val IMAGES_BUCKET = "annotations"

    // Supabase client
    private val supabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_API_KEY
    ) {
        install(Postgrest)
        install(Storage)

    }

    /**
     * Uploads an image to Supabase Storage
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Create a unique filename
                val fileName = "braille_image_${UUID.randomUUID()}.jpg"

                // Get input stream from URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext Result.failure(Exception("Failed to open image"))

                // Read bytes from input stream
                val bytes = inputStream.use { it.readBytes() }

                // Upload to Supabase Storage - correct method signature
                supabaseClient.storage.from(IMAGES_BUCKET)
                    .upload(path = fileName, data = bytes, upsert = true)

                // Get the public URL
                val publicUrl = supabaseClient.storage.from(IMAGES_BUCKET)
                    .publicUrl(fileName)

                Log.d(TAG, "Image uploaded successfully: $publicUrl")
                Result.success(publicUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Uploads a bitmap image to Supabase Storage
     */
    suspend fun uploadBitmap(bitmap: Bitmap): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Create a unique filename
                val fileName = "braille_image_${UUID.randomUUID()}.jpg"

                // Convert bitmap to byte array
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                val bytes = outputStream.toByteArray()

                // Upload to Supabase Storage with upsert enabled
                supabaseClient.storage.from(IMAGES_BUCKET)
                    .upload(
                        path = fileName, 
                        data = bytes,
                        upsert = true  // Enable upsert to override RLS policy
                    )

                // Get the public URL
                val publicUrl = supabaseClient.storage.from(IMAGES_BUCKET)
                    .publicUrl(fileName)

                Log.d(TAG, "Image uploaded successfully: $publicUrl")
                Result.success(publicUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading bitmap", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Saves annotation boxes to Supabase
     */
    suspend fun saveAnnotations(
        context: Context,
        boxes: List<DetectedBox>,
        imagePath: String,
        bitmap: Bitmap? = null,
        grade: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Handle image upload (existing code stays the same)
                val finalImagePath = if (imagePath.startsWith("http")) {
                    imagePath
                } else {
                    val uploadResult = if (bitmap != null) {
                        uploadBitmap(bitmap)
                    } else if (imagePath.startsWith("file:") || imagePath.startsWith("content:")) {
                        uploadImage(context, Uri.parse(imagePath))
                    } else {
                        return@withContext Result.success(imagePath)
                    }

                    uploadResult.getOrElse {
                        return@withContext Result.failure(it)
                    }
                }


                // Set grade flags based on selected grade
                val isGrade1 = grade == "1" || grade == "3"
                val isGrade2 = grade == "2" || grade == "3"

                // Enhanced boxes JSON with class_ids properly formatted
                val boxesJson = buildJsonArray {
                    boxes.forEach { box ->
                        add(buildJsonObject {
                            put("x", box.x)
                            put("y", box.y)
                            put("width", box.width)
                            put("height", box.height)
                            put("className", JsonPrimitive(box.className))

                        })
                    }
                }


                // Updated annotation data object
                val data = buildJsonObject {

                    put("image_path", finalImagePath)
                    put("boxes", boxesJson)
                    put("grade1", isGrade1)
                    put("grade2", isGrade2)
                }


                supabaseClient.postgrest.from("annotations").insert(data)

                Result.success("Annotations saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving annotations", e)
                Result.failure(e)
            }
        }
    }

}