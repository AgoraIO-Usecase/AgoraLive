package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;

import com.faceunity.FURenderer;
import com.faceunity.authpack;
import com.faceunity.entity.Effect;

import io.agora.capture.video.camera.CameraVideoChannel;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.framework.modules.channels.VideoChannel;
import io.agora.framework.modules.processors.IPreprocessor;

public class PreprocessorFaceUnity implements IPreprocessor, CameraVideoChannel.OnCameraStateListener {
    public interface OnFirstFrameListener {
        void onFirstFrame();
    }

    public interface OnFuEffectBundleLoadedListener {
        void onFuEffectBundleLoaded();
    }

    public final static int MSG_EFFECT_BUNDLE_COMPLETE = 1;

    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();
    private final static int ANIMOJI_COUNT = 2;

    public static final float DEFAULT_BLUR_VALUE = 0.7f;
    public static final float DEFAULT_WHITEN_VALUE = 0.3f;
    public static final float DEFAULT_CHEEK_VALUE = 0f;
    public static final float DEFAULT_EYE_VALUE = 0.4f;

    private FURenderer mFURenderer;
    private Context mContext;
    private boolean mEnabled;
    private boolean mAuthenticated = true;

    private Effect mHaskiEffect;
    private Effect mGirlEffect;

    private int mEffectBackgroundHandle;
    private int mEffectAnimojiHaskiHandle;
    private int mEffectAnimojiGirlHandle;
    private int mBeautyHandle;

    private OnFuEffectBundleLoadedListener mBundleListener;
    private OnFirstFrameListener mFirstFrameListener;

    public PreprocessorFaceUnity(Context context) {
        mContext = context;
    }

    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (mFURenderer == null || !mEnabled) {
            return outFrame;
        }

        outFrame.textureId = mFURenderer.onDrawFrame(outFrame.image,
                outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight());

        // The texture is transformed to texture2D by beauty module.
        outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        return outFrame;
    }

    @Override
    public void initPreprocessor() {
        if (authpack.A() == null) {
            mAuthenticated = false;
            return;
        }

        mFURenderer = new FURenderer.Builder(mContext).
                inputImageFormat(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .setNeedAnimoji3D(true)
                // Don't want FURenderer to loads beauty
                // bundle at the initialization phase
                // asynchronously
                .setNeedFaceBeauty(false)
                .build();
        mFURenderer.onSurfaceCreated();
        mFURenderer.onBlurLevelSelected(DEFAULT_BLUR_VALUE);
        mFURenderer.onColorLevelSelected(DEFAULT_WHITEN_VALUE);
        mFURenderer.onCheekVSelected(DEFAULT_CHEEK_VALUE);
        mFURenderer.onEyeEnlargeSelected(DEFAULT_EYE_VALUE);

        mFURenderer.setOnBundleLoadCompleteListener(what -> {
            if (what == MSG_EFFECT_BUNDLE_COMPLETE &&
                    mBundleListener != null) {
                mBundleListener.onFuEffectBundleLoaded();
            }
        });

        initAnimoji();

        // Enable beautification by default
        mEnabled = true;
        enableBeauty();
    }

    public boolean FUAuthenticated() {
        return mAuthenticated;
    }

    private void initAnimoji() {
        mHaskiEffect = new Effect("haski", -1, "hashiqi.bundle",
                1, Effect.EFFECT_TYPE_ANIMOJI, -1);
        mGirlEffect = new Effect("qgirl", -1, "girl.bundle",
                1, Effect.EFFECT_TYPE_ANIMOJI, -1);

        mEffectAnimojiHaskiHandle = mFURenderer.loadItem("hashiqi.bundle");
        mEffectAnimojiGirlHandle = mFURenderer.loadItem("girl.bundle");
        mEffectBackgroundHandle = mFURenderer.loadItem("bg.bundle");
        mBeautyHandle = mFURenderer.loadItem("face_beautification.bundle");
    }

    public void setOnBundleLoadedListener(OnFuEffectBundleLoadedListener listener) {
        mBundleListener = listener;
    }

    public void setOnFirstFrameListener(OnFirstFrameListener listener) {
        mFirstFrameListener = listener;
    }

    private void enableBeauty() {
        mFURenderer.onEffectImageSelected(null,
                0, mBeautyHandle, true);
    }

    public void onAnimojiSelected(int index) {
        if (mFURenderer != null) {
            if (0 == index) {
                enablePreProcess(true);
                mFURenderer.onEffectImageSelected(mHaskiEffect,
                        mEffectAnimojiHaskiHandle,
                        mEffectBackgroundHandle, true);
            } else if (1 == index) {
                mFURenderer.onEffectImageSelected(mGirlEffect,
                        mEffectAnimojiGirlHandle,
                        mEffectBackgroundHandle, true);
            } else {
                enablePreProcess(true);
                enableBeauty();
            }
        }
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        if (mAuthenticated) mEnabled = enabled;
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        if (mFURenderer != null) {
            mFURenderer.onSurfaceDestroyed();
        }
    }

    @Override
    public void setBlurValue(float blur) {
        if (mFURenderer != null) {
            mFURenderer.onBlurLevelSelected(blur);
        }
    }

    @Override
    public void setWhitenValue(float whiten) {
        if (mFURenderer != null) {
            mFURenderer.onColorLevelSelected(whiten);
        }
    }

    @Override
    public void setCheekValue(float cheek) {
        if (mFURenderer != null) {
            mFURenderer.onCheekThinningSelected(cheek);
        }
    }

    @Override
    public void setEyeValue(float eye) {
        if (mFURenderer != null) {
            mFURenderer.onEyeEnlargeSelected(eye);
        }
    }

    @Override
    public void onFrameFrame() {
        if (mFirstFrameListener != null) {
            mFirstFrameListener.onFirstFrame();
        }
    }
}
