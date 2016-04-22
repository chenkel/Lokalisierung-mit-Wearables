/*
 * Copyright (C) 2010 ZXing authors
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

package project.context.localization.glass.qr.barcode.migrated;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.google.zxing.Result;

/**
 * Simple listener used to exit the app in a few cases.
 ** ---------------------------------------------------------------------------------------------
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
 * @author Sean Owen
 */
final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

  private final Activity activityToFinish;

  public FinishListener(Activity activityToFinish) {
    this.activityToFinish = activityToFinish;
  }

  @Override
  public void onCancel(DialogInterface dialogInterface) {
    run();
  }

  @Override
  public void onClick(DialogInterface dialogInterface, int i) {
    run();
  }

  private void run() {
    activityToFinish.finish();
  }

}
