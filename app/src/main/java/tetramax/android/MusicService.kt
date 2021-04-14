package tetramax.android

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.io.IOException
import java.util.*

class MusicService : Service() {
    val player = MediaPlayer()

    // holds the name of the song currently being played
    public var song = ""

    // an implementation of Binder interface
    internal class LocalBinder(val service: MusicService) : Binder()

    // a reference to LocalBinder
    private val binder = LocalBinder(this)

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    /**
     * Starts the music player playback
     */
    fun play() {
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
            song = this

            // display song info
            Log.i(TAG, "Playing song $this")
            broadcastSongName()
        }
    }

    private fun broadcastSongName() {
        val intent = Intent("mplayer") // mplayer is the name of the broadcast
        intent.putExtra("song", song) // song name is added as the parameter
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent) // the broadcast is sent
    }


    /**
     * Stops the music player playback
     */
    fun stop() {
        if (player.isPlaying) {
            player.stop()
        }
        player.reset()
        this.song = " "
        broadcastSongName()
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
        private val TAG = MusicService::class.java.simpleName
    }
}