package smartwatch.context.project.activities;

import android.app.Activity;
import android.os.Bundle;

import smartwatch.context.project.graphic.CustomDrawableView;


public class GraphicActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomDrawableView mCustomDrawableView = new CustomDrawableView(this);

        setContentView(mCustomDrawableView);

    }

}
