package project.context.localization.wear.list;

import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import project.context.localization.wear.R;

/**
 * The WearableListItemLayout defines a Layout Implementation for List Items
 *
 * Also see: http://developer.android.com/training/wearables/ui/lists.html#layout-impl
 */
public class WearableListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private static final float NO_ALPHA = 1f, PARTIAL_ALPHA = 0.40f;
    private static final float NO_X_TRANSLATION = 0f, X_TRANSLATION = 20f;
    private final int mUnselectedCircleColor, mSelectedCircleColor;
    private final float mBigCircleRadius;
    private final float mSmallCircleRadius;
    private CircledImageView mCircle;

    /**
     * Instantiates a new Wearable list item layout.
     *
     * @param context the context of the application
     */
    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Wearable list item layout.
     *
     * @param context the context of the application
     * @param attrs   the custom attributes
     */
    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Wearable list item layout.
     *
     * @param context  the context of the application
     * @param attrs    the custom attributes
     * @param defStyle the user-defined styles
     */
    public WearableListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mUnselectedCircleColor = Color.parseColor("#434343");
        mSelectedCircleColor = Color.parseColor("#434343");
        mSmallCircleRadius = getResources().
                getDimensionPixelSize(R.dimen.small_circle_radius);
        mBigCircleRadius = getResources().
                getDimensionPixelSize(R.dimen.big_circle_radius);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (CircledImageView) findViewById(R.id.circle);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            animate().alpha(NO_ALPHA).translationX(X_TRANSLATION).start();
        }
        mCircle.setCircleColor(mSelectedCircleColor);
        mCircle.setCircleRadius(mBigCircleRadius);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            animate().alpha(PARTIAL_ALPHA).translationX(NO_X_TRANSLATION).start();
        }
        mCircle.setCircleColor(mUnselectedCircleColor);
        mCircle.setCircleRadius(mSmallCircleRadius);
    }
}
