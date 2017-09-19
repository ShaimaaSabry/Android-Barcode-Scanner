package com.rwazen.interview.shaimaa.shaimaarwazen.barcodescan;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


public class BarcodeScanner
{
    private static final String TAG = "BarcodeScan";

    /************************************/

    /* scan from image */

    public static SparseArray<Barcode> scanBarcodeFromBitmap(Context context, Bitmap photo)
    {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();

        if(!barcodeDetector.isOperational())
        {
            Log.d(TAG, "Couldn't setup the detector.");
            return null;
        }

        /**********/

        Frame frame = new Frame.Builder().setBitmap(photo).build();

        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

        return barcodes;
    }

    /************************************/

    /* scan from camera */

    public static BarcodeDetector scanBarcodeFromCamera(Context context, Detector.Processor<Barcode> processor)
    {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();

        if(!barcodeDetector.isOperational())
        {
            Log.d(TAG, "Couldn't setup the detector.");
            return null;
        }

        /**********/

        barcodeDetector.setProcessor(processor);

        return barcodeDetector;
    }

    /************************************/

}
