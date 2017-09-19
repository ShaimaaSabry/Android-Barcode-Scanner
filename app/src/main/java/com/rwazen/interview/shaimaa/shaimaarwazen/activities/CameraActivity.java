package com.rwazen.interview.shaimaa.shaimaarwazen.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.rwazen.interview.shaimaa.shaimaarwazen.R;
import com.rwazen.interview.shaimaa.shaimaarwazen.barcodescan.BarcodeScanner;
import java.io.IOException;
import java.util.List;


public class CameraActivity extends AppCompatActivity
{
    private static final String TAG = "BarcodeScan";

    Context _context = this;
    private CameraSource _cameraSource;
    private SurfaceView _cameraView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        /**********/

        Detector.Processor<Barcode> processor = getProcessor();
        BarcodeDetector barcodeDetector = BarcodeScanner.scanBarcodeFromCamera(_context, processor);
        if(barcodeDetector == null)
        {
            returnResultFailure("Couldn't setup barcode detector.");
        }
        setCamera(barcodeDetector);
    }

    /************************************/

    private Detector.Processor<Barcode> getProcessor()
    {
        Detector.Processor<Barcode> processor = new Detector.Processor<Barcode>()
        {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections)
            {
                Log.d(TAG, "Detections received.");

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() != 0)
                {
                    returnResultSuccess(barcodes);
                }
            }
        };

        return processor;
    }

    private void setCamera(BarcodeDetector barcodeDetector)
    {
        int[] size = calcOptimalPreviewSize();
        int width = size[0];
        int height = size[1];

        _cameraSource = new CameraSource
                .Builder(_context, barcodeDetector)
                .setRequestedPreviewSize(width, height)
                .setAutoFocusEnabled(true)
                .build();

        /**********/

        _cameraView = (SurfaceView) findViewById(R.id.surfaceView_camera);

        /**********/

        SurfaceHolder.Callback callback = new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                if (!startCameraSource())
                {
                    requestPermissions();
                    return;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                if(_cameraSource != null)
                {
                    _cameraSource.stop();
                    //_cameraSource.release();
                }
            }
        };

        /**********/

        _cameraView.getHolder().addCallback(callback);

        /**********/
    }


    private boolean startCameraSource()
    {
        int permissionCheck = ActivityCompat.checkSelfPermission(_context, android.Manifest.permission.CAMERA);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            try
            {
                _cameraSource.start(_cameraView.getHolder());
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error starting camera source: " + e.getMessage());
                returnResultFailure("Can't start camera source.");
            }

            return true;
        }
        else
        {
            return false;
        }
    }


    private int[] calcOptimalPreviewSize()
    {
        int[] optimalSize = new int[2];
        //optimalSize[0] = 640;
        //optimalSize[1] = 480;
        optimalSize[0] = 1920;
        optimalSize[1] = 1080;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = new String[0];
            try
            {
                cameraIds = manager.getCameraIdList();
                CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[0]);
                StreamConfigurationMap map = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                android.util.Size[] sizes = map.getOutputSizes(SurfaceTexture.class);

                android.util.Size size = sizes[0];

                optimalSize[0] = size.getWidth();
                optimalSize[1] = size.getHeight();
            }
            catch (CameraAccessException e)
            {
                Log.e(TAG, "Error PreviewSize: " + e.getMessage());
            }
        }
        else
        {
            Camera camera = Camera.open();
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            camera.release();

            Camera.Size size = sizes.get(0);

            optimalSize[0] = size.width;
            optimalSize[1] = size.height;
        }

        Log.d(TAG, "Optimal Size: " + String.valueOf(optimalSize[0]) + " " + String.valueOf(optimalSize[1]));
        return optimalSize;
    }

    /************************************/

    /* request camera permission */

    static final int PERMISSIONS_REQUEST_CAMERA = 1;

    private void requestPermissions()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSIONS_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_CAMERA:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startCameraSource();
                }
                else
                {
                    returnResultFailure("Camera permission denied.");
                }
                return;
            }
        }
    }

    /************************************/

    private void returnResultSuccess(SparseArray<Barcode> barcodes)
    {
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray("barcodes", barcodes);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void returnResultFailure(String error)
    {
        Intent intent = new Intent();
        intent.putExtra("error", error);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    /************************************/
}
