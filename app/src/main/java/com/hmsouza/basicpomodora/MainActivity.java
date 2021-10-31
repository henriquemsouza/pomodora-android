package com.hmsouza.basicpomodora;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.hmsouza.basicpomodora.domain.TimerStatus;
import com.hmsouza.basicpomodora.shared.TimerUtils;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public TimerUtils timerUtils = new TimerUtils();
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
        timerUtils.setInitialTimer(settings, MainActivity.this, this);
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
        timerUtils.setInitialTimer(settings, MainActivity.this, this);
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
                initTimer();
                break;
        }
    }

    private void resetCountDownTimer() {
        breakCount = 0;
        stopCountDownTimer();
        textViewTime.setText(TimerUtils.timerFormatter(timeCountInMilliSeconds));
        setProgressBarValues();
        iconTakeABreak.setVisibility(View.GONE);
        iconTomato.setVisibility(View.INVISIBLE);
        timerStatus = TimerStatus.STOPPED;
    }

    private void initTimer() {
        breakOrWork = true;
        if (timerStatus == TimerStatus.STOPPED) {
            workoutInitialTimerValue();
            setProgressBarValues();
            iconTomato.setVisibility(View.VISIBLE);
            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
        }
    }

    private void stopTimer() {
        breakOrWork = true;
        timerStatus = TimerStatus.STOPPED;
        stopCountDownTimer();
    }

    private void changeButtonsVisibility() {
        breakOrWork = true;

        if (timerStatus == TimerStatus.STOPPED) {
            workoutInitialTimerValue();
            setProgressBarValues();
            iconStop.setVisibility(View.VISIBLE);
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
        timeCountInMilliSeconds = getTimeCountInMilliSeconds(time);
    }

    private void breakInitialTimerValue() {
        int time = Integer.parseInt(settings.getString("break_duration", "25"));

        timeCountInMilliSeconds = getTimeCountInMilliSeconds(time);
    }

    private int getTimeCountInMilliSeconds(int time) {
        return time * 60 * 1000;
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTime.setText(TimerUtils.timerFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                breakCount++;
                textViewTime.setText(TimerUtils.timerFormatter(timeCountInMilliSeconds));
                setProgressBarValues();
                iconTakeABreak.setVisibility(View.GONE);
                imageViewStartStop.setImageResource(R.mipmap.ic_play_sign);
                timerStatus = TimerStatus.STOPPED;
                // Vibration
                vibration = settings.getBoolean("vibration", true);

                checkBreakOrWork();
            }
        }.start();

    }


    public void checkBreakOrWork() {
        if (breakCount != 8) {
            if (breakOrWork) {
                takeBreakAlert();
            } else {
                workoutAlert();
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
                initTimer();
            }
        });

        alertDialogBuilder.setNegativeButton(getText(R.string.take_a_break).toString(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
            iconTomato.setVisibility(View.INVISIBLE);

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

}
