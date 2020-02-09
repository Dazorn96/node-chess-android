package com.dazorn.node_chess_android.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.dazorn.node_chess_android.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ApplicationUtils {
    private static Activity _activity;
    private static boolean _restarting = false;
    private static boolean _restarted = false;

    public static void restartApplication(final Activity activity) {
        if(_restarting || !_restarted) {
            return;
        }

        _activity = activity;
        Handler handler = new Handler(_activity.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.app_restart_dialog_text)
                        .setTitle(R.string.app_restart_dialog_title)
                        .setPositiveButton(R.string.app_restart_dialog_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                restart();
                            }
                        }).show();

                _restarting = true;
            }
        };

        handler.post(runnable);
    }

    public static void forceRestartApplication(final Activity activity) {
        if(_restarting) {
            return;
        }

        _activity = activity;
        Handler handler = new Handler(_activity.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.app_restart_dialog_text)
                        .setTitle(R.string.app_restart_dialog_title)
                        .setPositiveButton(R.string.app_restart_dialog_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                restart();
                            }
                        }).show();

                _restarting = true;
            }
        };

        handler.post(runnable);
    }

    public static void SetRestartedComplete(){
        _restarted = true;
    }

    private static void restart() {
        Handler handler = new Handler(_activity.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = _activity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(_activity.getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                _activity.startActivity(intent);
                _activity.finish();

                _restarted = false;
                _restarting = false;
            }
        };
        handler.post(runnable);
    }

    public static Date parse(String input ) throws java.text.ParseException {

        //NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        //things a bit.  Before we go on we have to repair this.
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );

        return df.parse( input );

    }
}
