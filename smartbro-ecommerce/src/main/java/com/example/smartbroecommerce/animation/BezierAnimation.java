package com.example.smartbroecommerce.animation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.smartbro.delegates.SmartbroDelegate;
import com.example.smartbroecommerce.dimen.ScreenUtils;

public final class BezierAnimation {

    public static void addCart(SmartbroDelegate delegate, View start, View end,
                               ImageView target, BezierUtil.AnimationListener animationListener) {
        /* 起点 */
        final  int[] startXY = new int[2];
        start.getLocationInWindow(startXY);
        startXY[0] += start.getWidth() / 2;
        final int fx = startXY[0];
        final int fy = startXY[1];

        final  ViewGroup anim_mask_layout = BezierUtil.createAnimLayout(delegate.getProxyActivity());
        anim_mask_layout.addView(target);
        final View v = BezierUtil.addViewToAnimLayout(delegate.getContext(), target, startXY, true);
        if (v == null) {
            return;
        }
        /* 终点 */
        final int[] endXY = new int[2];
        end.getLocationInWindow(endXY);
        final int tx = endXY[0] + end.getWidth() / 2 - 48;
        final  int ty = endXY[1] + end.getHeight() / 2;
        /* 中点 */
        final  int mx = (tx + fx) / 2;
        final  int my = ScreenUtils.getScreenHeight(delegate.getContext()) / 10;
        BezierUtil.startAnimationForJd(v, 0, 0, fx, fy, mx, my, tx, ty, animationListener);
    }

}
