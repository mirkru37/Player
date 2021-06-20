package com.dev.umieplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class PlayButtonBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

    public PlayButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull RelativeLayout child, @NonNull View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull RelativeLayout child, @NonNull View dependency) {
        child.setY(dependency.getY());

        return false;
    }
}
