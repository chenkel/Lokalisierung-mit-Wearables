package smartwatch.context.project.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;

// TODO: 15.04.16 Delete this class
public class CustomDrawableView extends View {

    private ShapeDrawable mDrawable;
    private Rect rectangle1;
    private Rect rectangle2;

    private Paint paint;

    private int x;
    private int y;

    private int width;
    private int height;

    public CustomDrawableView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        /* Todo: Avoid object allocations during draw/layout operations (preallocate and reuse instead) */
        LocationCoordinates place1 = new LocationCoordinates("place1", 20, 20);
        LocationCoordinates place2 = new LocationCoordinates("place2", 500, 20);
        LocationCoordinates place3 = new LocationCoordinates("place3", 260, 300);


        Rect ourRect = new Rect();
        ourRect.set(0, 0, canvas.getWidth(), canvas.getHeight() / 2);


        Paint blue = new Paint();
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);

        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);

        Paint green = new Paint();
        green.setColor(Color.GREEN);
        green.setStyle(Paint.Style.FILL);


        canvas.drawRect(ourRect, blue);
        canvas.drawCircle(place1.getXcoord(), place1.getYcoord(), 20, red);
        canvas.drawCircle(place2.getXcoord(), place2.getYcoord(), 20, red);
        canvas.drawCircle(place3.getXcoord(), place3.getYcoord(), 20, green);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                for (int j = 0; j <= 60; j++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } //wait one second
                }
            }
        });
    }

}
