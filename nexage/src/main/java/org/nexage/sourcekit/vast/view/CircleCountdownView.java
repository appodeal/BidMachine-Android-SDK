package org.nexage.sourcekit.vast.view;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import org.nexage.sourcekit.util.Assets;


public class CircleCountdownView extends View {
    private float circleCenterPointX;
    private float circleCenterPointY;
    private int arcLoadingBackgroundColor = Assets.backgroundColor;
    private float arcLoadingStrokeWidth;
    private float arcLoadingStartAngle;
    private int mainColor = Assets.mainAssetsColor;
    private int innerSize;
    private int paddingInContainer;
    private Bitmap bitmap;
    private Bitmap resource;

    private int percent;
    private int remainingTime;

    public Handler uiThread = new Handler();

    public CircleCountdownView(Context context) {
        super(context);
        initializeAttributes();
    }

    public CircleCountdownView(Context context, int color, int backgroundColor) {
        super(context);
        mainColor = color;
        arcLoadingBackgroundColor = backgroundColor;
        initializeAttributes();
    }

    public CircleCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        circleCenterPointX = w / 2;
        circleCenterPointY = h / 2;
        paddingInContainer = Math.max(w, h) / 4;
        innerSize = (int) ((circleCenterPointX - paddingInContainer) * Math.sqrt(2));
        if (resource != null) {
            this.bitmap = Bitmap.createScaledBitmap(resource, innerSize, innerSize, true);
            fillBitmap();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (remainingTime != 0 || bitmap != null) {
            drawArcLoading(canvas);
            if (bitmap != null) {
                drawBitmap(canvas);
            } else {
                drawText(canvas);
            }
        }
    }

    private void initializeAttributes() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        circleCenterPointX = 54f;
        circleCenterPointY = 54f;
        arcLoadingStrokeWidth = 5f;
        percent = 100;
        arcLoadingStartAngle = 270f;
    }


    private void drawArcLoading(Canvas canvas) {
        float sweep = 360 - 360 * percent * 0.01f;
        RectF box = new RectF(paddingInContainer, paddingInContainer, circleCenterPointX * 2f - paddingInContainer, circleCenterPointX * 2f - paddingInContainer);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(arcLoadingBackgroundColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(box, 0, 360, false, paint);

        paint.setColor(mainColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(arcLoadingStrokeWidth);
        canvas.drawArc(box, arcLoadingStartAngle, sweep, false, paint);

    }

    private void drawBitmap(Canvas canvas) {
        int positionX = (int) (circleCenterPointX - innerSize / 2);
        int positionY = (int) (circleCenterPointY - innerSize / 2);

        Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setStyle(Paint.Style.FILL);
        bitmapPaint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, positionX, positionY, bitmapPaint);
    }

    private void drawText(Canvas canvas) {
        String text = String.valueOf(remainingTime);
        Paint textPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(mainColor);
        textPaint.setTextSize(innerSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int positionX = (getMeasuredWidth() / 2);
        int positionY = (int) ((getMeasuredHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        canvas.drawText(text, positionX, positionY, textPaint);
    }

    public void changePercentage(int percent, int remainingTime) {
        if (bitmap == null || percent == 100) {
            this.percent = percent;
            this.remainingTime = remainingTime;
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

    public void setImage(Bitmap bitmap) {
        if (bitmap != null && innerSize > 0) {
            this.resource = bitmap;
            this.bitmap = Bitmap.createScaledBitmap(bitmap, innerSize, innerSize, true);
            fillBitmap();
            percent = 100;
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        } else {
            this.resource = bitmap;
        }
    }

    private void fillBitmap() {
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(mainColor, 0);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(this.bitmap);
        canvas.drawBitmap(this.bitmap, 0, 0, paint);
    }
}