package project.context.localization.wear;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WearableListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private ImageView mCircle;
    private TextView mName;

    private final float mFadedTextAlpha;
    private final int mFadedCircleColor;
    private final int mChosenCircleColor;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = getResources()
                .getInteger(R.integer.action_text_faded_alpha) / 100f;
        mFadedCircleColor = getResources().getColor(R.color.grey);
        mChosenCircleColor = getResources().getColor(R.color.blue);
    }

    // Get references to the icon and text in the item layout definition
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // These are defined in the layout file for list items
        // (see next section)
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mName.setAlpha(1f);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        ((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
        mName.setAlpha(mFadedTextAlpha);
    }
}

///**
// * The type Wearable list item layout.
// */
//public class WearableListItemLayout extends LinearLayout
//        implements WearableListView.OnCenterProximityListener {
//
//    private static final float NO_ALPHA = 1f, PARTIAL_ALPHA = 0.40f;
//    private static final float NO_X_TRANSLATION = 0f, X_TRANSLATION = 20f;
//    private final int mUnselectedCircleColor, mSelectedCircleColor;
//    private final float mBigCircleRadius;
//    private final float mSmallCircleRadius;
//    private CircledImageView mCircle;
//
//    /**
//     * Instantiates a new Wearable list item layout.
//     *
//     * @param context the context
//     */
//    public WearableListItemLayout(Context context) {
//        this(context, null);
//    }
//
//    /**
//     * Instantiates a new Wearable list item layout.
//     *
//     * @param context the context
//     * @param attrs   the attrs
//     */
//    public WearableListItemLayout(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    /**
//     * Instantiates a new Wearable list item layout.
//     *
//     * @param context  the context
//     * @param attrs    the attrs
//     * @param defStyle the def style
//     */
//    public WearableListItemLayout(Context context, AttributeSet attrs,
//                                  int defStyle) {
//        super(context, attrs, defStyle);
//
//        mUnselectedCircleColor = Color.parseColor("#434343");
//        mSelectedCircleColor = Color.parseColor("#434343");
//        mSmallCircleRadius = getResources().
//                getDimensionPixelSize(R.dimen.small_circle_radius);
//        mBigCircleRadius = getResources().
//                getDimensionPixelSize(R.dimen.big_circle_radius);
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        mCircle = (CircledImageView) findViewById(R.id.circle);
//    }
//
//    @Override
//    public void onCenterPosition(boolean animate) {
//        if (animate) {
//            animate().alpha(NO_ALPHA).translationX(X_TRANSLATION).start();
//        }
//        mCircle.setCircleColor(mSelectedCircleColor);
//        mCircle.setCircleRadius(mBigCircleRadius);
//    }
//
//    @Override
//    public void onNonCenterPosition(boolean animate) {
//        if (animate) {
//            animate().alpha(PARTIAL_ALPHA).translationX(NO_X_TRANSLATION).start();
//        }
//        mCircle.setCircleColor(mUnselectedCircleColor);
//        mCircle.setCircleRadius(mSmallCircleRadius);
//    }
//}
