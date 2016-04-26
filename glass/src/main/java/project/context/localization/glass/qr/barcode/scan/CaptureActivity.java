/*
 * Copyright (C) 2008 ZXing authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package project.context.localization.glass.qr.barcode.scan;

// Adjust to whatever the main package name is

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.Result;
//import com.google.zxing.ResultMetadataType;
//import com.google.zxing.ResultPoint;
//import com.google.zxing.client.android.camera.CameraManager;
//import com.google.zxing.client.result.ParsedResult;
//import com.google.zxing.client.result.ResultParser;
//import com.jaxbot.glass.barcode.BaseGlassActivity;
//import com.jaxbot.glass.barcode.migrated.BeepManager;
//import com.jaxbot.glass.barcode.migrated.InactivityTimer;
//import com.jaxbot.glass.barcode.scan.ui.ViewfinderView;
//import com.jaxbot.glass.qrlens.MainGlassActivity;
//import com.jaxbot.glass.qrlens.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import google.zxing.client.android.camera.CameraManager;
import project.context.localization.glass.R;
import project.context.localization.glass.qr.barcode.BaseGlassActivity;
import project.context.localization.glass.qr.barcode.migrated.BeepManager;
import project.context.localization.glass.qr.barcode.migrated.InactivityTimer;
import project.context.localization.glass.qr.barcode.scan.ui.ViewfinderView;
import project.context.localization.glass.qr.qrlens.MainActivity;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as
 * the image processing
 * is happening, and then overlays the results when a scan is successful.
 * ---------------------------------------------------------------------------------------------
 * <p>The code was imported from the following repository</p> https://github.com/jaxbot/glass-qrlens.
 *
 * <p>Some changes that were made will be explained in the following section:</p>
 * <ul>
 *      <li>
 *          In -- {@link CaptureActivity#onResume()} --
 *          <p>Time out of QR Code Scanner extended</p>
 *          <p>from 15 seconds to 60 seconds</p>
 *
 *     </li>
 *     <li>
 *         In -- {@link CaptureActivity#handleDecode(Result, Bitmap, float)} --
 *         <p>Play Beep sound every time</p>
 *     </li>
 *     <li>
 *         In -- {@link CaptureActivity#handleDecodeInternally(Result, Bitmap)} --
 *         <p>Does not cancel timer.</p>
 *         <p>Gets Text value from Qr Code and lookup location description by place id</p>
 *         <p>Resets SurfaceView by calling onPause(),</p>
 *         <p>re-initialising the camera and calling onResume()</p>
 *     </li>
 *     <li>
 *         In -- {@link project.context.localization.glass.qr.barcode.scan.ui.ViewfinderView#ViewfinderView(Context, AttributeSet)} --
 *         <p>Added LinearLayout with textView</p>
 *     </li>
 *     <li>
 *         In -- {@link project.context.localization.glass.qr.barcode.scan.ui.ViewfinderView#onDraw(Canvas)} --
 *         <p>Display resultText in TextView, adjust textView width and add it to the layout</p>
 *     </li>
 *</ul>
 * ---------------------------------------------------------------------------------------------
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends BaseGlassActivity implements
        SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final Collection<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet
            .of(ResultMetadataType.ISSUE_NUMBER,
                    ResultMetadataType.SUGGESTED_PRICE,
                    ResultMetadataType.ERROR_CORRECTION_LEVEL,
                    ResultMetadataType.POSSIBLE_COUNTRY);

    private CameraManager mCameraManager;
    private CaptureActivityHandler mHandler;
    private Result mSavedResultToShow;
    private ViewfinderView mViewfinderView;
    private boolean mHasSurface;
    private Map<DecodeHintType, ?> mDecodeHints;
    private InactivityTimer mInactivityTimer;
    private BeepManager mBeepManager;

    private View tmpConvertView;

    private Timer mTimer;

    public ViewfinderView getViewfinderView() {
        return mViewfinderView;
    }

    public Handler getHandler() {
        return mHandler;
    }

    CameraManager getCameraManager() {
        return mCameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Context ctx = this;
        final Activity activity = this;
        final CaptureActivity that = this;

        CardScrollView csr = new CardScrollView(ctx);
        csr.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                View convertView;
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_capture, viewGroup);
                tmpConvertView = convertView;

                mHasSurface = false;
                mInactivityTimer = new InactivityTimer(activity);
                mBeepManager = new BeepManager(activity);

                mViewfinderView = (ViewfinderView) convertView.findViewById(R.id.viewfinder_view);

                // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
                // want to open the camera driver and measure the screen size if we're going to show the help on
                // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
                // off screen.
                mCameraManager = new CameraManager(getApplication());
                mViewfinderView.setCameraManager(mCameraManager);

                mHandler = null;

                SurfaceView surfaceView = (SurfaceView) convertView.findViewById(R.id.preview_view);
                SurfaceHolder surfaceHolder = surfaceView.getHolder();

                if (mHasSurface) {
                    // The activity was paused but not stopped, so the surface still exists. Therefore
                    // surfaceCreated() won't be called, so init the camera here.
                    initCamera(surfaceHolder);
                } else {
                    // Install the callback and wait for surfaceCreated() to init the camera.
                    surfaceHolder.addCallback(that);
                }

                mBeepManager.updatePrefs();

                mInactivityTimer.onResume();

                return convertView;
            }

            @Override
            public int getPosition(Object o) {
                return 1;
            }
        });
        csr.activate();
        setContentView(csr);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Context ctx = this;
        final Activity activity = this;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ctx, MainActivity.class);
                        intent.putExtra("qr_type", "-1");
                        intent.putExtra("qr_data", "");
                        startActivityForResult(intent, 2);
                    }
                });
            }
        }, 60000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK)
                finish();
        }
    }

    @Override
    protected void onPause() {
        mTimer.cancel();

        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        mInactivityTimer.onPause();
        mCameraManager.closeDriver();
        if (!mHasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mInactivityTimer.shutdown();
        super.onDestroy();
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (mHandler == null) {
            Log.w(TAG, "mHandler == null");
            mSavedResultToShow = result;
        } else {
            if (result != null) {
                mSavedResultToShow = result;
                Log.w(TAG, "mSavedResultToShow = result;");
            }
            if (mSavedResultToShow != null) {
                Log.w(TAG, "Message.obtain");
                Message message = Message.obtain(mHandler,
                        1, mSavedResultToShow);
                mHandler.sendMessage(message);
            }
            mSavedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG,
                    "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult
     *            The contents of the barcode.
     * @param scaleFactor
     *            amount by which thumbnail was scaled
     * @param barcode
     *            A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        mInactivityTimer.onActivity();

        mBeepManager.playBeepSoundAndVibrate();

        handleDecodeInternally(rawResult, barcode);
    }

    // Put up our own UI for how to handle the decoded contents.
    private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
        String parsedResult = ResultParser.parseResult(rawResult).toString();
        List<String> ar;

        parsedResult = parsedResult.replaceAll("[^-?0-9]+", " ");
        ar = Arrays.asList(parsedResult.trim().split(" "));

        if (ar.size() > 0){
            mViewfinderView.resultText = getLocationDescription(ar.get(0));
        } else {
            Log.d(TAG, "No valid Id was found in the QR Code.");
        }

        this.onPause();

        SurfaceView surfaceView = (SurfaceView) tmpConvertView.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        if (mHasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(CaptureActivity.this);
        }

        this.onResume();

        /*Intent intent = new Intent(this, MainGlassActivity.class);
        intent.putExtra("qr_type", parsedResult.getType().toString());
        intent.putExtra("qr_data", parsedResult.toString());
        startActivityForResult(intent, 2);*/
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG,
                    "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (mHandler == null) {
                mHandler = new CaptureActivityHandler(this, null, mDecodeHints,
                        null, mCameraManager);
            }

            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException | InterruptedException e) {
            Log.w(TAG, e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("qr_type", "-2");
        intent.putExtra("qr_data", "");
        startActivityForResult(intent, 2);
    }

    public void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }

    private String getLocationDescription(String foundPlaceId) {
        String sDescription = "";
        switch (foundPlaceId) {
            case "1":
                sDescription = "Verlasse das Zimmer und gehe nach links";
                break;
            case "2":
                sDescription = "Gehe durch die Glastür";
                break;
            case "3":
                sDescription = "Gehe geradeaus";
                break;
            case "4":
                sDescription = "Gehe durch die Glastür";
                break;
            case "5":
                sDescription = "Folge dem Flur und gehe nach rechts";
                break;
            case "6":
                sDescription = "Halte dich auf der rechten Seite";
                break;
            case "7":
                sDescription = "Gehe durch die Notfalltür";
                break;
            default:
                sDescription = "Dieser QR-Code ist keinem Ort zugewiesen...";
                break;
        }
        return sDescription;
    }
}

