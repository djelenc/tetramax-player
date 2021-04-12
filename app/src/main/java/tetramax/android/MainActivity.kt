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
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var musicInfoTextView: TextView? = null
    private var startServiceButton: Button? = null
    private var stopServiceButton: Button? = null
    private var aboutButton: Button? = null

    private val player = MediaPlayer()

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
                    Toast.LENGTH_SHORT).show()
        }
        stopServiceButton?.setOnClickListener {
            Toast.makeText(
                    applicationContext,
                    "Stop service button will be used in the service implementation.",
                    Toast.LENGTH_SHORT).show()
        }
        aboutButton?.setOnClickListener { startActivity(Intent(this@MainActivity, AboutActivity::class.java)) }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
    }

    /**
     * Starts the music player playback
     */
    private fun play() {
        if (player.isPlaying) {
            return
        }
        getFiles().random().apply {
            try {
                val descriptor = assets.openFd(this)
                player.setDataSource(
                        descriptor.fileDescriptor,
                        descriptor.startOffset,
                        descriptor.length
                )
                descriptor.close()
                player.prepare()
            } catch (e: IOException) {
                Log.w(TAG, "Could not open file", e)
                return
            }
            player.isLooping = true
            player.start()

            // display song info
            musicInfoTextView?.text = this
            Log.i(TAG, "Playing song $this")
        }
    }

    /**
     * Stops the music player playback
     */
    private fun stop() {
        if (player.isPlaying) {
            player.stop()
        }
        player.reset()

        // display song info
        musicInfoTextView?.text = ""
    }

    /**
     * Returns the list of mp3 files in the assets folder
     *
     * @return
     */
    private fun getFiles(): MutableList<String> {
        val mp3s: MutableList<String> = ArrayList()
        try {
            val files = assets.list("") ?: return mp3s
            for (fileName in files) {
                if (fileName.toLowerCase(Locale.getDefault()).endsWith("mp3")) {
                    mp3s.add(fileName)
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Error while getting files.", e)
        }
        return mp3s
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}