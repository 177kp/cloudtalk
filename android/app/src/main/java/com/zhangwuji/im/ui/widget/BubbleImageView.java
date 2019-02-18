package com.zhangwuji.im.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.zhangwuji.im.R;
import com.zhangwuji.im.utils.ImageLoaderUtil;

import java.lang.ref.WeakReference;


/**
 * Created by zhujian on 15/2/13.
 */
@SuppressLint("AppCompatCustomView")
public class BubbleImageView extends ImageView {
    /**
     * 图片设置相关
     */
    protected String imageUrl = null;
    protected boolean isAttachedOnWindow = false;
    protected int defaultImageRes = R.drawable.rc_image_error;

    protected ImageLoaddingCallback imageLoaddingCallback;
    private boolean mHasMask;
    private WeakReference<Bitmap> mWeakBitmap;
    private WeakReference<Bitmap> mShardWeakBitmap;

    public BubbleImageView(Context context) {
        super(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!this.isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
            this.mHasMask = a.getBoolean(R.styleable.AsyncImageView_RCMask, false);
        }
    }

    /* 图片设置相关 */
    public void setImageLoaddingCallback(ImageLoaddingCallback callback) {
        this.imageLoaddingCallback = callback;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mHasMask) {
            Bitmap bitmap = this.mWeakBitmap == null ? null : (Bitmap)this.mWeakBitmap.get();
            Drawable drawable = this.getDrawable();
            CTMessageFrameLayout parent = (CTMessageFrameLayout)this.getParent();
            Drawable background = parent.getBackgroundDrawable();
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, 0.0F, 0.0F, (Paint)null);
                this.getShardImage(background, bitmap, canvas);
            } else {
                int width = this.getWidth();
                int height = this.getHeight();
                if (width <= 0 || height <= 0) {
                    return;
                }

                try {
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                } catch (OutOfMemoryError var11) {
                    var11.printStackTrace();
                    System.gc();
                }

                if (bitmap != null) {
                    Canvas rCanvas = new Canvas(bitmap);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, width, height);
                        drawable.draw(rCanvas);
                        if (background != null && background instanceof NinePatchDrawable) {
                            NinePatchDrawable patchDrawable = (NinePatchDrawable)background;
                            patchDrawable.setBounds(0, 0, width, height);
                            Paint maskPaint = patchDrawable.getPaint();
                            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                            patchDrawable.draw(rCanvas);
                        }

                        this.mWeakBitmap = new WeakReference(bitmap);
                    }

                    canvas.drawBitmap(bitmap, 0.0F, 0.0F, (Paint)null);
                    this.getShardImage(background, bitmap, canvas);
                }
            }
        } else {
            super.onDraw(canvas);
        }
    }

    private void getShardImage(Drawable drawable_bg, Bitmap bp, Canvas canvas) {
        int width = bp.getWidth();
        int height = bp.getHeight();
        Bitmap bitmap = this.mShardWeakBitmap == null ? null : (Bitmap)this.mShardWeakBitmap.get();
        if (width > 0 && height > 0) {
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, 0.0F, 0.0F, (Paint)null);
            } else {
                try {
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                } catch (OutOfMemoryError var14) {
                    var14.printStackTrace();
                    System.gc();
                }

                if (bitmap != null) {
                    Canvas rCanvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    Rect rect = new Rect(0, 0, width, height);
                    Rect rectF = new Rect(1, 1, width - 1, height - 1);
                    BitmapDrawable drawable_in = new BitmapDrawable(bp);
                    drawable_in.setBounds(rectF);
                    drawable_in.draw(rCanvas);
                    if (drawable_bg instanceof NinePatchDrawable) {
                        NinePatchDrawable patchDrawable = (NinePatchDrawable)drawable_bg;
                        patchDrawable.setBounds(rect);
                        Paint maskPaint = patchDrawable.getPaint();
                        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                        patchDrawable.draw(rCanvas);
                    }

                    this.mShardWeakBitmap = new WeakReference(bitmap);
                    canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint);
                }
            }

        }
    }

    public void setDefaultImageRes(int defaultImageRes) {
        this.defaultImageRes = defaultImageRes;

    }

    public void setImageUrl(final String url) {
        this.imageUrl = url;
        if (isAttachedOnWindow) {
            final BubbleImageView view = this;
            if (!TextUtils.isEmpty(this.imageUrl)) {
                ImageAware imageAware = new ImageViewAware(this, false);
                ImageLoaderUtil.getImageLoaderInstance().displayImage(this.imageUrl, imageAware, new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .showImageOnLoading(defaultImageRes)
                        .showImageOnFail(defaultImageRes)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .delayBeforeLoading(100)
                        .build(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (imageLoaddingCallback != null) {

                            String cachePath = ImageLoaderUtil.getImageLoaderInstance().getDiskCache().get(imageUri).getPath();//这个路径其实已不再更新
                            imageLoaddingCallback.onLoadingComplete(cachePath, view, loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingStarted(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingCanceled(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingFailed(imageUri, view);
                        }
                    }
                });
            }
        } else {
            this.setImageResource(defaultImageRes);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedOnWindow = true;
        setImageUrl(this.imageUrl);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedOnWindow = false;
        ImageLoaderUtil.getImageLoaderInstance().cancelDisplayTask(this);
    }

    public interface ImageLoaddingCallback {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

        public void onLoadingStarted(String imageUri, View view);

        public void onLoadingCanceled(String imageUri, View view);

        public void onLoadingFailed(String imageUri, View view);
    }


}
