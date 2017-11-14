package com.eightbitpanda.lens;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eightbitpanda.lens.ui.staticscanner.ImageSurfaceView;

import java.util.List;

public class StaticScannerActivity extends AppCompatActivity {

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap != null) {
                Toast.makeText(StaticScannerActivity.this, "Captured", Toast.LENGTH_LONG).show();
                // TODO: Call Result Activity and pass the bitmap to scan it there
            }
        }
    };
    private Camera camera;
    private FrameLayout cameraPreviewLayout;
    private TextView helpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_scanner);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraPreviewLayout = (FrameLayout) findViewById(R.id.camera_preview);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String type = extras.getString("Type");
        setActionBarTitle(getActionBarTitle(type));
        helpText = (TextView) findViewById(R.id.help_text);
        setHelpText(getHelpText(type), helpText);


        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            setUpCamera();
        } else {
            requestCameraPermission();
        }


    }

    private String getActionBarTitle(String type) {
        switch (type) {
            case "Weblink":
                return "Looking for Weblinks";
            case "Call":
                return "Looking for Phone Numbers";
            case "Business Card":
                return "Scanning Business Card";
            case "Translate":
                return "Translate";
            case "Search":
                return "Search";
        }
        return "";
    }

    private void setActionBarTitle(String title) {
        if (getActionBar() != null)
            getActionBar().setTitle(title);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    private String getHelpText(String type) {
        switch (type) {
            case "Weblink":
                return "Place the scanner directly over the Weblink you want to open and tap";
            case "Call":
                return "Place the scanner directly over the Phone Number you want to call and tap";
            case "Business Card":
                return "Place the scanner directly over the Business Card you want to save and tap";
            case "Translate":
                return "Place the scanner directly over the text you want to Translate and tap";
            case "Search":
                return "Place the scanner directly over the text you want to Search and tap";
        }
        return "";
    }

    private void setHelpText(String text, TextView helpText) {
        helpText.setText(text);
    }

    private void setUpCamera() {
        camera = getCamera();
        ImageSurfaceView mImageSurfaceView = new ImageSurfaceView(this, camera);
        cameraPreviewLayout.addView(mImageSurfaceView);

        ImageView captureButton = new ImageView(this);
        captureButton.setImageResource(R.drawable.ic_camera_white_72dp);
        cameraPreviewLayout.addView(captureButton, getParams(Gravity.CENTER_HORIZONTAL, 0, 0, 8));

        ImageView galleryButton = new ImageView(this);
        galleryButton.setImageResource(R.drawable.ic_photo_size_select_actual_white_48dp);
        cameraPreviewLayout.addView(galleryButton, getParams(Gravity.START, 8, 0, 8));

        final ImageView flashButton = new ImageView(this);
        flashButton.setImageResource(R.drawable.ic_flash_on_white_48dp);
        cameraPreviewLayout.addView(flashButton, getParams(Gravity.END, 0, 8, 0));

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StaticScannerActivity.this, "Called", Toast.LENGTH_SHORT).show();
                camera.takePicture(null, null, pictureCallback);
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.Parameters.FLASH_MODE_ON.equals(camera.getParameters().getFlashMode())) {
                    camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flashButton.setImageResource(R.drawable.ic_flash_off_white_48dp);
                } else {
                    camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    flashButton.setImageResource(R.drawable.ic_flash_on_white_48dp);

                }

            }
        });
    }

    private FrameLayout.LayoutParams getParams(int gravity, int lM, int rM, int bM) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | gravity);
        lp.setMargins(lM, 0, rM, bM);
        return lp;
    }

    private Camera getCamera() {
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            List<Camera.Size> sizes = params.getSupportedPictureSizes();

            if (!sizes.isEmpty()) {
                Camera.Size mSize = sizes.get(0);
                Log.i("Res", "Chosen resolution: " + mSize.width + " " + mSize.height);
                params.setPictureSize(mSize.width, mSize.height);
            }

            mCamera.setParameters(params);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(helpText, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // We have permission, so create the camerasource
            setUpCamera();
            return;
        }


        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lens")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

}
