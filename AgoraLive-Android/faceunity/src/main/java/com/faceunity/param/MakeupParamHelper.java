package com.faceunity.param;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.entity.FaceMakeup;
import com.faceunity.utils.BitmapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * 美妆参数，包含质感美颜和美妆
 *
 * @author Richie on 2019.05.27
 */
public class MakeupParamHelper {
    private static final String TAG = "MakeupParamHelper";

    /**
     * 加载美妆贴图资源，返回图像的字节数组和宽高。
     *
     * @param context
     * @param resourcePath
     * @return TextureImage: width, height and bytes
     */
    public static TextureImage createTextureImage(Context context, String resourcePath) {
        if (TextUtils.isEmpty(resourcePath)) {
            return null;
        }

        InputStream is = null;
        Bitmap bitmap;
        try {
            is = context.getAssets().open(resourcePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            // open assets failed, then try sdcard
            Log.w(TAG, "createTextureImage: ", e);
            bitmap = BitmapFactory.decodeFile(resourcePath);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            byte[] bitmapBytes = BitmapUtil.copyRgbaByteFromBitmap(bitmap);
            return new TextureImage(width, height, bitmapBytes);
        }
        return null;
    }

    /**
     * 根据轻美妆类型，获取纹理贴图关键字
     *
     * @param type
     * @return
     */
    public static String getMakeupTextureKeyByType(int type) {
        switch (type) {
            case FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK:
                return "tex_lip";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER:
                return "tex_eyeLiner";
            case FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER:
                return "tex_blusher";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL:
                return "tex_pupil";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW:
                return "tex_brow";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW:
                return "tex_eye";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYELASH:
                return "tex_eyeLash";
            default:
                return "";
        }
    }

    /**
     * 根据轻美妆类型，获取妆容强度关键字
     *
     * @param type
     * @return
     */
    public static String getMakeupIntensityKeyByType(int type) {
        switch (type) {
            case FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK:
                return "makeup_intensity_lip";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER:
                return "makeup_intensity_eyeLiner";
            case FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER:
                return "makeup_intensity_blusher";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL:
                return "makeup_intensity_pupil";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW:
                return "makeup_intensity_eyeBrow";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW:
                return "makeup_intensity_eye";
            case FaceMakeup.FACE_MAKEUP_TYPE_EYELASH:
                return "makeup_intensity_eyelash";
            default:
                return "";
        }
    }

    /**
     * 读取 RGBA 颜色数据
     *
     * @param context
     * @param colorAssetPath
     * @return
     */
    public static double[] readRgbaColor(Context context, String colorAssetPath) {
        if (TextUtils.isEmpty(colorAssetPath)) {
            return null;
        }

        InputStream is = null;
        try {
            is = context.getAssets().open(colorAssetPath);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            JSONObject jsonObject = new JSONObject(new String(bytes));
            JSONArray jsonArray = jsonObject.optJSONArray("rgba");
            double[] colors = new double[jsonArray.length()];
            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                colors[i] = jsonArray.optDouble(i);
            }
            return colors;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "readMakeupLipColors: ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        return null;
    }

    public static final class TextureImage {
        private int width;
        private int height;
        private byte[] bytes;

        TextureImage(int width, int height, byte[] bytes) {
            this.width = width;
            this.height = height;
            this.bytes = bytes;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public String toString() {
            return "TextureImage{" +
                    "width=" + width +
                    ", height=" + height +
                    ", bytes=" + bytes +
                    '}';
        }
    }

    public static final class MakeupParam {
        /**
         * tex_ 开头的参数表示 fuCreateTexForItem 方法的 name 参数
         */
        public static final String TEX_BROW = "tex_brow";
        public static final String TEX_EYE = "tex_eye";
        public static final String TEX_EYE2 = "tex_eye2";
        public static final String TEX_EYE3 = "tex_eye3";
        public static final String TEX_PUPIL = "tex_pupil";
        public static final String TEX_EYE_LASH = "tex_eyeLash";
        public static final String TEX_EYE_LINER = "tex_eyeLiner";
        public static final String TEX_BLUSHER = "tex_blusher";
        public static final String TEX_FOUNDATION = "tex_foundation";
        public static final String TEX_HIGHLIGHT = "tex_highlight";
        public static final String TEX_SHADOW = "tex_shadow";
        /*非参数值，App 内使用*/
        public static final String TEX_PREFIX = "tex_";
        /**
         * 是否使用双色口红，1 为开，0 为关
         */
        public static final String IS_TWO_COLOR = "is_two_color";
        /**
         * 口红类型，0 雾面，1 缎面
         */
        public static final String LIP_TYPE = "lip_type";
        /**
         * 嘴唇优化效果开关，1 为开，0 为关
         */
        public static final String MAKEUP_LIP_MASK = "makeup_lip_mask";
        /**
         * alpha 值逆向，1 为开，0 为关
         */
        public static final String REVERSE_ALPHA = "reverse_alpha";
        /**
         * 是否使用眉毛变形，1 为开，0 为关
         */
        public static final String BROW_WARP = "brow_warp";
        /**
         * 眉毛变形类型
         */
        public static final String BROW_WARP_TYPE = "brow_warp_type";
        /**
         * 柳叶眉
         */
        public static final double BROW_WARP_TYPE_WILLOW = 0.0;
        /**
         * 一字眉
         */
        public static final double BROW_WARP_TYPE_ONE_WORD = 1.0;
        /**
         * 小山眉
         */
        public static final double BROW_WARP_TYPE_HILL = 2.0;
        /**
         * 标准眉
         */
        public static final double BROW_WARP_TYPE_STANDARD = 3.0;
        /**
         * 扶形眉
         */
        public static final double BROW_WARP_TYPE_SHAPE = 4.0;
        /**
         * 日常风
         */
        public static final double BROW_WARP_TYPE_DAILY = 5.0;
        /**
         * 日系风
         */
        public static final double BROW_WARP_TYPE_JAPAN = 6.0;

        /**
         * 下面是各个妆容的颜色值
         */
        public static final String MAKEUP_EYE_BROW_COLOR = "makeup_eyeBrow_color";
        public static final String MAKEUP_LIP_COLOR = "makeup_lip_color";
        public static final String MAKEUP_LIP_COLOR2 = "makeup_lip_color2";
        public static final String MAKEUP_EYE_COLOR = "makeup_eye_color";
        public static final String MAKEUP_EYE_LINER_COLOR = "makeup_eyeLiner_color";
        public static final String MAKEUP_EYELASH_COLOR = "makeup_eyelash_color";
        public static final String MAKEUP_BLUSHER_COLOR = "makeup_blusher_color";
        public static final String MAKEUP_FOUNDATION_COLOR = "makeup_foundation_color";
        public static final String MAKEUP_HIGHLIGHT_COLOR = "makeup_highlight_color";
        public static final String MAKEUP_SHADOW_COLOR = "makeup_shadow_color";
        public static final String MAKEUP_PUPIL_COLOR = "makeup_pupil_color";
        /**
         * 美妆开关，1 为开，0 为关
         */
        public static final String IS_MAKEUP_ON = "is_makeup_on";
        /**
         * 全局妆容强度，范围 [0-1]
         */
        public static final String MAKEUP_INTENSITY = "makeup_intensity";
        /**
         * 下面是各个妆容强度参数，范围 [0-1]
         */
        public static final String MAKEUP_INTENSITY_LIP = "makeup_intensity_lip";
        public static final String MAKEUP_INTENSITY_EYE_LINER = "makeup_intensity_eyeLiner";
        public static final String MAKEUP_INTENSITY_BLUSHER = "makeup_intensity_blusher";
        public static final String MAKEUP_INTENSITY_PUPIL = "makeup_intensity_pupil";
        public static final String MAKEUP_INTENSITY_EYE_BROW = "makeup_intensity_eyeBrow";
        public static final String MAKEUP_INTENSITY_EYE = "makeup_intensity_eye";
        public static final String MAKEUP_INTENSITY_EYELASH = "makeup_intensity_eyelash";
        public static final String MAKEUP_INTENSITY_FOUNDATION = "makeup_intensity_foundation";
        public static final String MAKEUP_INTENSITY_HIGHLIGHT = "makeup_intensity_highlight";
        public static final String MAKEUP_INTENSITY_SHADOW = "makeup_intensity_shadow";
        /*非参数值，App 内使用*/
        public static final String MAKEUP_INTENSITY_PREFIX = "makeup_intensity_";

        public static final String[] MAKEUP_INTENSITIES = {MAKEUP_INTENSITY_LIP, MAKEUP_INTENSITY_EYE_LINER,
                MAKEUP_INTENSITY_BLUSHER, MAKEUP_INTENSITY_PUPIL, MAKEUP_INTENSITY_EYE_BROW, MAKEUP_INTENSITY_EYE,
                MAKEUP_INTENSITY_EYELASH, MAKEUP_INTENSITY_FOUNDATION, MAKEUP_INTENSITY_HIGHLIGHT, MAKEUP_INTENSITY_SHADOW};
    }
}
