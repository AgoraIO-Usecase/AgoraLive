package com.faceunity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.faceunity.entity.CartoonFilter;
import com.faceunity.entity.Effect;
import com.faceunity.entity.FaceMakeup;
import com.faceunity.entity.Filter;
import com.faceunity.entity.LivePhoto;
import com.faceunity.entity.MakeupItem;
import com.faceunity.gles.core.GlUtil;
import com.faceunity.param.BeautificationParam;
import com.faceunity.param.MakeupParamHelper;
import com.faceunity.utils.Constant;
import com.faceunity.utils.FileUtils;
import com.faceunity.wrapper.faceunity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.faceunity.wrapper.faceunity.FU_ADM_FLAG_FLIP_X;
import static com.faceunity.wrapper.faceunity.fuItemSetParam;

/**
 * 一个基于Faceunity Nama SDK的简单封装，方便简单集成，理论上简单需求的步骤：
 * <p>
 * 1.通过OnEffectSelectedListener在UI上进行交互
 * 2.合理调用FURenderer构造函数
 * 3.对应的时机调用onSurfaceCreated和onSurfaceDestroyed
 * 4.处理图像时调用onDrawFrame
 */
public class FURenderer implements OnFUControlListener {
    private static final String TAG = FURenderer.class.getSimpleName();
    public static final int FU_ADM_FLAG_EXTERNAL_OES_TEXTURE = faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE;
    private Context mContext;

    // v3.bundle：人脸识别数据文件，缺少该文件会导致系统初始化失败。
    private static final String BUNDLE_V3 = "v3.bundle";
    // fxaa.bundle：3D绘制抗锯齿数据文件，加载后3D绘制效果更加平滑。
    private static final String BUNDLE_FXAA = "fxaa.bundle";
    // 美颜 bundle
    private static final String BUNDLE_FACE_BEAUTIFICATION = "beautify_face/face_beautification.bundle";
    // 美发极速版模型
    private static final String BUNDLE_HAIR_MODEL_HIGH_SPEED = "beautify_hair/hair_model_w0305.bundle";
    // 美发正常色 bundle
    private static final String BUNDLE_HAIR_NORMAL = "beautify_hair/hair_normal_no_face_no_render.bundle";
    // 美发渐变色 bundle
    private static final String BUNDLE_HAIR_GRADIENT = "beautify_hair/hair_gradient_no_face_no_render.bundle";
    // Animoji 舌头 bundle
    private static final String BUNDLE_TONGUE = "tongue.bundle";
    // 海报换脸 bundle
    private static final String BUNDLE_CHANGE_FACE = "change_face/change_face.bundle";
    // 动漫滤镜 bundle
    private static final String BUNDLE_CARTOON_FILTER = "cartoon_filter/fuzzytoonfilter.bundle";
    // 轻美妆 bundle
    private static final String BUNDLE_LIGHT_MAKEUP = "light_makeup/light_makeup.bundle";
    // 美妆 bundle
    private static final String BUNDLE_FACE_MAKEUP = "makeup/face_makeup.bundle";
    // 表情动图 bundle
    private static final String BUNDLE_LIVE_PHOTO = "live_photo/photolive.bundle";
    // 209 点位人脸识别 bundle
    private static final String BUNDLE_NEW_FACE_TRACKER = "makeup/new_face_tracker_normal.bundle";
    // Avatar 捏脸的背景 bundle
    private static final String BUNDLE_AVATAR_BACKGROUND = "avatar/avatar_background.bundle";
    // 美体 bundle
    private static final String BUNDLE_BEAUTIFY_BODY = "beautify_body/BodySlim.bundle";

    private volatile static float mFilterLevel = 1.0f;//滤镜强度
    private volatile static float mSkinDetect = 1.0f;//肤色检测开关
    private volatile static float mBlurLevel = 0.7f;//磨皮程度
    private volatile static float mBlurType = 0.0f;//磨皮类型
    private volatile static float mColorLevel = 0.3f;//美白
    private volatile static float mRedLevel = 0.3f;//红润
    private volatile static float mEyeBright = 0.0f;//亮眼
    private volatile static float mToothWhiten = 0.0f;//美牙
    private volatile static float mFaceShape = BeautificationParam.FACE_SHAPE_CUSTOM;//脸型
    private volatile static float mFaceShapeLevel = 1.0f;//程度
    private volatile static float mCheekThinning = 0f;//瘦脸
    private volatile static float mCheekV = 0.5f;//V脸
    private volatile static float mCheekNarrow = 0f;//窄脸
    private volatile static float mCheekSmall = 0f;//小脸
    private volatile static float mEyeEnlarging = 0.4f;//大眼
    private volatile static float mIntensityChin = 0.3f;//下巴
    private volatile static float mIntensityForehead = 0.3f;//额头
    private volatile static float mIntensityMouth = 0.4f;//嘴形
    private volatile static float mIntensityNose = 0.5f;//瘦鼻

    private int mFrameId = 0;

    // 句柄索引
    private static final int ITEM_ARRAYS_FACE_BEAUTY_INDEX = 0;
    public static final int ITEM_ARRAYS_EFFECT_INDEX = 1;
    private static final int ITEM_ARRAYS_LIGHT_MAKEUP_INDEX = 2;
    private static final int ITEM_ARRAYS_ABIMOJI_3D_INDEX = 3;
    private static final int ITEM_ARRAYS_HAIR_NORMAL_INDEX = 4;
    private static final int ITEM_ARRAYS_HAIR_GRADIENT_INDEX = 5;
    private static final int ITEM_ARRAYS_CHANGE_FACE_INDEX = 6;
    private static final int ITEM_ARRAYS_CARTOON_FILTER_INDEX = 7;
    private static final int ITEM_ARRAYS_LIVE_PHOTO_INDEX = 8;
    private static final int ITEM_ARRAYS_FACE_MAKEUP_INDEX = 9;
    public static final int ITEM_ARRAYS_AVATAR_BACKGROUND = 10;
    public static final int ITEM_ARRAYS_AVATAR_HAIR = 11;
    private static final int ITEM_ARRAYS_NEW_FACE_TRACKER = 12;
    private static final int ITEM_ARRAYS_BEAUTIFY_BODY = 13;
    private static final int ITEM_ARRAYS_HAIR_MODEL = 14;
    // 句柄数量
    private static final int ITEM_ARRAYS_COUNT = 15;

    // 海报换脸 track face 50次
    private static final int MAX_TRACK_COUNT = 50;

    // 头发
    public static final int HAIR_NORMAL = 1;
    public static final int HAIR_GRADIENT = 2;

