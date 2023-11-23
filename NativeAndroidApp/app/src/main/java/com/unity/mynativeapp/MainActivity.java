package com.unity.mynativeapp;

import java.util.*;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private enum ActivityType {
        PLAYER_ACTIVITY, PLAYER_GAME_ACTIVITY, BOTH
    }

    boolean isUnityLoaded = false;
    private ActivityType mActivityType = ActivityType.BOTH;
    private boolean isGameActivity = false;

    private Button mShowUnityButton;
    //private Button mShowUnityGameButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isUnityLoaded = false;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        adjustButtons();

        TextView textView = findViewById(R.id.kotlin_entry_point);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.unity.mynativeapp.kotlin.ChooserActivity.class);
                startActivity(intent);
            }
        });

        //handleIntent(getIntent());

        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    private boolean allRuntimePermissionsGranted() {
        for (String permission : REQUIRED_RUNTIME_PERMISSIONS) {
            if (permission != null) {
                if (!isPermissionGranted(this, permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_RUNTIME_PERMISSIONS) {
            if (permission != null) {
                if (!isPermissionGranted(this, permission)) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUESTS
            );
        }

    }


    private boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private static final String[] REQUIRED_RUNTIME_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*handleIntent(intent);
        setIntent(intent);
    }

    /*void handleIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) return;

        if (intent.getExtras().containsKey("setColor")) {
            View v = findViewById(R.id.finish_button);
            switch (intent.getExtras().getString("setColor")) {
                case "yellow":
                    v.setBackgroundColor(Color.YELLOW);
                    break;
                case "red":
                    v.setBackgroundColor(Color.RED);
                    break;
                case "blue":
                    v.setBackgroundColor(Color.BLUE);
                    break;
                default:
                    v.setBackgroundColor(0xFFd6d7d7);
                    break;
            }
        }*/
    }

    public void onClickShowUnity(View v) {
        isUnityLoaded = true;
        isGameActivity = !(v.getId() == R.id.show_unity_button);
        disableShowUnityButtons();

        Intent intent;
        switch (v.getId()) {
            case R.id.show_unity_button:
                intent = new Intent(this, getMainUnityActivityClass());
                break;

            //case R.id.show_unity_game_button:
              //  intent = new Intent(this, getMainUnityGameActivityClass());
              //  break;

            default:
                return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            isUnityLoaded = false;
            enableShowUnityButtons();
            showToast("Unity finished.");
        }
    }

    public void unloadUnity(Boolean doShowToast) {
        if (isUnityLoaded) {
            Intent intent;
            if (isGameActivity)
                intent = new Intent(this, getMainUnityGameActivityClass());
            else
                intent = new Intent(this, getMainUnityActivityClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("doQuit", true);
            startActivity(intent);
            isUnityLoaded = false;
        } else if (doShowToast) {
            showToast("Show Unity First");
        }
    }

    public void onClickFinish(View v) {
        unloadUnity(true);
    }

    private void showToast(String message) {
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private boolean checkActivityExist(String activityName) {
        ActivityInfo[] aInfo = new ActivityInfo[0];
        try {
            aInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;
            if (aInfo == null) {
                return false;
            }

            for (ActivityInfo info : aInfo) {
                if (activityName.equals(info.name)) return true;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean existUnityPlayerActivity() {
        return checkActivityExist("com.unity3d.player.UnityPlayerActivity");
    }

    private boolean existUnityPlayerGameActivity() {
        return checkActivityExist("com.unity3d.player.UnityPlayerGameActivity");
    }

    private Class findClassUsingReflection(String className) {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class getMainUnityActivityClass() {
        return findClassUsingReflection("com.unity.mynativeapp.MainUnityActivity");
    }

    private Class getMainUnityGameActivityClass() {
        return findClassUsingReflection("com.unity.mynativeapp.MainUnityGameActivity");
    }

    private void adjustButtons() {
        mShowUnityButton = findViewById(R.id.show_unity_button);
        //mShowUnityGameButton = findViewById(R.id.show_unity_game_button);

        if (existUnityPlayerActivity()) {
            mShowUnityButton.setVisibility(View.VISIBLE);
            mActivityType = ActivityType.PLAYER_ACTIVITY;
        }

        /*if (existUnityPlayerGameActivity()) {
            //mShowUnityGameButton.setVisibility(View.VISIBLE);
            mActivityType = ActivityType.PLAYER_GAME_ACTIVITY;
        }

        if (mShowUnityButton.getVisibility() == View.VISIBLE && mShowUnityGameButton.getVisibility() == View.VISIBLE) {
            mActivityType = ActivityType.BOTH;
        }*/
    }

    private void disableShowUnityButtons() {
        if (mActivityType != ActivityType.BOTH) return;

        mShowUnityButton.setEnabled(!isGameActivity);
        //mShowUnityGameButton.setEnabled(isGameActivity);
    }

    private void enableShowUnityButtons() {
        if (mActivityType != ActivityType.BOTH) return;

        mShowUnityButton.setEnabled(true);
        //mShowUnityGameButton.setEnabled(true);
    }
}
