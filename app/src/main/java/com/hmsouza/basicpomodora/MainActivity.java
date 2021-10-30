package com.hmsouza.basicpomodora;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public TimerUtils timer_utils = new TimerUtils();
    int breakCount = 0;
    boolean breakOrWork = false;
    private long timeCountInMilliSeconds = 1 * 60000;
    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private ImageView iconStop;
    private ImageView imageViewStartStop;
    private ImageView playButton, iconTomato, iconTakeABreak;
    private CountDownTimer countDownTimer;
    private boolean vibration;
    private SharedPreferences settings;
    private Vibrator vibrator;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadSettings();
        timer_utils.setInitialTimer(settings, MainActivity.this, this);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initDrawer();

        navigationClick();

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        loadSettings();

        initClickListeners();
        timer_utils.setInitialTimer(settings, MainActivity.this, this);
        initAds();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void initAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdView mAdView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();

                mAdView.loadAd(adRequest);
            }
        });
    }

    public void navigationClick() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.settings) {
                    Intent settingsIntent = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settingsIntent);
                }
                if (itemId == R.id.about) {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, getText(R.string.podomodora_search).toString());
                    startActivity(intent);

                }
                if (itemId == R.id.share) {
//                    Share share_class = new Share();
//                    share_class.shareAction(MainActivity.this);

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/html");
                }
                if (itemId == R.id.help_me) {
                    Intent help = new Intent(getBaseContext(), HelpMeActivity.class);
                    startActivity(help);
                }
                return false;
            }
        });
    }

    private void loadSettings() {
        vibration = settings.getBoolean("vibration", false);
        settings.registerOnSharedPreferenceChangeListener(MainActivity.this);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        progressBarCircle = findViewById(R.id.progressBarCircle);
        textViewTime = findViewById(R.id.textViewTime);
        iconStop = findViewById(R.id.iconStop);
        imageViewStartStop = findViewById(R.id.imageViewStartStop);
        playButton = findViewById(R.id.playButton);
        iconTakeABreak = findViewById(R.id.iconTakeABreak);
        iconTomato = findViewById(R.id.iconTomato);
    }

    private void initClickListeners() {
        iconStop.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
        playButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iconStop:
            case R.id.iconTakeABreak:
                stopTimer();
                break;
            case R.id.iconTomato:
                resetCountDownTimer();
                break;
            case R.id.playButton:
                // Log.i("teste", "playy");
                initTimer();
                break;
        }
    }

    public void visibleButton() {
        imageViewStartStop.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.GONE);
    }

    private void resetCountDownTimer() {
        breakCount = 0;
        stopCountDownTimer();
        textViewTime.setText(timerFormatter(timeCountInMilliSeconds));
        setProgressBarValues();
        iconTakeABreak.setVisibility(View.GONE);
        iconTomato.setVisibility(View.INVISIBLE);
//        imageViewStartStop.setImageResource(R.mipmap.ic_play_sign);
        timerStatus = TimerStatus.STOPPED;
    }

    private void initTimer() {
        breakOrWork = true;
        if (timerStatus == TimerStatus.STOPPED) {
            workoutInitialTimerValue();
            setProgressBarValues();
            iconTomato.setVisibility(View.VISIBLE);
//            iconStop.setVisibility(View.VISIBLE);
//            imageViewStartStop.setImageResource(R.mipmap.ic_pause_button);
            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
        }
    }

    private void stopTimer() {
        breakOrWork = true;
//        imageViewStartStop.setImageResource(R.mipmap.ic_play_sign);
        timerStatus = TimerStatus.STOPPED;
        stopCountDownTimer();
    }

    private void changeButtonsVisibility() {

        breakOrWork = true;
        if (timerStatus == TimerStatus.STOPPED) {
            workoutInitialTimerValue();
            setProgressBarValues();
            // iconTomato.setVisibility(View.VISIBLE);
            iconStop.setVisibility(View.VISIBLE);
//            imageViewStartStop.setImageResource(R.mipmap.ic_pause_button);
            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();

        } else {
            imageViewStartStop.setImageResource(R.mipmap.ic_play_sign);
            timerStatus = TimerStatus.STOPPED;
            stopCountDownTimer();
        }

    }

    private void workoutInitialTimerValue() {
        int time = Integer.parseInt(settings.getString("work_duration", "25"));
        timeCountInMilliSeconds = time * 60 * 1000;
    }

    private void breakInitialTimerValue() {
        int time = Integer.parseInt(settings.getString("break_duration", "25"));

        timeCountInMilliSeconds = time * 60 * 1000;
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTime.setText(timerFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                breakCount++;
                textViewTime.setText(timerFormatter(timeCountInMilliSeconds));
                setProgressBarValues();
                // iconTomato.setVisibility(View.GONE);
                iconTakeABreak.setVisibility(View.GONE);
                imageViewStartStop.setImageResource(R.mipmap.ic_play_sign);
                timerStatus = TimerStatus.STOPPED;
                // Vibration
                vibration = settings.getBoolean("vibration", true);

//                if (vibration) vibrator.vibrate(1000);

                // checking work and break times
                checkBreakOrWork();
            }
        }.start();

    }

    private void scheduleNotification(Notification notification, long delay) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = System.currentTimeMillis() + delay;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);

    }

    public void checkBreakOrWork() {
        if (breakCount != 8) {
            if (breakOrWork) {
                takeBreakAlert();
            } else if (!breakOrWork) {
                workoutAlert();
            } else {
                Toast.makeText(getApplicationContext(), getText(R.string.finish).toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getText(R.string.finished).toString(), Toast.LENGTH_LONG).show();
            resetCountDownTimer();

        }

    }

    public void takeBreakAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(getText(R.string.success_msg).toString());

        alertDialogBuilder.setPositiveButton(getText(R.string.no).toString(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
//                changeButtonsVisibility();
                initTimer();
            }
        });

        alertDialogBuilder.setNegativeButton(getText(R.string.take_a_break).toString(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        breakStartStop();
                        initBreak();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void workoutAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(getText(R.string.break_msg_over).toString());
        alertDialogBuilder.setPositiveButton(getText(R.string.continue_01).toString(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                changeButtonsVisibility();
            }
        });

        alertDialogBuilder.setNegativeButton(getText(R.string.finish).toString(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetCountDownTimer();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void initBreak() {
        breakOrWork = false;
        if (timerStatus == TimerStatus.STOPPED) {

            breakInitialTimerValue();
            setProgressBarValues();

            iconTakeABreak.setVisibility(View.VISIBLE);
//            iconStop.setVisibility(View.VISIBLE);
            iconTomato.setVisibility(View.INVISIBLE);
//            imageViewStartStop.setImageResource(R.mipmap.ic_pause_button);

            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
        }
    }

    private void breakStartStop() {
        breakOrWork = false;
        if (timerStatus == TimerStatus.STOPPED) {

            breakInitialTimerValue();
            setProgressBarValues();

            iconTakeABreak.setVisibility(View.VISIBLE);
            iconStop.setVisibility(View.VISIBLE);

            imageViewStartStop.setImageResource(R.mipmap.ic_pause_button);

            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
        } else {
            breakCount = 0;
            imageViewStartStop.setImageResource(R.mipmap.ic_play_sign);
            timerStatus = TimerStatus.STOPPED;
            stopCountDownTimer();

        }

    }

    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    private void setProgressBarValues() {
        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    private String timerFormatter(long milliSeconds) {

        String hour_minute_second = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hour_minute_second;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), Settings.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private enum TimerStatus {
        STARTED, STOPPED,
    }
}
