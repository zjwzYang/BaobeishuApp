package com.microbookcase.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.andea.microbook.R;

/**
 * Created on 2020/5/20 13:01
 * 箭头.
 *
 * @author yj
 * @org 浙江房超信息科技有限公司
 */
public class ArrowView extends View {

    private Paint paint;
    private int width;
    private int height;

    public ArrowView(Context context) {
        this(context, null);
    }

    public ArrowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ArrowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);   //设置画笔抗锯齿
        paint.setStrokeWidth(2);    //设置线宽
        paint.setColor(ContextCompat.getColor(getContext(), R.color.color_arrow));  //设置线的颜色
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width = getWidth();
        height = getHeight();
        Log.i("12345678", "onDraw: " + height);
        Path path = new Path();
        path.moveTo(0, height / 2);
        path.lineTo(width / 4, height / 2);
        path.lineTo(width / 4, height / 10);
        path.lineTo(width * 3 / 4, height / 10);
        path.lineTo(width * 3 / 4, height / 2);
        path.lineTo(width, height / 2);
        path.lineTo(width / 2, height * 9 / 10);
        path.close();
        canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }
}
