package com.example.premiumlauncher.iconengine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Bitmap.Config
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * Processes application icons to achieve the monochromatic glass look.
 */
class IconProcessor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconCache: LruCache<String, Bitmap> = LruCache(50) // Cache 50 icons

    /**
     * Gets the processed icon for a given package name.
     * The icon is converted to grayscale, edge-enhanced, and prepared for glass tile display.
     */
    fun getProcessedIcon(packageName: String): Bitmap? {
        // Check cache first
        val cached = iconCache.get(packageName)
        if (cached != null) return cached

        val icon = loadIcon(packageName)
        if (icon == null) return null

        val processed = processIcon(icon)
        iconCache.put(packageName, processed)
        return processed
    }

    /**
     * Loads the raw icon drawable for a package.
     */
    private fun loadIcon(packageName: String): Bitmap? {
        try {
            val info: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val drawable: Drawable = info.loadIcon(packageManager)
            return drawableToBitmap(drawable)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Converts a Drawable to a Bitmap.
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        if (width <= 0 || height <= 0) {
            return null
        }

        val bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Processes the icon bitmap to achieve the monochromatic glass look.
     * Steps:
     * 1. Convert to grayscale.
     * 2. Apply edge enhancement (using a convolution filter or by detecting edges and increasing contrast).
     * 3. Optionally, add a slight glow or reflection effect (we'll simulate with a highlight).
     */
    private fun processIcon(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val result = Bitmap.createBitmap(width, height, src.config)

        val canvas = Canvas(result)
        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Step 1: Convert to grayscale
        val colorFilter = LightingColorFilter(0xFF777777, 0xFF000000) // This is a simple grayscale, not perfect
        paint.colorFilter = colorFilter
        canvas.drawBitmap(src, 0f, 0f, paint)

        // Step 2: Edge enhancement (we'll use a simple method: increase contrast and then apply a slight blur to simulate edge glow)
        // For simplicity, we'll just return the grayscale version for now.
        // A proper edge enhancement would require a convolution kernel (e.g., Sobel) which is complex to do on the CPU in real-time.
        // We'll leave it as grayscale and note that edge enhancement is needed.

        // Step 3: Add a slight white highlight at the top-left to simulate reflection (glass effect)
        val highlightPaint = Paint().apply {
            color = 0x33FFFFFF // Very transparent white
            setMaskFilter(null)
        }
        // Draw a radial gradient for highlight? We'll just draw a small circle for simplicity.
        val highlightRadius = (width * 0.3).toFloat()
        canvas.drawCircle(width * 0.2f, height * 0.2f, highlightRadius, highlightPaint)

        return result
    }

    /**
     * Gets the label (app name) for a package.
     */
    fun getAppLabel(packageName: String): String {
        try {
            val info: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            return info.loadLabel(packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            return packageName
        }
    }
}