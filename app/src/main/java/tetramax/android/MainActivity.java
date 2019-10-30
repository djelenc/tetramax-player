package tetramax.android;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView musicInfoTextView;
    private Button playButton, stopButton, startServiceButton, stopServiceButton;

    private final MediaPlayer player = new MediaPlayer();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        musicInfoTextView = findViewById(R.id.musicInfoTextView);
        startServiceButton = findViewById(R.id.startServiceButton);
        stopServiceButton = findViewById(R.id.stopServiceButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        getApplicationContext(),
                        "Start service button will be used in the service implementation.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        getApplicationContext(),
                        "Stop service button will be used in the service implementation.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        player.release();
        super.onStop();
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

        // display song info
        musicInfoTextView.setText(song);
    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();

        // display song info
        musicInfoTextView.setText("");
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
