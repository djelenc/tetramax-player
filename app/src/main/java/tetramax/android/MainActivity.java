package tetramax.android;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView musicInfoTextView;
    private Button playButton, stopButton, startServiceButton, stopServiceButton;

    private MusicService service;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            MainActivity.this.service = ((LocalBinder) service).service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
            MainActivity.this.service = null;
        }
    };

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
                if (service != null) {
                    service.play();
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    service.stop();
                }
            }
        });
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, MusicService.class);
                startService(intent);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        });
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    unbindService(connection);
                    service = null;
                    stopService(new Intent(MainActivity.this, MusicService.class));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");

        if (isServiceRunning()) {
            bindService(new Intent(MainActivity.this, MusicService.class),
                    connection, Context.BIND_AUTO_CREATE);
        }

        // subscribes to receiving messages from service using filter "mplayer"
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("mplayer"));
    }

    /**
     * Handles messages coming from the service
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (musicInfoTextView != null) {
                musicInfoTextView.setText(intent.getStringExtra("song"));
            }
        }
    };

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        unbindService(connection);
        service = null;

        // unregisters from receiving messages from the service
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    private boolean isServiceRunning() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(MusicService.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }
}
