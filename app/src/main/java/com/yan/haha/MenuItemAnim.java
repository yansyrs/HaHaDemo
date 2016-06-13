package com.yan.haha;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class MenuItemAnim extends Animation {

    @Override   // 获取目标对象的宽高和容器的宽高。
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {

        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        // interpolatedTime 从0到1，等动画执行完毕后就会变成1。t 变化对象。
        //        System.out.println(interpolatedTime);
        //        t.setAlpha(interpolatedTime);
        // interpolatedTime 补间动画
        //        t.getMatrix().setTranslate(200*interpolatedTime, 200*interpolatedTime);
        // 左右摇摆动画（*20运动速度周期  *50表示左右摇摆幅度增大。）
        t.getMatrix().setTranslate((float) (Math.sin(interpolatedTime*20)*20), 0);

        super.applyTransformation(interpolatedTime, t);
    }

}