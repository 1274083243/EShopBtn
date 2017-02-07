package com.ike.eshopbtn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
作者：ike
时间：2017/2/6 11:42
功能描述：仿e了么添加购物车按钮
**/

public class EShopView extends View {
    private String Tag="EShopView";
    private Paint mAddPaint,mDelePaint,mTextPaint,bgPaint;
    private Path mAddPath,mDelePath;
    private int defaultWidth=5,defaltRadius=50,defaultLineHeight=5;
    private int mLeft,mTop,mWidth,mHeight;
    private int circleDis=200;
    private ValueAnimator deleAnimator;
    private ValueAnimator addAnimator;
    private ValueAnimator expendAnimator;
    private ValueAnimator unfoldAnimator;
    private Region dele_region,add_region;
    private int default_duration=500;
    private float del_persent;//删除动画完成的百分比
    private float expend_persent;//伸展动画的完成百分比
    private boolean isShowNotice;
    private int goodsNum=1;//商品数量
    /**
     * 圆角值(xml)
     */
    protected int mHintBgRoundValue=50;//圆角矩形值
    public EShopView(Context context) {
        this(context,null);
    }

    public EShopView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EShopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initDelAnimator();
    }

    /**
     * 初始化删除回滚动画
     */
    private void initDelAnimator() {
        deleAnimator=ValueAnimator.ofFloat(0,1);
        deleAnimator.setDuration(default_duration);
        deleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                del_persent= (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        deleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShowNotice=true;
               unfoldAnimator.start();
            }
        });
        addAnimator=ValueAnimator.ofFloat(1,0);
        addAnimator.setDuration(default_duration);
        addAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                del_persent= (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        //提示收起动画
        expendAnimator=ValueAnimator.ofFloat(0,1);
        expendAnimator.setDuration(default_duration);
        expendAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                expend_persent= (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        expendAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                addAnimator.start();
                isShowNotice=false;
            }
        });
        //提示展开动画
        unfoldAnimator=ValueAnimator.ofFloat(1,0);
        unfoldAnimator.setDuration(default_duration);
        unfoldAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                expend_persent= (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        isShowNotice=goodsNum==1?true:false;
    }

    public void init(){
        bgPaint=new Paint();
        bgPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTextPaint=new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(32);
        mTextPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        mAddPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mDelePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mAddPaint.setStyle(Paint.Style.FILL);
        mDelePaint.setStyle(Paint.Style.FILL);
        mAddPaint.setColor(getResources().getColor(R.color.colorAccent));
        mDelePaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        mAddPaint.setStrokeWidth(defaultWidth);
        mDelePaint.setStrokeWidth(defaultWidth);
        mAddPath=new Path();
        mDelePath=new Path();
        dele_region=new Region();
        add_region=new Region();
        mFontMetrics = mTextPaint.getFontMetrics();
    }
    protected Paint.FontMetrics mFontMetrics;
    @Override
    protected void onDraw(Canvas canvas) {
        //绘制提示文本背景
        if (isShowNotice){
            mAddPaint.setStyle(Paint.Style.FILL);
            RectF rect=new RectF(mLeft+(mWidth-defaultWidth-mLeft-2*defaltRadius)*expend_persent,mTop,mWidth-defaultWidth,mTop+defaltRadius*2);
            canvas.drawRoundRect(rect,mHintBgRoundValue,mHintBgRoundValue,bgPaint);
            //绘制提示文本
            // 计算Baseline绘制的起点X轴坐标
            String mHintText="加入购物车";
            int baseX = (int) (mWidth/2 - mTextPaint.measureText(mHintText) / 2);
            // 计算Baseline绘制的Y坐标
            int baseY = (int) ((defaltRadius*2+ defaultWidth*2)/2 - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
            canvas.drawText(mHintText,baseX,baseY,mTextPaint);
        }else {
            mAddPaint.setStyle(Paint.Style.FILL);
            float animOffsetMax=mWidth-defaltRadius*2-defaultWidth*2-defaltRadius;
            //绘制删除按钮
            mDelePaint.setAlpha((int) (255 * (1 - del_persent)));
            mDelePath.reset();
            mDelePath.addCircle(del_persent*animOffsetMax+mLeft+defaltRadius,mTop+defaltRadius,defaltRadius, Path.Direction.CCW);
            dele_region.setPath(mDelePath,new Region(mLeft,mTop,mWidth-getPaddingLeft(),mHeight-getPaddingBottom()));
            canvas.drawPath(mDelePath,mDelePaint);
            mDelePaint.setStrokeWidth(defaultLineHeight);
            //执行旋转动画
            canvas.save();
            canvas.translate(animOffsetMax * del_persent + mLeft + defaltRadius, mTop + defaltRadius);
            canvas.rotate((int) (360 * (1 - del_persent)));
            canvas.drawLine(-defaltRadius / 2, 0,defaltRadius / 2,0, mDelePaint);
            //绘制添加按钮
            canvas.restore();
            //绘制中间的商品数目
            canvas.save();
           // mTextPaint.setAlpha((int) (255 * (1 - del_persent)));
            int circleDis=mWidth-(defaltRadius*2+defaultWidth)*2;
            canvas.translate(del_persent*(circleDis/2-mTextPaint.measureText(goodsNum+"")/2+defaltRadius),0);
            canvas.rotate((int) (360 *del_persent),circleDis / 2 - mTextPaint.measureText(goodsNum + "") / 2 + mLeft + defaltRadius * 2,mTop+defaltRadius);
            canvas.drawText(goodsNum + "",  circleDis / 2 - mTextPaint.measureText(goodsNum + "") / 2 + mLeft + defaltRadius * 2, mTop + defaltRadius - (mFontMetrics.top + mFontMetrics.bottom) / 2, mTextPaint);
            canvas.restore();
            mAddPath.reset();
            int left=mWidth-defaltRadius*2-defaultWidth;
            mAddPath.addCircle(left+defaltRadius,mTop+defaltRadius,defaltRadius, Path.Direction.CCW);
            add_region.setPath(mAddPath,new Region(mLeft,mTop,mWidth-getPaddingLeft(),mHeight-getPaddingBottom()));
            canvas.drawPath(mAddPath,mAddPaint);
            canvas.drawLine(left+defaltRadius/2,mTop+defaltRadius,left+defaltRadius/2+defaltRadius,mTop + defaltRadius,mTextPaint);
            canvas.drawLine(left+defaltRadius,mTop+defaltRadius/2,left+defaltRadius,mTop + defaltRadius/2+defaltRadius,mTextPaint);
        }


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mLeft=getPaddingLeft()+defaultWidth;
        mTop=getPaddingTop()+defaultWidth;
        mWidth=w;
        mHeight=h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (dele_region.contains((int)event.getX(),(int)event.getY())){
                    cancelAllAnim();
                   deleAnimator.start();
                }
                else if (add_region.contains((int)event.getX(),(int)event.getY())){
                    cancelAllAnim();
                    addAnimator.start();
                }else {
                    expendAnimator.start();
                }
                break;
        }
        return super.onTouchEvent(event);
    }
    /**
     * 暂停所有动画
     */
    private void cancelAllAnim() {
        if (deleAnimator != null && deleAnimator.isRunning()) {
            deleAnimator.cancel();
        }
        if (addAnimator != null && addAnimator.isRunning()) {
            addAnimator.cancel();
        }
    }
}
