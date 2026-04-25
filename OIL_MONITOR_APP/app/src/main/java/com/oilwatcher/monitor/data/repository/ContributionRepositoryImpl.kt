package com.oilwatcher.monitor.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.oilwatcher.monitor.domain.repository.ContributionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContributionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ContributionRepository {

    override suspend fun uploadContribution(
        stationId: String,
        imageUri: Uri,
        prices: Map<String, Double>
    ): Result<String> {
        return try {
            // 1. Compress Image
            val compressedBytes = compressImage(imageUri)
                ?: return Result.failure(Exception("Failed to compress image."))

            // 2. Upload to Firebase Storage
            val storageRef = storage.reference
            val imagePath = "contributions/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(imagePath)
            
            imageRef.putBytes(compressedBytes).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // 3. Prepare Firestore Contribution Document
            val userId = auth.currentUser?.uid ?: "anonymous"
            val timestamp = FieldValue.serverTimestamp()
            
            val contributionData = hashMapOf(
                "userId" to userId,
                "imageUrl" to downloadUrl,
                "prices" to prices,
                "timestamp" to timestamp,
                "status" to "VERIFIED_OCR" // Assume OCR bypasses manual moderation queue
            )

            // 4. Batch Write (Contribution + Station Update)
            val batch = firestore.batch()
            
            // Add contribution
            val stationRef = firestore.collection("stations").document(stationId)
            val contributionRef = stationRef.collection("contributions").document()
            batch.set(contributionRef, contributionData)
            
            // Update master station document (merge top-level prices)
            val stationUpdate = hashMapOf<String, Any>(
                "lastUpdated" to timestamp
            )
            // Use dot notation matching FuelPrices field names (e.g., 'latestPrices.regular')
            prices.forEach { (fuelName, priceVal) ->
                stationUpdate["latestPrices.${fuelName.lowercase()}"] = priceVal
            }
            
            batch.update(stationRef, stationUpdate)
            
            // Execute batch
            batch.commit().await()
            
            Result.success(contributionRef.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Reads image from URI, scales down if too large, and returns JPEG byte array at 80% quality.
     */
    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Scale rules: max width or height of 1024px
            val maxDim = 1024f
            val ratio = kotlin.math.min(maxDim / originalBitmap.width, maxDim / originalBitmap.height)
            
            val scaledBitmap = if (ratio < 1f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (originalBitmap.width * ratio).toInt(),
                    (originalBitmap.height * ratio).toInt(),
                    true
                )
            } else {
                originalBitmap // Already small enough
            }

            val outputStream = ByteArrayOutputStream()
            // Compress heavily: JPEG 80% yields excellent OCR quality but tiny file size (~100-300kb)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
