package com.abiddarris.lanfileviewer.utils;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * {@link Drawable} helper class.
 *
 * @author Filipe Bezerra
 * @version 18/01/2016
 * @since 18/01/2016
 */
public class DrawableTinter {
    
    @NonNull Context mContext;
    @ColorRes private int mColor;
    private Drawable mDrawable;
    private Drawable mWrappedDrawable;

    public DrawableTinter(@NonNull Context context) {
        mContext = context;
    }

    public static DrawableTinter withContext(@NonNull Context context) {
        return new DrawableTinter(context);
    }

    public DrawableTinter withDrawable(@DrawableRes int drawableRes) {
        mDrawable = ContextCompat.getDrawable(mContext, drawableRes);
        return this;
    }

    public DrawableTinter withDrawable(@NonNull Drawable drawable) {
        mDrawable = drawable;
        return this;
    }

    public DrawableTinter withColor(@ColorRes int colorRes) {
        mColor = ContextCompat.getColor(mContext, colorRes);
        return this;
    }

    public DrawableTinter tint() {
        if (mDrawable == null) {
            throw new NullPointerException("É preciso informar o recurso drawable pelo método withDrawable()");
        }

        if (mColor == 0) {
            throw new IllegalStateException("É necessário informar a cor a ser definida pelo método withColor()");
        }

        mWrappedDrawable = mDrawable.mutate();
        mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable);
        DrawableCompat.setTint(mWrappedDrawable, mColor);
        DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);

        return this;
    }

    @SuppressWarnings("deprecation")
    public void applyToBackground(@NonNull View view) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(mWrappedDrawable);
        } else {
            view.setBackgroundDrawable(mWrappedDrawable);
        }
    }

    public void applyTo(@NonNull ImageView imageView) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        imageView.setImageDrawable(mWrappedDrawable);
    }

    public void applyTo(@NonNull MenuItem menuItem) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        menuItem.setIcon(mWrappedDrawable);
    }

    public Drawable get() {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        return mWrappedDrawable;
    }
}