package com.hmsouza.basicpomodora;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class TimerUtils {
    private TextView textViewTime;

    private String timerFormatter(long milliSeconds) {

        String hourMinuteSecond = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hourMinuteSecond;
    }

    private void initViews(Activity activity) {
        textViewTime = activity.findViewById(R.id.textViewTime);
    }

    public void setInitialTimer(SharedPreferences settings, Context context, Activity activity) {
        initViews(activity);

        Long workoutTime = Long.valueOf(Integer.parseInt(settings.getString("work_duration", "25")) * 60000);

        textViewTime.setText(timerFormatter(workoutTime));
    }


}
