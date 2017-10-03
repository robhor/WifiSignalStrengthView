package at.robhor.wifisignalstrength

import android.content.Context
import android.graphics.Outline
import android.net.wifi.WifiManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView


private const val NOT_CONNECTED = -1
private const val UPDATE_INTERVAL_MILLIS = 1000L

/**
 * Displays an icon representing wifi signal strength.
 *
 * @author Robert Horvath
 */
class WifiSignalStrengthView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs) {
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
        setImageDrawable(wifiDrawable)

        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.WifiSignalStrengthView)

            wifiDrawable.fillColor = ta.getColor(R.styleable.WifiSignalStrengthView_fillColor, wifiDrawable.fillColor)
            wifiDrawable.backgroundColor = ta.getColor(R.styleable.WifiSignalStrengthView_unfilledColor, wifiDrawable.backgroundColor)
            wifiDrawable.filled = ta.getFraction(R.styleable.WifiSignalStrengthView_fill, 1, 1, 0.8f)

            val strikeThrough = ta.getBoolean(R.styleable.WifiSignalStrengthView_strikeThrough, false)
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
        val isDisconnected = level == NOT_CONNECTED

        if (isDisconnected) {
            wifiDrawable.filled = 0f
        } else {
            wifiDrawable.filled = level.toFloat() / (levels - 1)
        }

        if (disconnected != isDisconnected && !isInEditMode) {
            disconnected = isDisconnected
            wifiDrawable.strikeThrough = isDisconnected
        }
    }

    private val signalLevel: Int
        get() {
            if (isInEditMode) return 3

            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) return NOT_CONNECTED

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
}
