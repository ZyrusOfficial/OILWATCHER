package com.oilwatcher.monitor.presentation.native_screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.oilwatcher.monitor.domain.model.FuelType
import com.oilwatcher.monitor.presentation.theme.OilWatcherColors
import androidx.compose.ui.graphics.toArgb

/**
 * Utility to generate custom bitmap descriptors for Map Markers.
 */
object MarkerUtil {

    /**
     * Creates a rounded rectangle bitmap with the price text and a small tail pointing down.
     */
    fun createPriceMarkerBitmap(
        context: Context,
        price: String,
        fuelType: FuelType? = FuelType.REGULAR,
        isSelected: Boolean = false
    ): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val width = (60 * density).toInt()
        val height = (40 * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Colors
        val bgColor = if (isSelected) {
            OilWatcherColors.PrimaryContainer.toArgb()
        } else {
            ContextCompat.getColor(context, android.R.color.white)
        }
        
        val textColor = if (isSelected) {
            ContextCompat.getColor(context, android.R.color.white)
        } else {
            OilWatcherColors.OnSurface.toArgb()
        }
        
        // Shadow (only if not selected to avoid dark bleeding)
        if (!isSelected) {
            paint.setShadowLayer(4f, 0f, 2f, 0x40000000)
        }
        
        paint.color = bgColor
        paint.style = Paint.Style.FILL

        // Draw rounded rectangle
        val rectF = RectF(2f, 2f, width - 2f, height - 10f * density)
        canvas.drawRoundRect(rectF, 12f * density, 12f * density, paint)
        
        // Clear shadow for the rest
        paint.clearShadowLayer()

        // Draw Triangle tip (tail)
        val path = android.graphics.Path()
        val midX = width / 2f
        val bottomY = height - 10f * density
        path.moveTo(midX - 6f * density, bottomY)
        path.lineTo(midX, height.toFloat() - 2f)
        path.lineTo(midX + 6f * density, bottomY)
        path.close()
        canvas.drawPath(path, paint)

        // Draw Text
        paint.color = textColor
        paint.textSize = 14f * density
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        // Center text vertically
        val xPos = width / 2f
        val yPos = (bottomY / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("$$price", xPos, yPos, paint)

        // Draw top accent based on fuel type
        if (!isSelected && fuelType != null) {
            val accentColor = when (fuelType) {
                FuelType.DIESEL -> 0xFF4CAF50.toInt()
                FuelType.PREMIUM -> 0xFFE91E63.toInt()
                FuelType.MIDGRADE -> 0xFFFF9800.toInt()
                FuelType.REGULAR -> OilWatcherColors.Accent.toArgb()
                FuelType.ELECTRIC -> 0xFF2196F3.toInt()  // Blue accent for EV
            }
            paint.color = accentColor
            val topBarRect = RectF(
                rectF.left,
                rectF.top,
                rectF.right,
                rectF.top + 3f * density
            )
            
            // Draw a small rounded box at the top, masking the corners requires complex pathing 
            // so we just do a simpler stripe near the top.
            canvas.drawRect(
                rectF.left + 4f, 
                rectF.top, 
                rectF.right - 4f, 
                rectF.top + 4f * density, 
                paint
            )
        }

        return BitmapDrawable(context.resources, bitmap)
    }
}
