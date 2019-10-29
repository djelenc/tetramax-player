package tetramax.android;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class LocalBinder extends Binder {
    final MusicService service;

    LocalBinder(MusicService service) {
        this.service = service;
    }
}

public class MusicService extends Service {
    private static final String TAG = MusicService.class.getSimpleName();

    private LocalBinder binder = new LocalBinder(this);

    private final MediaPlayer player = new MediaPlayer();
    private final Random random = new Random();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        player.release();
        super.onDestroy();
    }

    public void play() {
        if (player.isPlaying()) {
            return;
        }
        final List<String> files = getFiles();
        final String song = files.get(random.nextInt(files.size()));

        Log.i(TAG, "Playing song " + song);

        try {
            final AssetFileDescriptor descriptor = getAssets().openFd(song);
            player.setDataSource(
                    descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(),
                    descriptor.getLength()
            );
            descriptor.close();
            player.prepare();
        } catch (IOException e) {
            Log.w(TAG, "Could not open file", e);
            return;
        }

        player.setLooping(true);
        player.start();
    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();
    }

    /**
     * Returns the list of mp3 files in the assets folder
     *
     * @return
     */
    private List<String> getFiles() {
        final List<String> mp3s = new ArrayList<>();
        try {
            final String[] files = getAssets().list("");
            if (files == null) {
                return mp3s;
            }

            for (String fileName : files) {
                if (fileName.toLowerCase().endsWith("mp3")) {
                    mp3s.add(fileName);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Error while getting files.", e);
        }

        return mp3s;
    }
}
