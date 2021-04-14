package tetramax.android

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var musicInfoTextView: TextView? = null
    private var startServiceButton: Button? = null
    private var stopServiceButton: Button? = null
    private var aboutButton: Button? = null

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)
        musicInfoTextView = findViewById(R.id.musicInfoTextView)
        startServiceButton = findViewById(R.id.startServiceButton)
        stopServiceButton = findViewById(R.id.stopServiceButton)
        aboutButton = findViewById(R.id.aboutButton)

        startServiceButton?.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "Start service button will be used in the service implementation.",
                Toast.LENGTH_SHORT
            ).show()
        }
        stopServiceButton?.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "Stop service button will be used in the service implementation.",
                Toast.LENGTH_SHORT
            ).show()
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
        player = MediaPlayer()
        Log.i(TAG, "onStart()")
    }

    /**
     * Starts the music player playback
     */
    fun play() {
        player?.let {
            if (it.isPlaying) {
                return
            }
            getFiles().random().apply {
                try {
                    val descriptor = assets.openFd(this)
                    it.setDataSource(
                        descriptor.fileDescriptor,
                        descriptor.startOffset,
                        descriptor.length
                    )
                    descriptor.close()
                    it.prepare()
                } catch (e: IOException) {
                    Log.w(TAG, "Could not open file", e)
                    return
                }
                it.isLooping = true
                it.start()

                // display song info
                musicInfoTextView?.text = this
                Log.i(TAG, "Playing song $this")
            }
        }
    }

    /**
     * Stops the music player playback
     */
    fun stop() {
        player?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
        }

        // display song info
        musicInfoTextView?.text = ""
    }

    /**
     * Returns the list of mp3 files in the assets folder
     *
     * @return
     */
    private fun getFiles(): List<String> =
        assets.list("")?.filter { it.toLowerCase(Locale.getDefault()).endsWith("mp3") }
            ?: emptyList()

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}