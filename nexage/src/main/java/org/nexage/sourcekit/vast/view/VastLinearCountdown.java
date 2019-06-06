package org.nexage.sourcekit.vast.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import org.nexage.sourcekit.util.Assets;

public class VastLinearCountdown extends View {
    private float lineLength;
    private float lineWidth;
    private int lineColor = Assets.mainAssetsColor;

    private float percent;

    public Handler uiThread = new Handler();

    Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);

    public VastLinearCountdown(Context context) {
        super(context);
        initializeAttributes();
    }

    public VastLinearCountdown(Context context, int color ) {
        super(context);
        lineColor = color;
        initializeAttributes();
    }

    public VastLinearCountdown(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        lineLength = w;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineColor);
        drawLineLoading(paint, canvas);
    }

    private void initializeAttributes(){
        lineWidth = 15f;
        percent = 0;
    }


    private void drawLineLoading(Paint paint, Canvas canvas){
        float x = lineLength * percent / 100;
        canvas.drawLine(0, 0, x, 0, paint);
    }

    public void changePercentage(float percent){
        this.percent = percent;
        uiThread.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}