package at.robhor.wifisignalstrength

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.properties.Delegates

/**
 * Draws a wifi icon, with some fill representing the signal strength, and an animatable strikethrough.
 *
 * @author Robert Horvath
 */
class WifiSignalStrengthDrawable : Drawable() {
    var filled = 0.7f
        set(value) {
            field = maxOf(0f, minOf(value, 1f))
            invalidateSelf()
        }

    private val strikeThroughAnimator = ValueAnimator.ofFloat(0f, 1f)

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

    var backgroundColor by Delegates.observable(Color.argb(50, 0, 0, 0), { _, _, _ -> invalidateSelf() })
    var fillColor by Delegates.observable(Color.BLACK, { _, _, _ -> invalidateSelf() })

    private val sweepAngle = 75f
    private val radius = 1f
    private val rectF = RectF()
    private val boundsF = RectF()
    private val matrix = Matrix()
    private val paint = Paint()

    private val arcPath = Path()
    private val strikePath = Path()

    init {
        paint.isAntiAlias = true
        strikeThroughAnimator.addUpdateListener { invalidateSelf() }
    }

    override fun draw(canvas: Canvas) {
        boundsF.set(bounds)

        arcPath.rewind()
        arcPath.moveTo(0f, 0f)
        rectF.set(-radius, -radius, radius, radius)
        arcPath.arcTo(rectF, -90 + sweepAngle / 2, -sweepAngle)
        arcPath.close()
        val thickness = 0.05f * radius

        val strikeLength = strikeThroughAnimator.animatedValue as Float

        if (strikeLength > 0f) {
            matrix.setScale(strikeLength, 1f)
            matrix.postTranslate(-radius * 1.05f, -0.2f)
            matrix.postRotate(90f - sweepAngle / 2f)

            strikePath.rewind()
            strikePath.addRect(0f, -thickness * 3, radius * 1.1f, thickness, Path.Direction.CCW)
            strikePath.transform(matrix)

            arcPath.op(strikePath, Path.Op.DIFFERENCE)

            strikePath.rewind()
            strikePath.addRect(0f, -thickness, radius * 1.1f, thickness, Path.Direction.CCW)
            strikePath.transform(matrix)
            arcPath.op(strikePath, Path.Op.UNION)
        }

        val midX = radius * Math.cos(Math.toRadians(90 - sweepAngle / 2.0)).toFloat()
        rectF.set(-midX, -radius, midX, 0f)
        matrix.setRectToRect(rectF, boundsF, Matrix.ScaleToFit.CENTER)

        arcPath.transform(matrix)
        canvas.clipPath(arcPath)

        canvas.drawColor(backgroundColor)


        if (filled >= 1f) {
            canvas.drawColor(fillColor)
        } else if (filled > 0f) {
            arcPath.rewind()
            arcPath.addCircle(0f, 0f, radius * filled, Path.Direction.CW)
            arcPath.transform(matrix)

            paint.color = fillColor
            canvas.drawPath(arcPath, paint)
        }
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
