package com.abiddarris.lanfileviewer.utils;

import static android.Manifest.permission.*;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.gretta.util.log.Log;

public class Permission {

    private static ActivityResultLauncher<Boolean> launcher;
    private static ActivityResultLauncher<Void> requestSDCardPermissionLauncher;
    public static final int PERMISSION_REQUEST_CODE = 5627;

    public static final String TAG = Log.getTag(Permission.class);

    public static void onCreate(AppCompatActivity activity) {
        launcher = activity.registerForActivityResult(
                        new PermissionResultContract(), new PermissionCallback(activity));
        requestSDCardPermissionLauncher = activity.registerForActivityResult(new SDCardPermissionResultContract(activity), new NullCallback());
    }

    public static void checkPermission(AppCompatActivity activity) {
        if (hasStoragePermissions(activity)) {
            checkSDCardPermission(activity);
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[] {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        new RequestPermissionDialog().show(activity.getSupportFragmentManager(), null);
    }

    public static void checkSDCardPermission(AppCompatActivity activity) {
        if (!LocalFileSource.hasSDCard(activity) || LocalFileSource.canWriteToSDCard(activity)) {
            return;
        }

        new RequestSDCardPermission().show(activity.getSupportFragmentManager(), null);
    }

    public static void requestSDCardWritePermission() {
        requestSDCardPermissionLauncher.launch(null);
    }

    public static void requestPermissions() {
        try {
            launcher.launch(false);
        } catch (Exception e) {
            launcher.launch(true);
        }
    }

    public static boolean hasStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED
                    && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void onRequestPermissionsResult(
            AppCompatActivity activity, int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean READ_EXTERNAL_STORAGE =
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (!(READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE)) {
                        showPermissionDeniedDialog(activity);
                    }
                }
                break;
        }
    }

    public static void showPermissionDeniedDialog(AppCompatActivity activity) {
        new PermissionDeniedDialog().show(activity.getSupportFragmentManager(), null);
    }

    private static class PermissionResultContract extends ActivityResultContract<Boolean, Boolean> {

        @Override
        public Intent createIntent(Context context, Boolean otherMethod) {
            if (otherMethod) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                return intent;
            }
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(
                    Uri.parse(
                            String.format(
                                    "package:%s",
                                    context.getApplicationContext().getPackageName())));

            return intent;
        }

        @Override
        public Boolean parseResult(int resultCode, Intent intent) {
            return Environment.isExternalStorageManager();
        }
    }

    private static class SDCardPermissionResultContract extends ActivityResultContract<Void, Void> {

        private AppCompatActivity activity;

        public SDCardPermissionResultContract(AppCompatActivity activity) {
            this.activity = activity;
        }

        @Override
        public Intent createIntent(Context context, Void arg1) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            return intent;
        }

        @Override
        public Void parseResult(int resultCode, Intent data) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                return null;
            }

            Uri treeUri = data.getData();
            Log.debug.log(TAG, treeUri.toString());

            int takeFlags =
                    data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
           
            activity.getContentResolver()
                .takePersistableUriPermission(treeUri, takeFlags);
            
            LocalFileSource.setSDCardFallback(activity, treeUri);
            
            return null;
        }

    }

    private static class PermissionCallback implements ActivityResultCallback<Boolean> {

        private AppCompatActivity activity;

        public PermissionCallback(AppCompatActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onActivityResult(Boolean granted) {
            if (!granted) {
                showPermissionDeniedDialog(activity);
                return;
            }
            checkSDCardPermission(activity);
        }
    }

    private static class NullCallback implements ActivityResultCallback<Void> {

        @Override
        public void onActivityResult(Void arg0) {}
    }
}
