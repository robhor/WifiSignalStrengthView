package at.robhor.wifisignalstrength

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.wifi.WifiManager
import android.util.AttributeSet
import android.view.View


private const val UPDATE_INTERVAL_MILLIS = 1000L

/**
 * Displays an icon representing wifi signal strength.
 *
 * @author Robert Horvath
 */
class WifiSignalStrengthView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val wifiDrawable = WifiSignalStrengthDrawable()
    private val levels = 5
    private var disconnected = false

    private var visible = false
    private var started = false
    private var running = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (running) {
                update()
                postDelayed(this, UPDATE_INTERVAL_MILLIS)
            }
        }
    }

    init {
        wifiDrawable.callback = this

        if (!isInEditMode) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.WifiSignalStrengthView)

            wifiDrawable.fillColor = ta.getColor(R.styleable.WifiSignalStrengthView_fillColor, wifiDrawable.fillColor)
            wifiDrawable.backgroundColor = ta.getColor(R.styleable.WifiSignalStrengthView_backgroundColor, wifiDrawable.backgroundColor)
            wifiDrawable.filled = ta.getFraction(R.styleable.WifiSignalStrengthView_fill, 1, 1, 0.8f)

            val strikeThrough = ta.getBoolean(R.styleable.WifiSignalStrengthView_wifiOff, false)
            wifiDrawable.strikeThrough = strikeThrough && !isInEditMode

            if (ta.getBoolean(R.styleable.WifiSignalStrengthView_autoUpdating, true) && !isInEditMode) start()

            ta.recycle()
        } else if (!isInEditMode) {
            start()
        }

        wifiDrawable.jumpToCurrentState()
    }

    private fun update() {
        val level = signalLevel
        val isDisconnected = level == null

        wifiDrawable.filled = when (level) {
            null -> 0f
            else -> level.toFloat() / (levels - 1)
        }
        invalidate()

        if (disconnected != isDisconnected && !isInEditMode) {
            disconnected = isDisconnected
            wifiDrawable.strikeThrough = isDisconnected
        }
    }

    private val signalLevel: Int?
        get() {
            if (isInEditMode) return 3

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) return null

            val connectionInfo = wifiManager.connectionInfo

            val rssi = connectionInfo.rssi
            return WifiManager.calculateSignalLevel(rssi, levels)
        }


    fun start() {
        started = true
        updateRunning()
    }

    fun stop() {
        started = false
        updateRunning()
    }

    fun setNotConnected() {
        stop()
        wifiDrawable.filled = 0f
        wifiDrawable.strikeThrough = true
    }

    fun setLevel(level: Float) {
        stop()
        wifiDrawable.filled = level
        wifiDrawable.strikeThrough = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        visible = false
        updateRunning()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        visible = visibility == View.VISIBLE
        updateRunning()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateRunning()
    }

    private fun updateRunning() {
        val running = visible && started && isShown
        if (running != this.running) {
            if (running) {
                update()
                postDelayed(updateRunnable, UPDATE_INTERVAL_MILLIS)
            } else {
                removeCallbacks(updateRunnable)
            }
            this.running = running
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        wifiDrawable.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == wifiDrawable
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        wifiDrawable.setBounds(0, 0, w, h)
    }

    override fun getSuggestedMinimumWidth() = (24 * resources.displayMetrics.density).toInt()
    override fun getSuggestedMinimumHeight() = (24 * resources.displayMetrics.density).toInt()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(suggestedMinimumWidth, MeasureSpec.getSize(widthMeasureSpec))
            MeasureSpec.UNSPECIFIED -> suggestedMinimumWidth
            else -> suggestedMinimumWidth
        }

        val measuredHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(suggestedMinimumHeight, MeasureSpec.getSize(heightMeasureSpec))
            MeasureSpec.UNSPECIFIED -> suggestedMinimumHeight
            else -> suggestedMinimumHeight
        }

        setMeasuredDimension(measuredWidth, measuredHeight)
    }
}
