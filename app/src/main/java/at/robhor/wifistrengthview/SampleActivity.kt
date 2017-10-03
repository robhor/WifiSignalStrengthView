package at.robhor.wifistrengthview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import at.robhor.wifisignalstrength.WifiSignalStrengthView

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val findViewById = findViewById<WifiSignalStrengthView>(R.id.wifiStrengthIndicatorView)
        findViewById.alpha = 0.8f
    }
}
