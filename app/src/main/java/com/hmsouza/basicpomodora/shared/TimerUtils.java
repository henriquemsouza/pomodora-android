package com.hmsouza.basicpomodora.shared;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.hmsouza.basicpomodora.R;

import java.util.concurrent.TimeUnit;

public class TimerUtils {
    private TextView textViewTime;

    public static String timerFormatter(long milliSeconds) {

        String result = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return result;

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
