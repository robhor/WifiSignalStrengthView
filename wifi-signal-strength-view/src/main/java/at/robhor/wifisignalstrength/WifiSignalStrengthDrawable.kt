package at.robhor.wifisignalstrength

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.properties.Delegates.observable

/**
 * Draws a wifi icon, with some fill representing the signal strength, and an animatable strikethrough.
 *
 * @author Robert Horvath
 */
class WifiSignalStrengthDrawable : Drawable() {
    var filled = 0.7f
        set(value) {
            val newFilled = maxOf(0f, minOf(value, 1f))
            if (field != newFilled) {
                field = newFilled
                invalidateSelf()
            }
        }

    private val strikeThroughAnimator: ValueAnimator

    var strikeThrough = false
        set(value) {
            strikeThroughAnimator.end()
            if (value) {
                strikeThroughAnimator.setFloatValues(0f, 1f)
            } else {
                strikeThroughAnimator.setFloatValues(1f, 0f)
            }
            strikeThroughAnimator.start()
            invalidateSelf()
        }

    var backgroundColor by observable(Color.argb(50, 0, 0, 0), { _, _, _ -> invalidateSelf() })
    var fillColor by observable(Color.BLACK) { _, _, _ -> invalidateSelf() }

    private val sweepAngle = 75f
    private var radius = 1f
    private val rectF = RectF()
    private val boundsF = RectF()
    private val matrix = Matrix()

    private val paint = Paint()

    init {
        paint.isAntiAlias = true
        strikeThroughAnimator = ValueAnimator.ofFloat(0f, 1f)
        strikeThroughAnimator.addUpdateListener { invalidateSelf() }
    }

    override fun draw(canvas: Canvas) {
        radius = minOf(bounds.width(), bounds.height()).toFloat()
        boundsF.set(bounds)
        paint.xfermode = null

        val midX = radius * Math.cos(Math.toRadians(90 - sweepAngle / 2.0)).toFloat()
        rectF.set(-midX, -radius, midX, 0f)
        matrix.setRectToRect(rectF, boundsF, Matrix.ScaleToFit.CENTER)

        canvas.concat(matrix)

        paint.color = backgroundColor
        rectF.set(-radius, -radius, radius, radius)
        canvas.drawArc(rectF, -90 + sweepAngle / 2, -sweepAngle, true, paint)

        canvas.save()
        val scale = filled
        canvas.scale(scale, scale)
        paint.color = fillColor
        canvas.drawArc(rectF, -90 + sweepAngle / 2, -sweepAngle, true, paint)
        canvas.restore()

        canvas.rotate(90 - sweepAngle / 2)
        canvas.translate(-radius, -radius / 4)

        paint.color = backgroundColor
        paint.isAntiAlias = false
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)

        val thickness = radius / 10f
        setStrikethroughRect(thickness)
        canvas.drawRect(rectF, paint)
        paint.isAntiAlias = true

        canvas.translate(0f, -thickness)
        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        setStrikethroughRect(thickness * 1.1f)
        canvas.drawRect(rectF, paint)
    }

    private fun setStrikethroughRect(thickness: Float) {
        val extension = radius / 10f
        val maxLength = radius * 1.2f
        rectF.set(-extension, thickness / 2, -extension + maxLength * strikeThroughAnimator.animatedValue as Float, -thickness / 2)
    }

    override fun jumpToCurrentState() {
        super.jumpToCurrentState()
        strikeThroughAnimator.end()
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}
