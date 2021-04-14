package tetramax.android

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {
    private var service: MusicService? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(TAG, "onServiceConnected()")
            this@MainActivity.service = (service as MusicService.LocalBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i(TAG, "onServiceDisconnected()")
            service = null
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            musicInfoTextView?.text = intent?.getStringExtra("song")
        }
    }

    private var musicInfoTextView: TextView? = null
    private var startServiceButton: Button? = null
    private var stopServiceButton: Button? = null
    private var aboutButton: Button? = null
    private var playButton: Button? = null
    private var stopButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)
        musicInfoTextView = findViewById(R.id.musicInfoTextView)
        startServiceButton = findViewById(R.id.startServiceButton)
        stopServiceButton = findViewById(R.id.stopServiceButton)
        aboutButton = findViewById(R.id.aboutButton)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)

        playButton?.setOnClickListener { service?.play() }
        stopButton?.setOnClickListener { service?.stop() }
        startServiceButton?.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            startService(intent)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
        stopServiceButton?.setOnClickListener {
            service?.let {
                unbindService(connection)
                service = null
                stopService(Intent(this@MainActivity, MusicService::class.java))
            }
        }

        aboutButton?.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    AboutActivity::class.java
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
        if (isServiceRunning()) {
            bindService(
                Intent(this@MainActivity, MusicService::class.java),
                connection,
                BIND_AUTO_CREATE
            )
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("mplayer"))
    }


    /** Returns true iff the MusicService service is running */
    @Suppress("DEPRECATION")
    private fun isServiceRunning(): Boolean =
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == MusicService::class.java.canonicalName }


    override fun onStop() {
        Log.i(TAG, "onStop()")
        service?.let {
            unbindService(connection)
            service = null
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onStop()
    }


    override fun onPause() {
        Log.i(TAG, "onPause()")
        super.onPause()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}