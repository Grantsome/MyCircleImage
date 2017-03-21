package com.grantsome.circleimageview;

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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by tom on 2017/3/12.
 */

public class CircleImageView extends ImageView {

    private int mBitmapWidth;

    private int mBitmapHeight;

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
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleImageView,0,0);
        mBorderColor = typedArray.getColor(R.styleable.CircleImageView_border_color, Color.WHITE);
        mBorderWidth = typedArray.getColor(R.styleable.CircleImageView_border_width,0);
        typedArray.recycle();
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleImageView,0,0);
        mBorderColor = typedArray.getColor(R.styleable.CircleImageView_border_color, Color.WHITE);
        mBorderWidth = typedArray.getColor(R.styleable.CircleImageView_border_width,0);
        typedArray.recycle();
        init();
    }

    private void initBitmap(){
        mBitmap = getBitmap();
        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();
        init();
    }

    private Bitmap getBitmap(){
        Drawable mDrawable = getDrawable();
        if(mDrawable == null){
            return null;
        }
        if(mDrawable instanceof BitmapDrawable){
            return ((BitmapDrawable) mDrawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (mDrawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            mDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            mDrawable.draw(canvas);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private RectF dealWithPadding(){
        int width = getWidth()-getPaddingLeft()-getPaddingRight();
        int height = getHeight()-getPaddingTop()-getPaddingBottom();
        int side = Math.min(width,height);
        float left = getPaddingLeft()+(width-side)/2.0f;
        float top = getPaddingTop()+(height-side)/2.0f;
        RectF rectF = new RectF(left,top,left+side,top+side);
        return rectF;
    }

    private void setup(){
        if(mBitmap != null){
            mBitmap = getBitmap();
        }
        if(getWidth()==0&&getHeight()==0){
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        initPaint();
        setRadius();
        setShaderMatrix();
    }

    private void init(){
        super.setScaleType(ScaleType.CENTER_CROP);
        setup();
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
        if (mBorderWidth > 0) {
            mDrawableRectF.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f);
        }
        mDrawableRadius = Math.min(mDrawableRectF.width()/2.0f,mDrawableRectF.height()/2.0f);
    }

    private void setShaderMatrix(){
        float scale = 0;
        float x = 0;
        float y = 0;
        try {
        mShaderMatrix.set(null);
        if (mBitmapWidth * mDrawableRectF.height() > mDrawableRectF.width() * mBitmapHeight) {
            scale = mDrawableRectF.height() / (float) mBitmapHeight;
            x = (mDrawableRectF.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRectF.width() / (float) mBitmapWidth;
            y = (mDrawableRectF.height() - mBitmapHeight * scale) * 0.5f;
        }
        }catch (Exception e){
            e.printStackTrace();
            x = 1;
            y = 0;
            scale = 2;
        }
        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (x + 0.5f) + mDrawableRectF.left, (int) (y + 0.5f) + mDrawableRectF.top);
        mBitmapShader.setLocalMatrix(mShaderMatrix);

    }


    @Override
    protected void onDraw(Canvas canvas){
        setup();
        canvas.drawCircle(mDrawableRectF.centerX(),mDrawableRectF.centerY(),mDrawableRadius,mPaint);
        canvas.drawCircle(mBorderRectF.centerX(), mBorderRectF.centerY(), mBorderRadius, mBorderPaint);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initBitmap();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initBitmap();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        initBitmap();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initBitmap();
    }



}
