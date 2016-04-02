package smartwatch.context.project.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

/**
 * Created by jan on 02.04.16.
 */
public class CustomDrawableView extends View {

    private ShapeDrawable mDrawable;

    public CustomDrawableView(Context context) {
        super(context);

        int x = 50;
        int y = 50;
        int width = 300;
        int height = 500;

        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(0xff74AC23);
        mDrawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

}
