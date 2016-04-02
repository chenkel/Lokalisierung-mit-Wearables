package smartwatch.context.project.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jan on 02.04.16.
 */
public class CustomDrawableView extends View {

    private ShapeDrawable mDrawable;

    private int x;
    private int y;

    private int width;
    private int height;

    public CustomDrawableView(Context context, int x, int y, int width, int height) {
        super(context);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(0xff74AC23);
        mDrawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

}
