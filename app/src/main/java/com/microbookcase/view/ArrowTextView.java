package com.microbookcase.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.andea.microbook.R;

/**
 * Created on 2020/5/7 13:35
 * .
 *
 * @author yj
 * @org 浙江趣看点科技有限公司
 */
public class ArrowTextView extends TextView {

    public ArrowTextView(Context context) {
        this(context, null);
    }

    public ArrowTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArrowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);   //设置画笔抗锯齿
        paint.setStrokeWidth(2);    //设置线宽
        paint.setColor(ContextCompat.getColor(getContext(), R.color.color_arrow));  //设置线的颜色

        int height = getHeight();   //获取View的高度
        int width = getWidth();     //获取View的宽度
        int radius = height / 11;
//        setPadding(radius, radius, radius, radius);

        //框定文本显示的区域
        canvas.drawRoundRect(new RectF(0, radius, width, height - radius)
                , 10, 10, paint);

        Path path = new Path();

        //以下是绘制文本的那个箭头
        path.moveTo(width / 2, height);// 三角形顶点
        path.lineTo(width / 2 - radius, height - radius);   //三角形左边的点
        path.lineTo(width / 2 + radius, height - radius);   //三角形右边的点

        path.close();
        canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }
}
