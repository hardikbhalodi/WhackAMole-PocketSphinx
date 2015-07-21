/*
 * 
 */
package com.welkinlan.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageTextItem.
 */
public class ImageTextItem extends RelativeLayout{
	 
 	/**
 	 * Instantiates a new image text item.
 	 *
 	 * @param context the context
 	 * @param attrs the attrs
 	 * @param defStyle the def style
 	 */
 	public ImageTextItem(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	    }

	    /**
    	 * Instantiates a new image text item.
    	 *
    	 * @param context the context
    	 * @param attrs the attrs
    	 */
    	public ImageTextItem(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    /**
    	 * Instantiates a new image text item.
    	 *
    	 * @param context the context
    	 */
    	public ImageTextItem(Context context) {
	        super(context);
	    }

	    /* (non-Javadoc)
    	 * @see android.widget.RelativeLayout#onMeasure(int, int)
    	 */
    	@SuppressWarnings("unused")
	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        // For simple implementation, or internal size is always 0.
	        // We depend on the container to specify the layout size of
	        // our view. We can't really know what it is since we will be
	        // adding and removing different arbitrary views and do not
	        // want the layout to change as this happens.
	        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

	        // Children are just made to fill our space.
	        int childWidthSize = getMeasuredWidth();
	        int childHeightSize = getMeasuredHeight();
	        //高度和宽度一样
	        //heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    }
}
