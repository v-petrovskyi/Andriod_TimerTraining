package training.android.timertraining;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences defaultSharedPreferences;
    private SeekBar seekBar;
    private TextView textView;
    private Button button;
    private boolean isTimerOn;
    private CountDownTimer countDownTimer;
    private int defaultInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.buttonStart);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        seekBar.setMax(600);
        setIntervalFromSharedPreferences(defaultSharedPreferences);
        isTimerOn = false;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateTimer(progress * 1000L);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void start(View view) {
        if (!isTimerOn) {
            button.setText("Stop");
            seekBar.setEnabled(false);
            isTimerOn = true;
            countDownTimer = new CountDownTimer(seekBar.getProgress() * 1_000L, 1_000) {
                @Override
                public void onTick(long l) {
                    updateTimer(l);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable_sound", true)) {
                        String melodyName = sharedPreferences.getString("timer_melody", "bell");
                        switch (melodyName) {
                            case "bell": {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                                mediaPlayer.start();
                                break;
                            }
                            case "alarm_siren": {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                                mediaPlayer.start();
                                break;
                            }
                            case "bip": {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                                mediaPlayer.start();
                                break;
                            }
                        }
                    }
                    setDefaultData();
                }
            };
            countDownTimer.start();
        } else {
            setDefaultData();
        }
    }

    private void setDefaultData() {
        countDownTimer.cancel();
        button.setText("Start");
        seekBar.setEnabled(true);
        isTimerOn = false;
        setIntervalFromSharedPreferences(defaultSharedPreferences);
    }

    private void updateTimer(long l) {
        int minutes = (int) (l / 1000 / 60);
        int seconds = (int) (l / 1000 - (minutes * 60));

        String minutesString = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String secondsString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);

        textView.setText(minutesString + ":" + secondsString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this, AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences) {
        defaultInterval = Integer.parseInt(sharedPreferences.getString("default_interval", "30"));
        updateTimer(defaultInterval * 1000L);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")) {
            setIntervalFromSharedPreferences(defaultSharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}