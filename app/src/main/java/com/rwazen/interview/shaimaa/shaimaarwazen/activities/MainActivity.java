package com.rwazen.interview.shaimaa.shaimaarwazen.activities;

import android.content.Context;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.vision.barcode.Barcode;
import com.rwazen.interview.shaimaa.shaimaarwazen.R;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "BarcodeScan";

    private static final int REQUEST_CAMERA_ACTIVITY = 2;

    Context _context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /************************************/

    public void onClickScan(View view)
    {
        Intent intent = new Intent(_context, CameraActivity.class);
        startActivityForResult(intent, REQUEST_CAMERA_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult");

        if (requestCode == REQUEST_CAMERA_ACTIVITY)
        {
            if(resultCode == RESULT_OK)
            {
                SparseArray<Barcode> barcodes = data.getExtras().getSparseParcelableArray("barcodes");

                if (barcodes == null)
                {
                    displayError(null);
                }
                else
                {
                    displayResult(barcodes);
                }
            }
            else
            {
                String error = null;
                if(data != null) {
                    error = data.getStringExtra("error");
                }
                displayError(error);
            }
        }
    }

    /************************************/

    private void displayResult(SparseArray<Barcode> barcodes)
    {
        String result = "";

        Log.d(TAG, String.valueOf("Number of barcodes detected: " + barcodes.size()));

        if(barcodes.size() <= 0)
            result = "No barcodes detected.";

        for(int i =0; i < barcodes.size(); i++)
        {
            String value = barcodes.valueAt(i).displayValue;

            result += "Barcode #" + String.valueOf(i) + ": " + value + "\n";
        }

        TextView textView = (TextView) findViewById(R.id.textView_result);
        textView.setText(result);
    }

    private void displayError(String error)
    {
        if(error == null) {
            error = "Detection cancelled.";
        }

        TextView textView = (TextView) findViewById(R.id.textView_result);
        textView.setText(error);
    }

    /************************************/
}