    // 默认滤镜，粉嫩效果
    private volatile static String sFilterName = Filter.Key.FENNEN_1;
    //美颜和其他道具的handle数组
    private final int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];
    //用于和异步加载道具的线程交互
    private Handler mFuItemHandler;

    private boolean isNeedBeautyHair = false;
    private boolean isNeedFaceBeauty = true;
    private boolean isNeedAnimoji3D = false;
    private boolean isNeedPosterFace = false;
    private volatile Effect mDefaultEffect;//默认道具（同步加载）
    private boolean mIsCreateEGLContext; //是否需要手动创建EGLContext
    private int mInputTextureType = 0; //输入的图像texture类型，Camera提供的默认为EXTERNAL OES
    private int mInputImageFormat = 0;
    //美颜和滤镜的默认参数
    private volatile boolean isNeedUpdateFaceBeauty = true;
    // 是否启用美体
    private boolean mUseBeautifyBody;
    private float mBodySlimStrength = 0.0f; // 瘦身
    private float mLegSlimStrength = 0.0f; // 长腿
    private float mWaistSlimStrength = 0.0f; // 细腰
    private float mShoulderSlimStrength = 0.5f; // 美肩
    private float mHipSlimStrength = 0.0f; // 美胯

    private volatile int mInputImageOrientation = 270;
    private volatile int mInputPropOrientation = 270;//道具方向（针对全屏道具）
    private volatile int mIsInputImage = 0;//输入的是否是图片
    private volatile int mCurrentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private volatile int mMaxFaces = 4; //同时识别的最大人脸
    // 美发参数
    private volatile float mHairColorStrength = 0.6f;
    private volatile int mHairColorType = HAIR_GRADIENT;
    private volatile int mHairColorIndex = 0;

    // 美妆妆容参数集合
    private Map<String, Object> mMakeupParams = new ConcurrentHashMap<>(16);
    // 轻美妆妆容集合
    private Map<Integer, MakeupItem> mLightMakeupItemMap = new ConcurrentHashMap<>(16);
    private double[] mLipStickColor;

    private float[] landmarksData = new float[150];
    private float[] expressionData = new float[46];
    private float[] rotationData = new float[4];
    private float[] pupilPosData = new float[2];
    private float[] rotationModeData = new float[1];
    private float[] faceRectData = new float[4];
    private double[] posterTemplateLandmark;
    private double[] posterPhotoLandmark;

    private List<Runnable> mEventQueue;
    private OnBundleLoadCompleteListener mOnBundleLoadCompleteListener;
    private volatile int mComicFilterStyle = CartoonFilter.NO_FILTER;
    private static boolean mIsInited;
    private volatile int mDefaultOrientation = 90;
    private int mRotMode = 1;
    private boolean mNeedBackground;

    /**
     * 全局加载相应的底层数据包，应用使用期间只需要初始化一次
     * 初始化系统环境，加载系统数据，并进行网络鉴权。必须在调用SDK其他接口前执行，否则会引发崩溃。
     */
    public static void initFURenderer(Context context) {
        if (mIsInited) {
            return;
        }
        try {
            //获取faceunity SDK版本信息
            Log.e(TAG, "fu sdk version " + faceunity.fuGetVersion());
            long startTime = System.currentTimeMillis();
            /**
             * fuSetup faceunity初始化
             * 其中 v3.bundle：人脸识别数据文件，缺少该文件会导致系统初始化失败；
             *      authpack：鉴权证书内存数组。
             * 首先调用完成后再调用其他 API
             */
            InputStream v3 = context.getAssets().open(BUNDLE_V3);
            byte[] v3Data = new byte[v3.available()];
            v3.read(v3Data);
            v3.close();
            faceunity.fuSetup(v3Data, authpack.A());

            /**
             * fuLoadTongueModel 识别舌头动作数据包加载
             * 其中 tongue.bundle：头动作驱动数据包；
             */
            InputStream tongue = context.getAssets().open(BUNDLE_TONGUE);
            byte[] tongueDate = new byte[tongue.available()];
            tongue.read(tongueDate);
            tongue.close();
            faceunity.fuLoadTongueModel(tongueDate);

            long duration = System.currentTimeMillis() - startTime;
            Log.i(TAG, "setup fu sdk finish: " + duration + "ms");
            mIsInited = true;
        } catch (Exception e) {
            Log.e(TAG, "initFURenderer error", e);
        }
    }

    /**
     * AgoraLive implementation
     * @param image virtual image effect
     * @param background effect used to draw background images
     */
    public void onEffectImageSelected(Effect image, final Effect background) {
        onEffectSelected(image);
        mFuItemHandler.post(new Runnable() {
            @Override
            public void run() {
                final int backgroundItem = loadItem(background.path());
                if (backgroundItem <= 0) {
                    Log.w(TAG, "load background item failed: " + backgroundItem);
                }

                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = backgroundItem;
                    }
                });
            }
        });
    }

    public void onEffectImageSelected(Effect imageEffect, int imageHandle, int backgroundHandle, boolean updateBeauty) {
        mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = backgroundHandle;

        if (imageHandle > 0) {
            updateEffectItemParams(imageEffect, imageHandle);
            // Private parameter to set the virtual image in the middle
            fuItemSetParam(imageHandle, "{\"thing\":\"<global>\",\"param\":\"follow\"}", 0);
        }

        mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = imageHandle;
        if (mOnBundleLoadCompleteListener != null) {
            mOnBundleLoadCompleteListener.onBundleLoadComplete(ITEM_ARRAYS_EFFECT_INDEX);
        }
    }

    /**
     * 设置相机滤镜的风格
     *
     * @param style
     */
    @Override
    public void onCartoonFilterSelected(final int style) {
        if (mComicFilterStyle == style) {
            return;
        }
        mComicFilterStyle = style;
        if (mFuItemHandler == null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_CARTOON_FILTER_INDEX, mComicFilterStyle));
                }
            });
        } else {
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_CARTOON_FILTER_INDEX, mComicFilterStyle));
        }
    }

    /**
     * 获取faceunity sdk 版本库
     */
    public static String getVersion() {
        return faceunity.fuGetVersion();
    }

    /**
     * 获取证书相关的权限码
     */
    public static int getModuleCode(int index) {
        return faceunity.fuGetModuleCode(index);
    }

    /**
     * FURenderer构造函数
     */
    private FURenderer(Context context, boolean isCreateEGLContext) {
        this.mContext = context;
        this.mIsCreateEGLContext = isCreateEGLContext;
    }

    /**
     * 创建及初始化faceunity相应的资源
     */
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated");
        onSurfaceDestroyed();

        mEventQueue = Collections.synchronizedList(new ArrayList<Runnable>(16));

        HandlerThread handlerThread = new HandlerThread("FUItemWorker");
        handlerThread.start();
        mFuItemHandler = new FUItemHandler(handlerThread.getLooper());

        /**
         * fuCreateEGLContext 创建OpenGL环境
         * 适用于没OpenGL环境时调用
         * 如果调用了fuCreateEGLContext，在销毁时需要调用fuReleaseEGLContext
         */
        if (mIsCreateEGLContext) {
            faceunity.fuCreateEGLContext();
        }

        mFrameId = 0;
        /**
         *fuSetExpressionCalibration 控制表情校准功能的开关及不同模式，参数为0时关闭表情校准，2为被动校准。
         * 被动校准：该种模式下会在整个用户使用过程中逐渐进行表情校准，用户对该过程没有明显感觉。
         *
         * 优化后的SDK只支持被动校准功能，即fuSetExpressionCalibration接口只支持0（关闭）或2（被动校准）这两个数字，设置为1时将不再有效果。
         */
        faceunity.fuSetExpressionCalibration(2);
        faceunity.fuSetMaxFaces(mMaxFaces);//设置多脸，目前最多支持8人。

        if (isNeedFaceBeauty) {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_FACE_BEAUTY_INDEX);
        }
        if (isNeedBeautyHair) {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_HAIR_MODEL);
            if (mHairColorType == HAIR_NORMAL) {
                mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_HAIR_NORMAL_INDEX);
            } else {
                mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_HAIR_GRADIENT_INDEX);
            }
        }
        if (isNeedAnimoji3D) {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_ABIMOJI_3D_INDEX);
        }
        if (isNeedPosterFace) {
            posterPhotoLandmark = new double[150];
            posterTemplateLandmark = new double[150];
            mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX] = loadItem(BUNDLE_CHANGE_FACE);
        }

        // 设置动漫滤镜
        int style = mComicFilterStyle;
        mComicFilterStyle = CartoonFilter.NO_FILTER;
        onCartoonFilterSelected(style);

        if (mNeedBackground) {
            loadAvatarBackground();
        }

        // 异步加载默认道具，放在加载 animoji 3D 和动漫滤镜之后
        if (mDefaultEffect != null) {
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
        }

        // 恢复美妆的参数值
        if (mMakeupParams.size() > 0) {
            selectMakeupItem(new HashMap<>(mMakeupParams), false);
        }

        // 恢复质感美颜的参数值
        if (mLightMakeupItemMap.size() > 0) {
            Set<Map.Entry<Integer, MakeupItem>> entries = mLightMakeupItemMap.entrySet();
            for (Map.Entry<Integer, MakeupItem> entry : entries) {
                MakeupItem makeupItem = entry.getValue();
                onLightMakeupSelected(makeupItem, makeupItem.getLevel());
            }
        }

        // 加载美体道具，并设置美体参数
        if (mUseBeautifyBody) {
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_BEAUTIFY_BODY));
        }

        // 设置同步
        setAsyncTrackFace(false);
        // 设置 Animoji 嘴巴灵敏度
        setMouthExpression(0.5f);
    }

    /**
     * 加载 bundle 道具，不需要 EGL Context，可以异步执行
     *
     * @param bundlePath bundle 文件路径
     * @return 道具句柄，大于 0 表示加载成功
     */
    private static int loadItem(Context context, String bundlePath) {
        int handle = 0;
        if (!TextUtils.isEmpty(bundlePath)) {
            byte[] buffer = FileUtils.readFile(context, bundlePath);
            if (buffer != null) {
                handle = faceunity.fuCreateItemFromPackage(buffer);
            }
        }
        Log.d(TAG, "loadItem. bundlePath: " + bundlePath + ", itemHandle: " + handle);
        return handle;
    }

    /**
     * 单输入接口(fuRenderToNV21Image)
     *
     * @param img NV21数据
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(byte[] img, int w, int h) {
        if (img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, mItemsArray, flags);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 单输入接口(fuRenderToNV21Image)，自定义画面数据需要回写到的byte[]
     *
     * @param img         NV21数据
     * @param w
     * @param h
     * @param readBackImg 画面数据需要回写到的byte[]
     * @param readBackW
     * @param readBackH
     * @return
     */
    public int onDrawFrame(byte[] img, int w, int h, byte[] readBackImg, int readBackW, int readBackH) {
        if (img == null || w <= 0 || h <= 0 || readBackImg == null || readBackW <= 0 || readBackH <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, mItemsArray, flags,
                readBackW, readBackH, readBackImg);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 双输入接口(fuDualInputToTexture)(处理后的画面数据并不会回写到数组)，由于省去相应的数据拷贝性能相对最优，推荐使用。
     *
     * @param img NV21数据
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(byte[] img, int tex, int w, int h) {
        if (tex <= 0 || img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType | mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 双输入接口(fuDualInputToTexture)，自定义画面数据需要回写到的byte[]
     *
     * @param img         NV21数据
     * @param tex         纹理ID
     * @param w
     * @param h
     * @param readBackImg 画面数据需要回写到的byte[]
     * @param readBackW
     * @param readBackH
     * @return
     */
    public int onDrawFrame(byte[] img, int tex, int w, int h, byte[] readBackImg, int readBackW, int readBackH) {
        if (tex <= 0 || img == null || w <= 0 || h <= 0 || readBackImg == null || readBackW <= 0 || readBackH <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType | mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray,
                readBackW, readBackH, readBackImg);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 单输入接口(fuRenderToTexture)
     *
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(int tex, int w, int h) {
        if (tex <= 0 || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuRenderToTexture(tex, w, h, mFrameId++, mItemsArray, flags);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 单美颜接口(fuBeautifyImage)，将输入的图像数据，送入SDK流水线进行全图美化，并输出处理之后的图像数据。
     * 该接口仅执行图像层面的美化处 理（包括滤镜、美肤），不执行人脸跟踪及所有人脸相关的操作（如美型）。
     * 由于功能集中，相比 fuDualInputToTexture 接口执行美颜道具，该接口所需计算更少，执行效率更高。
     *
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrameBeautify(int tex, int w, int h) {
        if (tex <= 0 || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuBeautifyImage(tex, flags, w, h, mFrameId++, mItemsArray);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    public float[] getRotationData() {
        Arrays.fill(rotationData, 0.0f);
        faceunity.fuGetFaceInfo(0, "rotation", rotationData);
        return rotationData;
    }

    /**
     * 销毁faceunity相关的资源
     */
    public void onSurfaceDestroyed() {
        Log.e(TAG, "onSurfaceDestroyed");
        if (mFuItemHandler != null) {
            mFuItemHandler.getLooper().quitSafely();
            mFuItemHandler = null;
        }
        if (mEventQueue != null) {
            mEventQueue.clear();
            mEventQueue = null;
        }

        int posterIndex = mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX];
        if (posterIndex > 0) {
            faceunity.fuDeleteTexForItem(posterIndex, "tex_input");
            faceunity.fuDeleteTexForItem(posterIndex, "tex_template");
        }

        int lightMakeupIndex = mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX];
        if (lightMakeupIndex > 0) {
            Set<Integer> makeupTypes = mLightMakeupItemMap.keySet();
            for (Integer makeupType : makeupTypes) {
                faceunity.fuDeleteTexForItem(lightMakeupIndex, MakeupParamHelper.getMakeupTextureKeyByType(makeupType));
            }
        }

        int faceMakeupIndex = mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX];
        if (faceMakeupIndex > 0) {
            Set<String> keys = mMakeupParams.keySet();
            for (String key : keys) {
                if (key.startsWith(MakeupParamHelper.MakeupParam.TEX_PREFIX)) {
                    faceunity.fuDeleteTexForItem(faceMakeupIndex, key);
                }
            }
        }
        int livePhotoPhotoIndex = mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX];
        if (livePhotoPhotoIndex > 0) {
            faceunity.fuDeleteTexForItem(livePhotoPhotoIndex, "tex_input");
        }

        mFrameId = 0;
        isNeedUpdateFaceBeauty = true;
        Arrays.fill(mItemsArray, 0);
        faceunity.fuDestroyAllItems();
        faceunity.fuOnDeviceLost();
        faceunity.fuDone();
        if (mIsCreateEGLContext) {
            faceunity.fuReleaseEGLContext();
        }
    }

    /**
     * 获取 landmark 点位
     *
     * @param faceId
     * @return 调用时不要修改返回值，如需修改或传入 Nama 接口，请拷贝一份
     */
    public float[] getLandmarksData(int faceId) {
        int isTracking = faceunity.fuIsTracking();
        Arrays.fill(landmarksData, 0.0f);
        if (isTracking > 0) {
            faceunity.fuGetFaceInfo(faceId, "landmarks", landmarksData);
        }
        return landmarksData;
    }

    public int trackFace(byte[] img, int w, int h) {
        if (img == null) {
            return 0;
        }
        faceunity.fuOnCameraChange();
        int flags = mInputImageFormat;
        for (int i = 0; i < MAX_TRACK_COUNT; i++) {
            faceunity.fuTrackFace(img, flags, w, h);
        }
        return faceunity.fuIsTracking();
    }

    public float[] getFaceRectData(int i) {
        Arrays.fill(faceRectData, 0.0f);
        faceunity.fuGetFaceInfo(i, "face_rect", faceRectData);
        return faceRectData;
    }

    //--------------------------------------对外可使用的接口----------------------------------------

    /**
     * 使用 fuTrackFace + fuAvatarToTexture 的方法组合绘制画面，该组合没有camera画面绘制，适用于animoji等相关道具的绘制。
     * fuTrackFace 获取识别到的人脸信息
     * fuAvatarToTexture 依据人脸信息绘制道具
     *
     * @param img 数据格式可由 flags 定义
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrameAvatar(byte[] img, int w, int h) {
        if (img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrameAvatar data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputImageFormat;
        if (mNeedBenchmark) {
            mFuCallStartTime = System.nanoTime();
        }

        faceunity.fuTrackFace(img, flags, w, h);
        int isTracking = faceunity.fuIsTracking();

        Arrays.fill(landmarksData, 0.0f);
        Arrays.fill(rotationData, 0.0f);
        Arrays.fill(expressionData, 0.0f);
        Arrays.fill(pupilPosData, 0.0f);
        Arrays.fill(rotationModeData, 0.0f);

        if (isTracking > 0) {
            /**
             * landmarks 2D人脸特征点，返回值为75个二维坐标，长度75*2
             */
            faceunity.fuGetFaceInfo(0, "landmarks", landmarksData);
            /**
             *rotation 人脸三维旋转，返回值为旋转四元数，长度4
             */
            faceunity.fuGetFaceInfo(0, "rotation", rotationData);
            /**
             * expression  表情系数，长度46
             */
            faceunity.fuGetFaceInfo(0, "expression", expressionData);
            /**
             * pupil pos 眼球旋转，长度2
             */
            faceunity.fuGetFaceInfo(0, "pupil_pos", pupilPosData);
            /**
             * rotation mode 人脸朝向，0-3分别对应手机四种朝向，长度1
             */
            faceunity.fuGetFaceInfo(0, "rotation_mode", rotationModeData);
        } else {
            rotationData[3] = 1.0f;
            rotationModeData[0] = 1.0f * (360 - mInputImageOrientation) / 90;
        }

        int tex = faceunity.fuAvatarToTexture(AvatarConstant.PUP_POS_DATA, AvatarConstant.EXPRESSIONS,
                AvatarConstant.ROTATION_DATA, rotationModeData, 0, w, h, mFrameId++, mItemsArray,
                AvatarConstant.VALID_DATA);
        if (mNeedBenchmark) {
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        }
        return tex;
    }

    /**
     * 进入捏脸状态
     */
    public void enterFaceShape() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "enter_facepup", 1);
                }
            }
        });
    }

    /**
     * 清除全部捏脸参数
     */
    public void clearFaceShape() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "clear_facepup", 1);
                }

                if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR], "clear_facepup", 1);
                }
            }
        });
    }

    /**
     * 保存和退出，二选一即可
     * 直接退出捏脸状态，不保存当前捏脸状态，进入跟踪状态。使用上一次捏脸，进行人脸表情跟踪。
     */
    public void quitFaceup() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "quit_facepup", 1);
                }
            }
        });
    }

    /**
     * 触发保存捏脸，并退出捏脸状态，进入跟踪状态。耗时操作，必要时设置。
     */
    public void recomputeFaceup() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "need_recompute_facepup", 1);
                }
            }
        });
    }

    /**
     * 设置捏脸属性的权值，范围[0-1]。这里param对应的就是第几个捏脸属性，从1开始。
     *
     * @param key
     * @param value
     */
    public void fuItemSetParamFaceup(final String key, final double value) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "{\"name\":\"facepup\",\"param\":\"" + key + "\"}", value);
                }
            }
        });
    }

    /**
     * 设置 avatar 颜色参数
     *
     * @param key
     * @param value [r,g,b] 或 [r,g,b,intensity]
     */
    public void fuItemSetParamFaceColor(final String key, final double[] value) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (value.length > 3) {
                    if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR], key, value);
                    }
                } else {
                    if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], key, value);
                    }
                }
            }
        });
    }

    /**
     * whether avatar bundle is loaded
     *
     * @return
     */
    public boolean isAvatarLoaded() {
        return mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0;
    }

    /**
     * whether avatar hair and background bundle is loaded
     *
     * @return
     */
    public boolean isAvatarMakeupItemLoaded() {
        return mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] > 0 && mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0;
    }

    /**
     * （参数为浮点数）,直接设置绝对缩放
     *
     * @param scale
     */
    public void setAvatarScale(final float scale) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "absoluteScale", scale);
                }
            }
        });
    }

    /**
     * （参数为浮点数）,直接设置绝对缩放
     *
     * @param scale
     */
    public void setAvatarHairScale(final float scale) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR], "absoluteScale", scale);
                }
            }
        });
    }

    /**
     * （参数为[x,y,z]数组）,直接设置绝对位移
     *
     * @param xyz
     */
    public void setAvatarTranslate(final double[] xyz) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "absoluteTranslate", xyz);
                }
            }
        });
    }

    /**
     * （参数为[x,y,z]数组）,直接设置绝对位移
     *
     * @param xyz
     */
    public void setAvatarHairTranslate(final double[] xyz) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR], "absoluteTranslate", xyz);
                }
            }
        });
    }

    /**
     * 类似GLSurfaceView的queueEvent机制
     */
    public void queueEvent(Runnable r) {
        if (mEventQueue == null) {
            return;
        }
        mEventQueue.add(r);
    }

    /**
     * 设置人脸跟踪异步
     *
     * @param isAsync 是否异步
     */
    public void setAsyncTrackFace(final boolean isAsync) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                faceunity.fuSetAsyncTrackFace(isAsync ? 1 : 0);
            }
        });
    }

    /**
     * 设置需要识别的人脸个数
     *
     * @param maxFaces
     */
    public void setMaxFaces(final int maxFaces) {
        if (mMaxFaces != maxFaces && maxFaces > 0) {
            mMaxFaces = maxFaces;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    faceunity.fuSetMaxFaces(mMaxFaces);
                }
            });
        }
    }

    /**
     * 表情动图，切换相机时设置方向
     *
     * @param isFront 是否为前置相机
     */
    public void setIsFrontCamera(final boolean isFront) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX] > 0) {
                    if (isFront && mInputImageOrientation == 90) {
                        // 解决 Nexus 手机前置相机发生X镜像的问题
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX], "is_swap_x", 1.0);
                    } else {
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX], "is_swap_x", 0.0);
                    }
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX], "is_front", isFront ? 1.0 : 0.0);
                }
            }
        });
    }

    /**
     * 每帧处理画面时被调用
     */
    private void prepareDrawFrame() {
        //计算FPS等数据
        benchmarkFPS();

        if (mUseBeautifyBody) {
            //  是否检测到人体
            int hasHuman = (int) faceunity.fuItemGetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], "HasHuman");
            if (mOnTrackingStatusChangedListener != null && mTrackingStatus != hasHuman) {
                mOnTrackingStatusChangedListener.onTrackingStatusChanged(mTrackingStatus = hasHuman);
            }
        } else {
            //获取人脸是否识别，并调用回调接口
            int isTracking = faceunity.fuIsTracking();
            if (mOnTrackingStatusChangedListener != null && mTrackingStatus != isTracking) {
                mOnTrackingStatusChangedListener.onTrackingStatusChanged(mTrackingStatus = isTracking);
            }
        }

        //获取faceunity错误信息，并调用回调接口
        int error = faceunity.fuGetSystemError();
        if (error != 0) {
            Log.e(TAG, "fuGetSystemErrorString " + faceunity.fuGetSystemErrorString(error));
            if (mOnSystemErrorListener != null) {
                mOnSystemErrorListener.onSystemError(faceunity.fuGetSystemErrorString(error));
            }
        }

        //修改美颜参数
        if (isNeedUpdateFaceBeauty && mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] > 0) {
            int itemFaceBeauty = mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX];
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.FILTER_NAME, sFilterName);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.FILTER_LEVEL, mFilterLevel);

            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.SKIN_DETECT, mSkinDetect);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.HEAVY_BLUR, 0.0f);
            faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], BeautificationParam.BLUR_TYPE, mBlurType);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.BLUR_LEVEL, 6.0 * mBlurLevel);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.COLOR_LEVEL, mColorLevel);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.RED_LEVEL, mRedLevel);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.EYE_BRIGHT, mEyeBright);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.TOOTH_WHITEN, mToothWhiten);

            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.FACE_SHAPE_LEVEL, mFaceShapeLevel);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.FACE_SHAPE, mFaceShape);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.EYE_ENLARGING, mEyeEnlarging);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.CHEEK_THINNING, mCheekThinning);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.CHEEK_NARROW, mCheekNarrow);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.CHEEK_SMALL, mCheekSmall);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.CHEEK_V, mCheekV);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.INTENSITY_NOSE, mIntensityNose);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.INTENSITY_CHIN, mIntensityChin);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.INTENSITY_FOREHEAD, mIntensityForehead);
            faceunity.fuItemSetParam(itemFaceBeauty, BeautificationParam.INTENSITY_MOUTH, mIntensityMouth);
            isNeedUpdateFaceBeauty = false;
        }

        //queueEvent的Runnable在此处被调用
        while (mEventQueue != null && !mEventQueue.isEmpty()) {
            mEventQueue.remove(0).run();
        }
    }

    /**
     * camera切换时需要调用
     *
     * @param currentCameraType     前后置摄像头ID
     * @param inputImageOrientation
     */
    public void onCameraChange(final int currentCameraType, final int inputImageOrientation) {
        if (mCurrentCameraType == currentCameraType && mInputImageOrientation == inputImageOrientation)
            return;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mFrameId = 0;
                mCurrentCameraType = currentCameraType;
                mInputImageOrientation = inputImageOrientation;
                mInputPropOrientation = inputImageOrientation;
                faceunity.fuOnCameraChange();
                mRotMode = calculateRotMode();
                updateEffectItemParams(mDefaultEffect, mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
                int trackerOrientation = calculateFaceTrackerOrientation();
                setFaceTrackerOrientation(trackerOrientation);
                if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                    int orientation;
                    if (mIsInputImage == 1) {
                        orientation = 0;
                    } else {
                        orientation = trackerOrientation;
                    }
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.ORIENTATION, orientation);
                }
            }
        });
    }

    /**
     * camera切换时需要调用
     *
     * @param currentCameraType     前后置摄像头ID
     * @param inputImageOrientation
     * @param inputPropOrientation
     */
    public void onCameraChange(final int currentCameraType, final int inputImageOrientation
            , final int inputPropOrientation) {
        if (mCurrentCameraType == currentCameraType && mInputImageOrientation == inputImageOrientation &&
                mInputPropOrientation == inputPropOrientation)
            return;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mFrameId = 0;
                mCurrentCameraType = currentCameraType;
                mInputImageOrientation = inputImageOrientation;
                mInputPropOrientation = inputPropOrientation;
                faceunity.fuOnCameraChange();
                mRotMode = calculateRotMode();
                updateEffectItemParams(mDefaultEffect, mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
                setFaceTrackerOrientation(calculateFaceTrackerOrientation());
            }
        });
    }

    /**
     * 设置识别方向
     *
     * @param rotation
     */
    public void setTrackOrientation(final int rotation) {
        if (mDefaultOrientation != rotation) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mDefaultOrientation = rotation;
                    /* 要设置的人脸朝向，取值范围为 0-3，分别对应人脸相对于图像数据旋转0度、90度、180度、270度。
                     * Android 前置摄像头一般设置参数 1，后置摄像头一般设置参数 3。部分手机存在例外 */
                    faceunity.fuSetDefaultOrientation(mDefaultOrientation / 90);
                    mRotMode = calculateRotMode();
                    // 背景分割 Animoji 表情识别 人像驱动 手势识别，转动手机时，重置人脸识别
                    if (mDefaultEffect != null && (mDefaultEffect.effectType() == Effect.EFFECT_TYPE_BACKGROUND
                            || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_ANIMOJI
                            || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_EXPRESSION
                            || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_GESTURE
                            || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_PORTRAIT_DRIVE
                            || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_AVATAR)) {
                        faceunity.fuOnCameraChange();
                    }
                    int trackerOrientation = calculateFaceTrackerOrientation();
                    if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "rotMode", mRotMode);
                        // 加载背景分割、全屏前景和全屏背景道具时，当手机发生旋转需要设置参数"rotationMode"
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "rotationMode", trackerOrientation);
                    }
                    Log.i(TAG, "setTrackOrientation. rotation: " + rotation + ", trackOrientation" + trackerOrientation);
                    setFaceTrackerOrientation(trackerOrientation);
                    if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                        int orientation;
                        if (mIsInputImage == 1) {
                            orientation = 0;
                        } else {
                            orientation = trackerOrientation;
                        }
                        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.ORIENTATION, orientation);
                    }
                }
            });
        }
    }

    /**
     * 设置 209 点位的人脸识别方向
     *
     * @param orientation 设置的范围是0 1 2 3 ，对应手机旋转角度
     *                    <p>
     *                    FUAI_CAMERA_VIEW_ROT_0 = 0,
     *                    FUAI_CAMERA_VIEW_ROT_90 = 1,
     *                    FUAI_CAMERA_VIEW_ROT_180 = 2,
     *                    FUAI_CAMERA_VIEW_ROT_270 = 3,
     */
    public void setFaceTrackerOrientation(final int orientation) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_NEW_FACE_TRACKER] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_NEW_FACE_TRACKER], "orientation", orientation);
                }
            }
        });
    }

    /**
     * 美妆功能点位镜像，0为关闭，1为开启
     *
     * @param isFlipPoints
     */
    public void setIsFlipPoints(final boolean isFlipPoints) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX], "is_flip_points", isFlipPoints ? 1 : 0);
                }
            }
        });
    }

    /**
     * "mouth_expression_more_flexible"
     * 0 到 1 之间的浮点数，默认为 0。值越大代表嘴巴越灵活（同时可能会越抖动）
     *
     * @param value
     */
    public void setMouthExpression(final float value) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                faceunity.fuSetFaceTrackParam("mouth_expression_more_flexible", value);
            }
        });
    }

    /**
     * 计算 RotMode
     *
     * @return
     */
    private int calculateRotMode() {
        int mode;
        if (mInputImageOrientation == 270) {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mode = mDefaultOrientation / 90;
            } else {
                mode = (mDefaultOrientation - 180) / 90;
            }
        } else {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mode = (mDefaultOrientation + 180) / 90;
            } else {
                mode = (mDefaultOrientation) / 90;
            }
        }
        return mode;
    }

    /**
     * 计算 209 点位的人脸识别方向
     *
     * @return orientation
     */
    private int calculateFaceTrackerOrientation() {
        int orientation;
        if (mInputImageOrientation == 270) {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                orientation = mDefaultOrientation / 90;
            } else {
                if (mDefaultOrientation == 90) {
                    orientation = 3;
                } else if (mDefaultOrientation == 270) {
                    orientation = 1;
                } else {
                    orientation = mDefaultOrientation / 90;
                }
            }
        } else {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (mDefaultOrientation == 0) {
                    orientation = 2;
                } else if (mDefaultOrientation == 90) {
                    orientation = 3;
                } else if (mDefaultOrientation == 180) {
                    orientation = 0;
                } else {
                    orientation = 1;
                }
            } else {
                if (mDefaultOrientation == 90) {
                    orientation = 3;
                } else if (mDefaultOrientation == 270) {
                    orientation = 1;
                } else {
                    orientation = mDefaultOrientation / 90;
                }
            }
        }
        return orientation;
    }

    public void changeInputType() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mFrameId = 0;
            }
        });
    }

    public void setDefaultEffect(Effect defaultEffect) {
        mDefaultEffect = defaultEffect;
    }

    //--------------------------------------美颜参数与道具回调----------------------------------------

    @Override
    public void onMusicFilterTime(final long time) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "music_time", time);
            }
        });
    }

    @Override
    public void onEffectSelected(Effect effect) {
        if (effect == null) {
            return;
        }
        mDefaultEffect = effect;
        if (mFuItemHandler == null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.removeMessages(ITEM_ARRAYS_EFFECT_INDEX);
                    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
                }
            });
        } else {
            mFuItemHandler.removeMessages(ITEM_ARRAYS_EFFECT_INDEX);
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
        }
    }

    @Override
    public void onFilterLevelSelected(float progress) {
        isNeedUpdateFaceBeauty = true;
        mFilterLevel = progress;
    }

    @Override
    public void onFilterNameSelected(String filterName) {
        isNeedUpdateFaceBeauty = true;
        sFilterName = filterName;
    }

    @Override
    public void onHairSelected(int type, int hairColorIndex, float hairColorLevel) {
        mHairColorIndex = hairColorIndex;
        mHairColorStrength = hairColorLevel;
        final int lastHairType = mHairColorType;
        mHairColorType = type;
        if (mHairColorType == lastHairType) {
            onHairLevelSelected(mHairColorType, mHairColorIndex, mHairColorStrength);
        } else {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mHairColorType == HAIR_NORMAL) {
                        mFuItemHandler.removeMessages(ITEM_ARRAYS_HAIR_NORMAL_INDEX);
                        mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_HAIR_NORMAL_INDEX);
                    } else if (mHairColorType == HAIR_GRADIENT) {
                        mFuItemHandler.removeMessages(ITEM_ARRAYS_HAIR_GRADIENT_INDEX);
                        mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_HAIR_GRADIENT_INDEX);
                    }
                }
            });
        }
    }

    @Override
    public void onHairLevelSelected(@HairType final int type, int hairColorIndex, float hairColorLevel) {
        mHairColorIndex = hairColorIndex;
        mHairColorStrength = hairColorLevel;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (type == HAIR_NORMAL) {
                    // 美发类型
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX], "Index", mHairColorIndex);
                    // 美发强度
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX], "Strength", mHairColorStrength);
                } else if (type == HAIR_GRADIENT) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX], "Index", mHairColorIndex);
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX], "Strength", mHairColorStrength);
                }
            }
        });
    }

    @Override
    public void onSkinDetectSelected(float isOpen) {
        isNeedUpdateFaceBeauty = true;
        mSkinDetect = isOpen;
    }

    @Override
    public void onBlurTypeSelected(float blurType) {
        mBlurType = blurType;
        isNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onBlurLevelSelected(float level) {
        mBlurLevel = level;
        isNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onColorLevelSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mColorLevel = level;
    }


    @Override
    public void onRedLevelSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mRedLevel = level;
    }

    @Override
    public void onEyeBrightSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mEyeBright = level;
    }

    @Override
    public void onToothWhitenSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mToothWhiten = level;
    }

    @Override
    public void onEyeEnlargeSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mEyeEnlarging = level;
    }

    @Override
    public void onCheekThinningSelected(float level) {
        mCheekThinning = level;
        isNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekNarrowSelected(float level) {
        // 窄脸参数上限为0.5
        mCheekNarrow = level / 2;
        isNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekSmallSelected(float level) {
        // 小脸参数上限为0.5
        mCheekSmall = level / 2;
        isNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekVSelected(float level) {
        mCheekV = level;
        isNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onIntensityChinSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mIntensityChin = level;
    }

    @Override
    public void onIntensityForeheadSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mIntensityForehead = level;
    }

    @Override
    public void onIntensityNoseSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mIntensityNose = level;
    }

    @Override
    public void onIntensityMouthSelected(float level) {
        isNeedUpdateFaceBeauty = true;
        mIntensityMouth = level;
    }

    @Override
    public void onPosterTemplateSelected(final int tempWidth, final int tempHeight, final byte[] temp, final float[] landmark) {
        Arrays.fill(posterTemplateLandmark, 0);
        for (int i = 0; i < landmark.length; i++) {
            posterTemplateLandmark[i] = landmark[i];
        }
        // 模板图片的宽
        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "template_width", tempWidth);
        // 模板图片的高
        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "template_height", tempHeight);
        // 图片的特征点，75个点
        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "template_face_points", posterTemplateLandmark);
        // 模板图片的 RGBA byte数组
        faceunity.fuCreateTexForItem(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "tex_template", temp, tempWidth, tempHeight);
    }

    @Override
    public void onPosterInputPhoto(final int inputWidth, final int inputHeight, final byte[] input, final float[] landmark) {
        Arrays.fill(posterPhotoLandmark, 0);
        for (int i = 0; i < landmark.length; i++) {
            posterPhotoLandmark[i] = landmark[i];
        }
        // 输入图片的宽
        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "input_width", inputWidth);
        // 输入图片的高
        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "input_height", inputHeight);
        // 输入图片的特征点，75个点
        faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "input_face_points", posterPhotoLandmark);
        // 输入图片的 RGBA byte 数组
        faceunity.fuCreateTexForItem(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "tex_input", input, inputWidth, inputHeight);
    }

    @Override
    public void setLivePhoto(final LivePhoto livePhoto) {
        if (mFuItemHandler == null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_LIVE_PHOTO_INDEX, livePhoto));
                }
            });
        } else {
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_LIVE_PHOTO_INDEX, livePhoto));
        }
    }

    @Override
    public void selectMakeupItem(final Map<String, Object> paramMap, final boolean removePrevious) {
        if (removePrevious) {
            final Map<String, Object> prevParams = new HashMap<>(mMakeupParams);
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    int itemHandle = mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX];
                    if (itemHandle > 0) {
                        Set<Map.Entry<String, Object>> entries = prevParams.entrySet();
                        for (Map.Entry<String, Object> entry : entries) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (value instanceof double[]) {
                                double[] emp = new double[((double[]) value).length];
//                                Log.v(TAG, "clear: setParams double array:" + key + ", value:" + Arrays.toString(emp));
                                faceunity.fuItemSetParam(itemHandle, key, emp);
                            } else if (value instanceof Double) {
                                if (key.startsWith(MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY_PREFIX)) {
//                                    Log.v(TAG, "clear: setParams double:" + key + ", 0.0");
                                    faceunity.fuItemSetParam(itemHandle, key, 0.0);
                                }
                            }
                        }
                    }
                }
            });
        }
        mMakeupParams.putAll(paramMap);
        if (mFuItemHandler != null) {
            mFuItemHandler.removeMessages(ITEM_ARRAYS_FACE_MAKEUP_INDEX);
            Message.obtain(mFuItemHandler, ITEM_ARRAYS_FACE_MAKEUP_INDEX, paramMap).sendToTarget();
        } else {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.removeMessages(ITEM_ARRAYS_FACE_MAKEUP_INDEX);
                    Message.obtain(mFuItemHandler, ITEM_ARRAYS_FACE_MAKEUP_INDEX, paramMap).sendToTarget();
                }
            });
        }
    }

    @Override
    public void setMakeupItemIntensity(final String name, final double density) {
        mMakeupParams.put(name, density);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX], name, density);
                } else {
                    selectMakeupItem(new HashMap<>(mMakeupParams), false);
                }
            }
        });
    }

    @Override
    public void setMakeupItemColor(final String name, final double[] colors) {
        mMakeupParams.put(name, colors);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX], name, colors);
                } else {
                    selectMakeupItem(new HashMap<>(mMakeupParams), false);
                }
            }
        });
    }

    @Override
    public void setBodySlimIntensity(final float intensity) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mBodySlimStrength = intensity;
                if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.BODY_SLIM_STRENGTH, intensity);
                }
            }
        });
    }

    @Override
    public void setLegSlimIntensity(final float intensity) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mLegSlimStrength = intensity;
                if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.LEG_SLIM_STRENGTH, intensity);
                }
            }
        });
    }

    @Override
    public void setWaistSlimIntensity(final float intensity) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mWaistSlimStrength = intensity;
                if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.WAIST_SLIM_STRENGTH, intensity);
                }
            }
        });
    }

    @Override
    public void setShoulderSlimIntensity(final float intensity) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mShoulderSlimStrength = intensity;
                if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.SHOULDER_SLIM_STRENGTH, intensity);
                }
            }
        });
    }

    @Override
    public void setHipSlimIntensity(final float intensity) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mHipSlimStrength = intensity;
                if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY], BeautifyBodyParam.HIP_SLIM_STRENGTH, intensity);
                }
            }
        });
    }

    public void loadAvatarBackground() {
        mNeedBackground = true;
        if (mFuItemHandler == null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_AVATAR_BACKGROUND);
                }
            });
        } else {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_AVATAR_BACKGROUND);
        }
    }

    public void unloadAvatarBackground() {
        mNeedBackground = false;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] > 0) {
                    faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND]);
                    mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] = 0;
                }
            }
        });
    }

    /**
     * 加载头发道具
     *
     * @param path 道具路径，如果为空就销毁
     */
    public void loadAvatarHair(final String path) {
        if (TextUtils.isEmpty(path)) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                        faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR]);
                        mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] = 0;
                    }
                }
            });
        } else {
            if (mFuItemHandler != null) {
                Message message = Message.obtain(mFuItemHandler, ITEM_ARRAYS_AVATAR_HAIR, path);
                mFuItemHandler.sendMessage(message);
            } else {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        Message message = Message.obtain(mFuItemHandler, ITEM_ARRAYS_AVATAR_HAIR, path);
                        mFuItemHandler.sendMessage(message);
                    }
                });
            }
        }
    }

    @Override
    public void onLightMakeupBatchSelected(List<MakeupItem> makeupItems) {
        Set<Integer> keySet = mLightMakeupItemMap.keySet();
        for (final Integer integer : keySet) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX], MakeupParamHelper.getMakeupIntensityKeyByType(integer), 0);
                }
            });
        }
        mLightMakeupItemMap.clear();

        if (makeupItems != null && makeupItems.size() > 0) {
            for (int i = 0, size = makeupItems.size(); i < size; i++) {
                MakeupItem makeupItem = makeupItems.get(i);
                onLightMakeupSelected(makeupItem, makeupItem.getLevel());
            }
        } else {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX], "is_makeup_on", 0);
                }
            });
        }
    }

    private void onLightMakeupSelected(final MakeupItem makeupItem, final float level) {
        int type = makeupItem.getType();
        MakeupItem mp = mLightMakeupItemMap.get(type);
        if (mp != null) {
            mp.setLevel(level);
        } else {
            // 复制一份
            mLightMakeupItemMap.put(type, makeupItem.cloneSelf());
        }
        if (mFuItemHandler == null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_LIGHT_MAKEUP_INDEX, makeupItem));
                }
            });
        } else {
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_LIGHT_MAKEUP_INDEX, makeupItem));
        }
    }

    @Override
    public void onLightMakeupOverallLevelChanged(final float level) {
        Set<Map.Entry<Integer, MakeupItem>> entries = mLightMakeupItemMap.entrySet();
        for (final Map.Entry<Integer, MakeupItem> entry : entries) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX], MakeupParamHelper.getMakeupIntensityKeyByType(entry.getKey()), level);
                    entry.getValue().setLevel(level);
                }
            });
        }
    }

    /**
     * 设置表情动图是否使用卡通点位，闭眼效果更好
     *
     * @param isCartoon
     */
    public void setIsCartoon(final boolean isCartoon) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX], "is_use_cartoon", isCartoon ? 1 : 0);
                }
            }
        });
    }

    /**
     * 海报换脸，输入人脸五官，自动变形调整
     *
     * @param value 范围 [0-1]，0 为关闭
     */
    public void fixPosterFaceParam(final float value) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX] > 0) {
                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX], "warp_intensity", value);
                }
            }
        });
    }

    //--------------------------------------IsTracking（人脸识别回调相关定义）----------------------------------------

    private int mTrackingStatus = 0;

    public interface OnTrackingStatusChangedListener {
        void onTrackingStatusChanged(int status);
    }

    private OnTrackingStatusChangedListener mOnTrackingStatusChangedListener;

    //--------------------------------------FaceUnitySystemError（faceunity错误信息回调相关定义）----------------------------------------

    public interface OnSystemErrorListener {
        void onSystemError(String error);
    }

    private OnSystemErrorListener mOnSystemErrorListener;


    //--------------------------------------OnBundleLoadCompleteListener（faceunity道具加载完成）----------------------------------------

    public void setOnBundleLoadCompleteListener(OnBundleLoadCompleteListener onBundleLoadCompleteListener) {
        mOnBundleLoadCompleteListener = onBundleLoadCompleteListener;
    }

    /**
     * 通过 fuCreateItemFromPackage 加载道具
     *
     * @param bundlePath 道具 bundle 的路径
     * @return 道具句柄，大于 0 表示加载成功
     */
    public int loadItem(String bundlePath) {
        if (TextUtils.isEmpty(bundlePath)) {
            return 0;
        }
        int item = 0;
        InputStream is = null;
        try {
            is = bundlePath.startsWith(Constant.filePath) ? new FileInputStream(new File(bundlePath)) : mContext.getAssets().open(bundlePath);
            byte[] itemData = new byte[is.available()];
            int len = is.read(itemData);
            item = faceunity.fuCreateItemFromPackage(itemData);
            Log.e(TAG, "bundle path: " + bundlePath + ", length: " + len + "Byte, handle:" + item);
        } catch (IOException e) {
            Log.e(TAG, "loadItem error ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        return item;
    }

    //--------------------------------------FPS（FPS相关定义）----------------------------------------

    private static final float NANO_IN_ONE_MILLI_SECOND = 1000000.0f;
    private static final float TIME = 5f;
    private int mCurrentFrameCnt = 0;
    private long mLastOneHundredFrameTimeStamp = 0;
    private long mOneHundredFrameFUTime = 0;
    private boolean mNeedBenchmark = true;
    private long mFuCallStartTime = 0;

    private OnFUDebugListener mOnFUDebugListener;

    public interface OnFUDebugListener {
        void onFpsChange(double fps, double renderTime);
    }

    private void benchmarkFPS() {
        if (!mNeedBenchmark) {
            return;
        }
        if (++mCurrentFrameCnt == TIME) {
            mCurrentFrameCnt = 0;
            long tmp = System.nanoTime();
            double fps = (1000.0f * NANO_IN_ONE_MILLI_SECOND / ((tmp - mLastOneHundredFrameTimeStamp) / TIME));
            mLastOneHundredFrameTimeStamp = tmp;
            double renderTime = mOneHundredFrameFUTime / TIME / NANO_IN_ONE_MILLI_SECOND;
            mOneHundredFrameFUTime = 0;

            if (mOnFUDebugListener != null) {
                mOnFUDebugListener.onFpsChange(fps, renderTime);
            }
        }
    }

    //--------------------------------------道具（异步加载道具）----------------------------------------

    public interface OnBundleLoadCompleteListener {
        /**
         * bundle 加载完成
         *
         * @param what
         */
        void onBundleLoadComplete(int what);
    }

    @IntDef(value = {HAIR_NORMAL, HAIR_GRADIENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HairType {
    }

    /**
     * 设置对道具设置相应的参数
     *
     * @param itemHandle
     */
    private void updateEffectItemParams(Effect effect, final int itemHandle) {
        if (effect == null || itemHandle == 0) {
            return;
        }
        if (mIsInputImage == 1) {
            faceunity.fuItemSetParam(itemHandle, "isAndroid", 0.0);
        } else {
            faceunity.fuItemSetParam(itemHandle, "isAndroid", 1.0);
        }
        int effectType = effect.effectType();
        if (effectType == Effect.EFFECT_TYPE_NORMAL) {
            //rotationAngle 参数是用于旋转普通道具
            faceunity.fuItemSetParam(itemHandle, "rotationAngle", 360 - mInputPropOrientation);
        }
        if (effectType == Effect.EFFECT_TYPE_BACKGROUND) {
            //计算角度（全屏背景分割，第一次未识别人脸）
            faceunity.fuSetDefaultRotationMode((360 - mInputImageOrientation) / 90);
        }
        int back = mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_BACK ? 1 : 0;
        if (effectType == Effect.EFFECT_TYPE_AVATAR) {
            // Avatar 头型和头发镜像
            faceunity.fuItemSetParam(itemHandle, "isFlipExpr", back);
            setAvatarHairParams(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR]);
        }

        if (effectType == Effect.EFFECT_TYPE_ANIMOJI || effectType == Effect.EFFECT_TYPE_PORTRAIT_DRIVE) {
            // 镜像顶点
            faceunity.fuItemSetParam(itemHandle, "is3DFlipH", back);
            // 镜像表情
            faceunity.fuItemSetParam(itemHandle, "isFlipExpr", back);
            //这两句代码用于识别人脸默认方向的修改，主要针对animoji道具的切换摄像头倒置问题
            faceunity.fuItemSetParam(itemHandle, "camera_change", 1.0);
            faceunity.fuSetDefaultRotationMode((360 - mInputImageOrientation) / 90);
        }

        if (effectType == Effect.EFFECT_TYPE_GESTURE) {
            //loc_y_flip与loc_x_flip 参数是用于对手势识别道具的镜像
            faceunity.fuItemSetParam(itemHandle, "is3DFlipH", back);
            faceunity.fuItemSetParam(itemHandle, "loc_y_flip", back);
            faceunity.fuItemSetParam(itemHandle, "loc_x_flip", back);
            faceunity.fuItemSetParam(itemHandle, "rotMode", mRotMode);
            int trackerOrientation = calculateFaceTrackerOrientation();
            faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "rotationMode", trackerOrientation);
        }

        if (effectType == Effect.EFFECT_TYPE_ANIMOJI) {
            // 镜像跟踪（位移和旋转）
            faceunity.fuItemSetParam(itemHandle, "isFlipTrack", back);
            // 镜像灯光
            faceunity.fuItemSetParam(itemHandle, "isFlipLight ", back);
            // 设置 Animoji 跟随人脸
            faceunity.fuItemSetParam(itemHandle, "{\"thing\":\"<global>\",\"param\":\"follow\"}", 1);
        }
        setMaxFaces(effect.maxFace());
    }

    private void setAvatarHairParams(int itemAvatarHair) {
        if (itemAvatarHair > 0) {
            int back = mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_BACK ? 1 : 0;
            faceunity.fuItemSetParam(itemAvatarHair, "is3DFlipH", back);
            faceunity.fuItemSetParam(itemAvatarHair, "isFlipTrack", back);
        }
    }

    /*----------------------------------Builder---------------------------------------*/

    /**
     * FURenderer Builder
     */
    public static class Builder {

        private boolean createEGLContext = false;
        private Effect defaultEffect;
        private int maxFaces = 1;
        private Context context;
        private int inputTextureType = 0;
        private int inputImageFormat = 0;
        private int inputImageRotation = 270;
        private int inputPropRotation = 270;
        private int isIputImage = 0;
        private boolean isNeedAnimoji3D = false;
        private boolean isNeedBeautyHair = false;
        private boolean isNeedFaceBeauty = true;
        private boolean isNeedPosterFace = false;
        private int filterStyle = CartoonFilter.NO_FILTER;
        private int currentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
        private OnBundleLoadCompleteListener onBundleLoadCompleteListener;
        private OnFUDebugListener onFUDebugListener;
        private OnTrackingStatusChangedListener onTrackingStatusChangedListener;
        private OnSystemErrorListener onSystemErrorListener;
        private boolean useBeautifyBody = false;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * 是否需要自己创建EGLContext
         *
         * @param createEGLContext
         * @return
         */
        public Builder createEGLContext(boolean createEGLContext) {
            this.createEGLContext = createEGLContext;
            return this;
        }

        /**
         * 是否需要立即加载道具
         *
         * @param defaultEffect
         * @return
         */
        public Builder defaultEffect(Effect defaultEffect) {
            this.defaultEffect = defaultEffect;
            return this;
        }


        /**
         * 输入的是否是图片
         *
         * @param isIputImage
         * @return
         */
        public Builder inputIsImage(int isIputImage) {
            this.isIputImage = isIputImage;
            return this;
        }

        /**
         * 识别最大人脸数
         *
         * @param maxFaces
         * @return
         */
        public Builder maxFaces(int maxFaces) {
            this.maxFaces = maxFaces;
            return this;
        }

        /**
         * 是否使用美体
         *
         * @param useBeautBody
         * @return
         */
        public Builder setUseBeautifyBody(boolean useBeautBody) {
            this.useBeautifyBody = useBeautBody;
            return this;
        }

        /**
         * 传入纹理的类型（传入数据没有纹理则无需调用）
         * camera OES纹理：1
         * 普通2D纹理：2
         *
         * @param textureType
         * @return
         */
        public Builder inputTextureType(int textureType) {
            this.inputTextureType = textureType;
            return this;
        }

        /**
         * 输入的byte[]数据类型
         *
         * @param inputImageFormat
         * @return
         */
        public Builder inputImageFormat(int inputImageFormat) {
            this.inputImageFormat = inputImageFormat;
            return this;
        }

        /**
         * 输入的画面数据方向
         *
         * @param inputImageRotation
         * @return
         */
        public Builder inputImageOrientation(int inputImageRotation) {
            this.inputImageRotation = inputImageRotation;
            return this;
        }

        /**
         * 道具方向
         *
         * @param inputPropRotation
         * @return
         */
        public Builder inputPropOrientation(int inputPropRotation) {
            this.inputPropRotation = inputPropRotation;
            return this;
        }

        /**
         * 是否需要3D道具的抗锯齿功能
         *
         * @param needAnimoji3D
         * @return
         */
        public Builder setNeedAnimoji3D(boolean needAnimoji3D) {
            this.isNeedAnimoji3D = needAnimoji3D;
            return this;
        }

        /**
         * 是否需要美发功能
         *
         * @param needBeautyHair
         * @return
         */
        public Builder setNeedBeautyHair(boolean needBeautyHair) {
            isNeedBeautyHair = needBeautyHair;
            return this;
        }

        /**
         * 是否需要美颜效果
         *
         * @param needFaceBeauty
         * @return
         */
        public Builder setNeedFaceBeauty(boolean needFaceBeauty) {
            isNeedFaceBeauty = needFaceBeauty;
            return this;
        }

        /**
         * 设置默认动漫滤镜
         *
         * @param filterStyle
         * @return
         */
        public Builder setFilterStyle(int filterStyle) {
            this.filterStyle = filterStyle;
            return this;
        }

        /**
         * 是否需要海报换脸
         *
         * @param needPosterFace
         * @return
         */
        public Builder setNeedPosterFace(boolean needPosterFace) {
            isNeedPosterFace = needPosterFace;
            return this;
        }

        /**
         * 当前的摄像头（前后置摄像头）
         *
         * @param cameraType
         * @return
         */
        public Builder setCurrentCameraType(int cameraType) {
            currentCameraType = cameraType;
            return this;
        }

        /**
         * 设置debug数据回调
         *
         * @param onFUDebugListener
         * @return
         */
        public Builder setOnFUDebugListener(OnFUDebugListener onFUDebugListener) {
            this.onFUDebugListener = onFUDebugListener;
            return this;
        }

        /**
         * 设置是否检查到人脸的回调
         *
         * @param onTrackingStatusChangedListener
         * @return
         */
        public Builder setOnTrackingStatusChangedListener(OnTrackingStatusChangedListener onTrackingStatusChangedListener) {
            this.onTrackingStatusChangedListener = onTrackingStatusChangedListener;
            return this;
        }

        /**
         * 设置bundle加载完成回调
         *
         * @param onBundleLoadCompleteListener
         * @return
         */
        public Builder setOnBundleLoadCompleteListener(OnBundleLoadCompleteListener onBundleLoadCompleteListener) {
            this.onBundleLoadCompleteListener = onBundleLoadCompleteListener;
            return this;
        }


        /**
         * 设置SDK使用错误回调
         *
         * @param onSystemErrorListener
         * @return
         */
        public Builder setOnSystemErrorListener(OnSystemErrorListener onSystemErrorListener) {
            this.onSystemErrorListener = onSystemErrorListener;
            return this;
        }

        public FURenderer build() {
            FURenderer fuRenderer = new FURenderer(context, createEGLContext);
            fuRenderer.mMaxFaces = maxFaces;
            fuRenderer.mInputTextureType = inputTextureType;
            fuRenderer.mInputImageFormat = inputImageFormat;
            fuRenderer.mInputImageOrientation = inputImageRotation;
            fuRenderer.mInputPropOrientation = inputPropRotation;
            fuRenderer.mIsInputImage = isIputImage;
            fuRenderer.mDefaultEffect = defaultEffect;
            fuRenderer.isNeedAnimoji3D = isNeedAnimoji3D;
            fuRenderer.isNeedBeautyHair = isNeedBeautyHair;
            fuRenderer.isNeedFaceBeauty = isNeedFaceBeauty;
            fuRenderer.isNeedPosterFace = isNeedPosterFace;
            fuRenderer.mCurrentCameraType = currentCameraType;
            fuRenderer.mComicFilterStyle = filterStyle;
            fuRenderer.mUseBeautifyBody = useBeautifyBody;

            fuRenderer.mOnFUDebugListener = onFUDebugListener;
            fuRenderer.mOnTrackingStatusChangedListener = onTrackingStatusChangedListener;
            fuRenderer.mOnSystemErrorListener = onSystemErrorListener;
            fuRenderer.mOnBundleLoadCompleteListener = onBundleLoadCompleteListener;
            return fuRenderer;
        }
    }

    /**
     * 美体道具参数
     */
    public class BeautifyBodyParam {
        /**
         * 0.0~1.0，值越大，瘦身幅度越大，0.0为不变形
         */
        public static final String BODY_SLIM_STRENGTH = "BodySlimStrength";
        /**
         * 0.0~1.0，值越大，腿拉伸幅度越大，0.0为不变形
         */
        public static final String LEG_SLIM_STRENGTH = "LegSlimStrength";
        /**
         * 0.0~1.0，值越大，瘦腰幅度越大，0.0为不变形
         */
        public static final String WAIST_SLIM_STRENGTH = "WaistSlimStrength";
        /**
         * 0.0~1.0，小于0.5肩膀变窄，大于0.5肩膀变宽，0.5为不变形
         */
        public static final String SHOULDER_SLIM_STRENGTH = "ShoulderSlimStrength";
        /**
         * 0.0~1.0，值越大，臀部变宽上提越大，0.0为不变形
         */
        public static final String HIP_SLIM_STRENGTH = "HipSlimStrength";
        /**
         * 清除所有身体变形
         */
        public static final String CLEAR_SLIM = "ClearSlim";
        /**
         * 设置相机方向 0, 1, 2, 3
         */
        public static final String ORIENTATION = "Orientation";
        /**
         * 参数 0.0 或者 1.0,  0.0 为关闭点位绘制，1.0 为打开，默认打开
         */
        public static final String DEBUG = "Debug";
    }

    /**
     * Avatar 捏脸输入参数
     */
    static class AvatarConstant {
        public static final int EXPRESSION_LENGTH = 46;
        public static final float[] ROTATION_DATA = new float[]{0f, 0f, 0f, 1f};
        public static final float[] PUP_POS_DATA = new float[]{0f, 0f};
        public static final int VALID_DATA = 1;
        public static final float[] EXPRESSIONS = new float[EXPRESSION_LENGTH];
    }

//--------------------------------------Builder----------------------------------------

    class FUItemHandler extends Handler {

        FUItemHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //加载普通道具 bundle
                case ITEM_ARRAYS_EFFECT_INDEX: {
                    final Effect effect = (Effect) msg.obj;
                    if (effect == null) {
                        return;
                    }
                    boolean isNone = effect.effectType() == Effect.EFFECT_TYPE_NONE;
                    final int itemEffect = isNone ? 0 : loadItem(effect.path());
                    if (!isNone && itemEffect <= 0) {
                        Log.w(TAG, "create effect item failed: " + itemEffect);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
                                mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = 0;
                            }
                            if (mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] > 0 && !mNeedBackground) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND]);
                                mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] = 0;
                            }
                            if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR]);
                                mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] = 0;
                            }
                            if (itemEffect > 0) {
                                updateEffectItemParams(effect, itemEffect);
                            }
                            mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = itemEffect;
                        }
                    });
                }
                break;
                //加载美颜 bundle
                case ITEM_ARRAYS_FACE_BEAUTY_INDEX: {
                    final int itemBeauty = loadItem(BUNDLE_FACE_BEAUTIFICATION);
                    if (itemBeauty <= 0) {
                        Log.w(TAG, "load face beauty item failed: " + itemBeauty);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX]);
                                mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = 0;
                            }
                            mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = itemBeauty;
                            isNeedUpdateFaceBeauty = true;
                        }
                    });
                }
                break;
                // 加载轻美妆 bundle
                case ITEM_ARRAYS_LIGHT_MAKEUP_INDEX: {
                    final MakeupItem makeupItem = (MakeupItem) msg.obj;
                    if (makeupItem == null) {
                        return;
                    }
                    String path = makeupItem.getPath();
                    if (!TextUtils.isEmpty(path)) {
                        if (mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX] <= 0) {
                            int itemLightMakeup = loadItem(BUNDLE_LIGHT_MAKEUP);
                            if (itemLightMakeup <= 0) {
                                Log.w(TAG, "create light makeup item failed: " + itemLightMakeup);
                                return;
                            }
                            mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX] = itemLightMakeup;
                        }
                        MakeupParamHelper.TextureImage textureImage = null;
                        if (makeupItem.getType() == FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK) {
                            mLipStickColor = MakeupParamHelper.readRgbaColor(mContext, path);
                        } else {
                            textureImage = MakeupParamHelper.createTextureImage(mContext, path);
                        }
                        final MakeupParamHelper.TextureImage finalTextureImage = textureImage;
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                int itemHandle = mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX];
                                faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.IS_MAKEUP_ON, 1.0);
                                faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY, 1.0);
                                faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.REVERSE_ALPHA, 1.0);
                                if (mLipStickColor != null) {
                                    if (makeupItem.getType() == FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK) {
                                        faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.MAKEUP_LIP_COLOR, mLipStickColor);
                                        faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.MAKEUP_LIP_MASK, 1.0);
                                    }
                                } else {
                                    faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY_LIP, 0.0);
                                }
                                if (finalTextureImage != null) {
                                    String key = MakeupParamHelper.getMakeupTextureKeyByType(makeupItem.getType());
                                    faceunity.fuCreateTexForItem(itemHandle, key, finalTextureImage.getBytes(), finalTextureImage.getWidth(), finalTextureImage.getHeight());
                                }
                                faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.getMakeupIntensityKeyByType(makeupItem.getType()), makeupItem.getLevel());
                            }
                        });
                    } else {
                        // 卸某个妆
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                if (mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX] > 0) {
                                    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIGHT_MAKEUP_INDEX],
                                            MakeupParamHelper.getMakeupIntensityKeyByType(makeupItem.getType()), 0.0);
                                }
                            }
                        });
                    }
                }
                break;
                // 加载美妆 bundle
                case ITEM_ARRAYS_FACE_MAKEUP_INDEX: {
                    final Map<String, Object> paramMap = (Map<String, Object>) msg.obj;
                    if (paramMap == null) {
                        return;
                    }
                    // 预先加载 209 点位的人脸识别库
                    if (mItemsArray[ITEM_ARRAYS_NEW_FACE_TRACKER] <= 0) {
                        int itemNewFaceTracker = loadItem(BUNDLE_NEW_FACE_TRACKER);
                        if (itemNewFaceTracker <= 0) {
                            Log.w(TAG, "create new face tracker item failed: " + itemNewFaceTracker);
                            return;
                        }
                        mItemsArray[ITEM_ARRAYS_NEW_FACE_TRACKER] = itemNewFaceTracker;
                        setFaceTrackerOrientation(calculateFaceTrackerOrientation());
                    }
                    if (mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] <= 0) {
                        int itemFaceMakeup = loadItem(BUNDLE_FACE_MAKEUP);
                        if (itemFaceMakeup <= 0) {
                            Log.w(TAG, "create face makeup item failed: " + itemFaceMakeup);
                            return;
                        }
                        mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] = itemFaceMakeup;
                    }
                    final Set<Map.Entry<String, Object>> paramEntries = paramMap.entrySet();
                    final Map<String, MakeupParamHelper.TextureImage> texImageMap = new HashMap<>(16);
                    for (Map.Entry<String, Object> entry : paramEntries) {
                        Object value = entry.getValue();
                        if (value instanceof String) {
//                            Log.v(TAG, "createTextureImage: " + value);
                            MakeupParamHelper.TextureImage texParams = MakeupParamHelper.createTextureImage(mContext, (String) value);
                            if (texParams != null) {
                                texImageMap.put(entry.getKey(), texParams);
                            }
                        }
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            int itemHandle = mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX];
                            faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.IS_MAKEUP_ON, 1.0);
                            faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.REVERSE_ALPHA, 1.0);
                            faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.MAKEUP_LIP_MASK, 1.0);
                            faceunity.fuItemSetParam(itemHandle, MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY, 1.0);
                            for (Map.Entry<String, Object> entry : paramEntries) {
                                Object value = entry.getValue();
                                String key = entry.getKey();
                                if (value instanceof double[]) {
                                    double[] val = (double[]) value;
//                                    Log.v(TAG, "run: setParams double array:" + key + ", value:" + Arrays.toString(val));
                                    faceunity.fuItemSetParam(itemHandle, key, val);
                                } else if (value instanceof Double) {
                                    Double val = (Double) value;
//                                    Log.v(TAG, "run: setParams double:" + key + ", value:" + val);
                                    faceunity.fuItemSetParam(itemHandle, key, val);
                                }
                            }
                            Set<Map.Entry<String, MakeupParamHelper.TextureImage>> texEntries = texImageMap.entrySet();
                            for (Map.Entry<String, MakeupParamHelper.TextureImage> texEntry : texEntries) {
                                MakeupParamHelper.TextureImage value = texEntry.getValue();
                                String key = texEntry.getKey();
//                                Log.v(TAG, "run: setParams:" + key + ", tex:" + value);
                                faceunity.fuCreateTexForItem(itemHandle, key, value.getBytes(), value.getWidth(), value.getHeight());
                            }
                        }
                    });
                }
                break;
                //加载普通美发 bundle
                case ITEM_ARRAYS_HAIR_NORMAL_INDEX: {
                    final int itemHair = loadItem(BUNDLE_HAIR_NORMAL);
                    if (itemHair <= 0) {
                        Log.w(TAG, "create hair normal item failed: " + itemHair);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX]);
                                mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX] = 0;
                            }
                            if (mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX]);
                                mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX] = 0;
                            }
                            // 美发类型
                            faceunity.fuItemSetParam(itemHair, "Index", mHairColorIndex);
                            // 美发强度
                            faceunity.fuItemSetParam(itemHair, "Strength", mHairColorStrength);
                            mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX] = itemHair;
                        }
                    });
                }
                break;
                // 加载渐变美发 bundle
                case ITEM_ARRAYS_HAIR_GRADIENT_INDEX: {
                    final int itemHair = loadItem(BUNDLE_HAIR_GRADIENT);
                    if (itemHair <= 0) {
                        Log.w(TAG, "create hair gradient item failed: " + itemHair);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX]);
                                mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX] = 0;
                            }
                            if (mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX]);
                                mItemsArray[ITEM_ARRAYS_HAIR_NORMAL_INDEX] = 0;
                            }
                            faceunity.fuItemSetParam(itemHair, "Index", mHairColorIndex);
                            faceunity.fuItemSetParam(itemHair, "Strength", mHairColorStrength);
                            mItemsArray[ITEM_ARRAYS_HAIR_GRADIENT_INDEX] = itemHair;
                        }
                    });
                }
                break;
                // 加载头发模型 bundle
                case ITEM_ARRAYS_HAIR_MODEL: {
                    final int itemModel = loadItem(BUNDLE_HAIR_MODEL_HIGH_SPEED);
                    if (itemModel < 0) {
                        Log.w(TAG, "create hair model failed: " + itemModel);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_HAIR_MODEL] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_HAIR_MODEL]);
                                mItemsArray[ITEM_ARRAYS_HAIR_MODEL] = 0;
                            }
                            mItemsArray[ITEM_ARRAYS_HAIR_MODEL] = itemModel;
                        }
                    });
                }
                break;
                // 加载 Animoji 风格滤镜 bundle
                case ITEM_ARRAYS_CARTOON_FILTER_INDEX: {
                    final int style = (int) msg.obj;
                    if (style >= 0) {
                        if (mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX] <= 0) {
                            int itemCartoonFilter = loadItem(BUNDLE_CARTOON_FILTER);
                            if (itemCartoonFilter <= 0) {
                                Log.w(TAG, "create cartoon filter item failed: " + itemCartoonFilter);
                                return;
                            }
                            mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX] = itemCartoonFilter;
                        }
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX], "style", style);
                                GlUtil.logVersionInfo();
                                int glMajorVersion = GlUtil.getGlMajorVersion();
                                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX], "glVer", glMajorVersion);
                            }
                        });
                    } else {
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                if (mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX] > 0) {
                                    faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX]);
                                    mItemsArray[ITEM_ARRAYS_CARTOON_FILTER_INDEX] = 0;
                                }
                            }
                        });
                    }
                }
                break;
                // 加载表情动图 bundle
                case ITEM_ARRAYS_LIVE_PHOTO_INDEX: {
                    final LivePhoto livePhoto = (LivePhoto) msg.obj;
                    if (livePhoto == null) {
                        return;
                    }
                    if (mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX] <= 0) {
                        int itemLivePhoto = loadItem(BUNDLE_LIVE_PHOTO);
                        if (itemLivePhoto <= 0) {
                            Log.w(TAG, "create live photo item failed: " + itemLivePhoto);
                            return;
                        }
                        mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX] = itemLivePhoto;
                    }
                    setIsFrontCamera(mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT);

                    final MakeupParamHelper.TextureImage textureImage = MakeupParamHelper.createTextureImage(mContext, livePhoto.getTemplateImagePath());
                    if (textureImage != null) {
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                int itemHandle = mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX];
                                // 设置五官类型
                                faceunity.fuItemSetParam(itemHandle, "group_type", livePhoto.getGroupType());
                                // 设置五官点位
                                faceunity.fuItemSetParam(itemHandle, "group_points", livePhoto.getGroupPoints());
                                // 输入图片的宽
                                faceunity.fuItemSetParam(itemHandle, "target_width", textureImage.getWidth());
                                // 输入图片的高
                                faceunity.fuItemSetParam(itemHandle, "target_height", textureImage.getHeight());
                                // 输入图片的 RGBA byte 数组
                                faceunity.fuCreateTexForItem(itemHandle, "tex_input", textureImage.getBytes(), textureImage.getWidth(), textureImage.getHeight());
                                // 设置插值开关
                                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_LIVE_PHOTO_INDEX], "use_interpolate2", 0.0);
                                // 设置使用卡通点位运行
                                setIsCartoon(true);
                            }
                        });
                    }
                }
                break;
                // 美体
                case ITEM_ARRAYS_BEAUTIFY_BODY: {
                    final int itemBeautifyBody = loadItem(BUNDLE_BEAUTIFY_BODY);
                    if (itemBeautifyBody <= 0) {
                        Log.w(TAG, "create beautify body item failed: " + itemBeautifyBody);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY]);
                                mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] = 0;
                            }
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.BODY_SLIM_STRENGTH, mBodySlimStrength);
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.LEG_SLIM_STRENGTH, mLegSlimStrength);
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.WAIST_SLIM_STRENGTH, mWaistSlimStrength);
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.SHOULDER_SLIM_STRENGTH, mShoulderSlimStrength);
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.HIP_SLIM_STRENGTH, mHipSlimStrength);
                            int orientation;
                            if (mIsInputImage == 1) {
                                orientation = 0;
                            } else {
                                orientation = calculateFaceTrackerOrientation();
                            }
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.DEBUG, 0.0);
                            faceunity.fuItemSetParam(itemBeautifyBody, BeautifyBodyParam.ORIENTATION, orientation);
                            mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] = itemBeautifyBody;
                        }
                    });
                }
                break;
                // 加载 Avatar 捏脸的头发 bundle
                case ITEM_ARRAYS_AVATAR_HAIR: {
                    String path = (String) msg.obj;
                    if (!TextUtils.isEmpty(path)) {
                        final int itemAvatarHair = loadItem(path);
                        if (itemAvatarHair <= 0) {
                            Log.w(TAG, "create avatar hair item failed: " + itemAvatarHair);
                            return;
                        }
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                int oldItem = mItemsArray[ITEM_ARRAYS_AVATAR_HAIR];
                                setAvatarHairParams(itemAvatarHair);
                                mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] = itemAvatarHair;
                                if (oldItem > 0) {
                                    faceunity.fuDestroyItem(oldItem);
                                }
                            }
                        });
                    } else {
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                if (mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] > 0) {
                                    faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_AVATAR_HAIR]);
                                    mItemsArray[ITEM_ARRAYS_AVATAR_HAIR] = 0;
                                }
                            }
                        });
                    }
                }
                break;
                // 加载 Avatar 捏脸的背景 bundle
                case ITEM_ARRAYS_AVATAR_BACKGROUND: {
                    final int itemAvatarBg = loadItem(BUNDLE_AVATAR_BACKGROUND);
                    if (itemAvatarBg <= 0) {
                        Log.w(TAG, "create avatar background item failed: " + itemAvatarBg);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND]);
                                mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] = 0;
                            }
                            mItemsArray[ITEM_ARRAYS_AVATAR_BACKGROUND] = itemAvatarBg;
                        }
                    });
                }
                break;
                // 加载 Animoji 道具3D抗锯齿 bundle
                case ITEM_ARRAYS_ABIMOJI_3D_INDEX: {
                    final int itemAnimoji3D = loadItem(BUNDLE_FXAA);
                    if (itemAnimoji3D <= 0) {
                        Log.w(TAG, "create Animoji3D item failed: " + itemAnimoji3D);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_ABIMOJI_3D_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_ABIMOJI_3D_INDEX]);
                                mItemsArray[ITEM_ARRAYS_ABIMOJI_3D_INDEX] = 0;
                            }
                            mItemsArray[ITEM_ARRAYS_ABIMOJI_3D_INDEX] = itemAnimoji3D;
                        }
                    });
                }
                break;
                default:
            }
            if (mOnBundleLoadCompleteListener != null) {
                mOnBundleLoadCompleteListener.onBundleLoadComplete(msg.what);
            }
        }
    }
}
