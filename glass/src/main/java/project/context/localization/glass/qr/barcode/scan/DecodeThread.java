/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package project.context.localization.glass.qr.barcode.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
//import com.jaxbot.glass.barcode.migrated.DecodeFormatManager;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import project.context.localization.glass.qr.barcode.migrated.DecodeFormatManager;

/**
 * This thread does all the heavy lifting of decoding the images.
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
 */
final class DecodeThread extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";
  public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

  private final CaptureActivity activity;
  private final Map<DecodeHintType,Object> hints;
  private Handler handler;
  private final CountDownLatch handlerInitLatch;

  DecodeThread(CaptureActivity activity,
               Collection<BarcodeFormat> decodeFormats,
               Map<DecodeHintType,?> baseHints,
               String characterSet,
               ResultPointCallback resultPointCallback) {

    this.activity = activity;
    handlerInitLatch = new CountDownLatch(1);

    hints = new EnumMap<>(DecodeHintType.class);
    if (baseHints != null) {
      hints.putAll(baseHints);
    }

    // The prefs can't change while the thread is running, so pick them up once here.
    if (decodeFormats == null || decodeFormats.isEmpty()) {
      decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
    }

    decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    if (characterSet != null) {
      hints.put(DecodeHintType.CHARACTER_SET, characterSet);
    }
    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    Log.i("DecodeThread", "Hints: " + hints);
  }

  Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new DecodeHandler(activity, hints);
    handlerInitLatch.countDown();
    Looper.loop();
  }

}
