/*
 * Copyright (C) 2010 ZXing authors
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

package project.context.localization.glass.qr.barcode.migrated;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;

import com.google.zxing.Result;

/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power.
 *
 * * ---------------------------------------------------------------------------------------------
 * <p>The code was imported from the following repository</p> https://github.com/jaxbot/glass-qrlens.
 *
 * <p>Some changes that were made will be explained in the following section:</p>
 * <ul>
 *      <li>
 *          In -- {@link project.context.localization.glass.qr.barcode.scan.CaptureActivity#onResume()} --
 *          <p>Time out of QR Code Scanner extended</p>
 *          <p>from 15 seconds to 60 seconds</p>
 *
 *     </li>
 *     <li>
 *         In -- {@link project.context.localization.glass.qr.barcode.scan.CaptureActivity#handleDecode(Result, Bitmap, float)} --
 *         <p>Play Beep sound every time</p>
 *     </li>
 *     <li>
 *         In -- {@link project.context.localization.glass.qr.barcode.scan.CaptureActivity#handleDecodeInternally(Result, Bitmap)} --
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
 *
 */
public final class InactivityTimer {

    private static final String TAG = InactivityTimer.class.getSimpleName();

    private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

    private final Activity activity;
    private final BroadcastReceiver powerStatusReceiver;
    private boolean registered;
    private AsyncTask<Object, Object, Object> inactivityTask;

    public InactivityTimer(Activity activity) {
        this.activity = activity;
        powerStatusReceiver = new PowerStatusReceiver();
        registered = false;
        onActivity();
    }

    public synchronized void onActivity() {
        cancel();
        inactivityTask = new InactivityAsyncTask();
        inactivityTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public synchronized void onPause() {
        cancel();
        if (registered) {
            activity.unregisterReceiver(powerStatusReceiver);
            registered = false;
        } else {
            Log.w(TAG, "PowerStatusReceiver was never registered?");
        }
    }

    public synchronized void onResume() {
        if (registered) {
            Log.w(TAG, "PowerStatusReceiver was already registered?");
        } else {
            activity.registerReceiver(powerStatusReceiver, new IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED));
            registered = true;
        }
        onActivity();
    }

    private synchronized void cancel() {
        AsyncTask<?, ?, ?> task = inactivityTask;
        if (task != null) {
            task.cancel(true);
            inactivityTask = null;
        }
    }

    public void shutdown() {
        cancel();
    }

    private final class PowerStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // 0 indicates that we're on battery
                boolean onBatteryNow = intent.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1) <= 0;
                if (onBatteryNow) {
                    InactivityTimer.this.onActivity();
                } else {
                    InactivityTimer.this.cancel();
                }
            }
        }
    }

    private final class InactivityAsyncTask extends
            AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... objects) {
            try {
                Thread.sleep(INACTIVITY_DELAY_MS);
                Log.i(TAG, "Finishing activity due to inactivity");
                activity.finish();
            } catch (InterruptedException e) {
                // continue without killing
            }
            return null;
        }
    }

}
