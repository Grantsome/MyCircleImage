package com.grantsome.mycircleimage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by tom on 2017/3/12.
 */

public class CircleImageView extends ImageView {

    private Bitmap mBitmap;

    private BitmapShader mBitmapShader;

    private RectF mBorderRectF = new RectF();

    private RectF mDrawableRectF = new RectF();

    private Matrix mShaderMatrix = new Matrix();

    private Paint mPaint = new Paint();

    private Paint mBorderPaint = new Paint();

    private int mBorderColor;

    private int mBorderWidth;

    private float mDrawableRadius;

    private float mBorderRadius;

    public CircleImageView(Context context) {
        super(context);
        initPaint();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleImageView,0,0);
        mBorderColor = typedArray.getColor(R.styleable.CircleImageView_border_color, Color.WHITE);
        mBorderWidth = typedArray.getColor(R.styleable.CircleImageView_border_width,0);
        typedArray.recycle();
        initPaint();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleImageView,0,0);
        mBorderColor = typedArray.getColor(R.styleable.CircleImageView_border_color, Color.WHITE);
        mBorderWidth = typedArray.getColor(R.styleable.CircleImageView_border_width,0);
        typedArray.recycle();
        initPaint();
    }

    private Bitmap getBitmap(){
        Drawable mDrawable = getDrawable();
        if(mDrawable == null){
            return null;
        }
        if(mDrawable instanceof BitmapDrawable){
            return ((BitmapDrawable) mDrawable).getBitmap();
        }
        return null;
    }

    private RectF dealWithPadding(){
        int width = getWidth()-getPaddingLeft()-getPaddingRight();
        int height = getHeight()-getPaddingTop()-getPaddingBottom();
        int side = Math.min(width,height);
        float left = getPaddingLeft()+(side-width)/2.0f;
        float top = getPaddingTop()+(side-height)/2.0f;
        RectF rectF = new RectF(left,top,left+side,top+side);
        return rectF;
    }

    private void setup(){
        if(mBitmap == null){
            mBitmap = getBitmap();
            invalidate();
        }
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        initPaint();

        setRadius();
        setShaderMatrix();
        invalidate();
    }

    private void initPaint(){
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(mBitmapShader);

        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    private void setRadius(){
        mBorderRectF.set(dealWithPadding());
        mBorderRadius = Math.min((mBorderRectF.width()-mBorderWidth)/2.0f,(mBorderRectF.height()-mBorderWidth)/2.0f);
        mDrawableRectF.set(mBorderRectF);
        mDrawableRadius = Math.min(mDrawableRectF.width()/2.0f,mDrawableRectF.height()/2.0f);
    }

    private void setShaderMatrix(){
        float scale;
        float x = 0;
        float y = 0;

        mShaderMatrix.set(null);
        if(mBitmap.getWidth()*mDrawableRectF.height()>mDrawableRectF.width()*mBitmap.getHeight()){
            scale = mDrawableRectF.height()/(float) mBitmap.getHeight();
            x = (mDrawableRectF.width()-mBitmap.getWidth()*scale)*0.5f;
        }else {
            scale = mDrawableRectF.width()/(float) mBitmap.getWidth();
            y = (mDrawableRectF.height()-mBitmap.getHeight()*scale)*0.5f;
        }
        mShaderMatrix.setScale(scale,scale);
        mShaderMatrix.postTranslate((int) (x+0.5f) + mDrawableRectF.left,(int) (y+0.5f) +mDrawableRectF.top);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas){
        setup();
        canvas.drawCircle(mBorderRectF.centerX(),mBorderRectF.centerY(),mBorderRadius,mBorderPaint);
        canvas.drawCircle(mDrawableRectF.centerX(),mDrawableRectF.centerY(),mDrawableRadius,mPaint);
    }

}
