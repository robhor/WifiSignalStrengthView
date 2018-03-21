package at.robhor.wifistrengthview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import at.robhor.wifisignalstrength.WifiSignalStrengthView

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val toggleButton = findViewById<Button>(R.id.toggleButton)
        val togglingWifiView = findViewById<WifiSignalStrengthView>(R.id.wifiStrengthIndicatorView4)

        var connected = true
        togglingWifiView.setLevel(0.8f)
        togglingWifiView.jumpDrawablesToCurrentState()

        toggleButton.setOnClickListener {
            connected = !connected
            if (connected) {
                togglingWifiView.setLevel(0.8f)
            } else {
                togglingWifiView.setNotConnected()
            }
        }
    }
}
