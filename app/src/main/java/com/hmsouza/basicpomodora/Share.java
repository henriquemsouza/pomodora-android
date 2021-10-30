package com.hmsouza.basicpomodora;

import android.content.Context;
import android.content.Intent;


public class Share {

    public void shareAction(Context context){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("Text/Plain");
        String shareBody = "Test Share";
        String shareSub = "Basics Pomodoro";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        String using_test = context.getString(R.string.share_using);
        context.startActivity(Intent.createChooser(shareIntent, using_test));
    }
}
