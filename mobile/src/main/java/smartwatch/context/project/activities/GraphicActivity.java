package smartwatch.context.project.activities;

import android.app.Activity;
import android.os.Bundle;

import smartwatch.context.project.graphic.CustomDrawableView;

// TODO: 15.04.16 Delete this class
public class GraphicActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomDrawableView mCustomDrawableView = new CustomDrawableView(this);

        setContentView(mCustomDrawableView);

    }

}
