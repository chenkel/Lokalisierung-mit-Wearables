package smartwatch.context.project.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;

import smartwatch.context.project.R;
import smartwatch.context.project.graphic.CustomDrawableView;

/**
 * Created by jan on 02.04.16.
 */
public class GraphicActivity extends Activity{

    CustomDrawableView mCustomDrawableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomDrawableView = new CustomDrawableView(this, 50, 50, 500, 300);

        setContentView(mCustomDrawableView);

    }

}
