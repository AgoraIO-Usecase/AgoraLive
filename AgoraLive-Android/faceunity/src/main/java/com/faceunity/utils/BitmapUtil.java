package com.faceunity.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.util.Log;

import com.faceunity.gles.ProgramTexture2d;
import com.faceunity.gles.ProgramTextureOES;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tujh on 2018/6/28.
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * 读取图片（glReadPixels）
     *
     * @param textureId
     * @param mtx
     * @param mvp
     * @param texWidth
     * @param texHeight
     * @param listener
     * @param isOes     是否是OES纹理
     */
    public static void glReadBitmap(int textureId, float[] mtx, float[] mvp, final int texWidth, final int texHeight, final OnReadBitmapListener listener,
                                    boolean isOes) {
        final IntBuffer intBuffer = IntBuffer.allocate(texWidth * texHeight);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texWidth, texHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        int[] mFrameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures[0], 0);
        int viewport[] = new int[4];
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
        GLES20.glViewport(0, 0, texWidth, texHeight);
        if (isOes)
            new ProgramTextureOES().drawFrame(textureId, mtx, mvp);
        else
            new ProgramTexture2d().drawFrame(textureId, mtx, mvp);
        GLES20.glReadPixels(0, 0, texWidth, texHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
        GLES20.glFinish();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final int bitmapSource[] = new int[texWidth * texHeight];
                int offset1, offset2;
                int[] data = intBuffer.array();
                for (int i = 0; i < texHeight; i++) {
                    offset1 = i * texWidth;
                    offset2 = (texHeight - i - 1) * texWidth;
                    for (int j = 0; j < texWidth; j++) {
                        int texturePixel = data[offset1 + j];
                        int blue = (texturePixel >> 16) & 0xff;
                        int red = (texturePixel << 16) & 0x00ff0000;
                        int pixel = (texturePixel & 0xff00ff00) | red | blue;
                        bitmapSource[offset2 + j] = pixel;
                    }
                }
                final Bitmap shotCaptureBitmap = Bitmap.createBitmap(bitmapSource, texWidth, texHeight, Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true);
                if (listener != null) {
                    listener.onReadBitmapListener(shotCaptureBitmap);
                }
            }
        });
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteTextures(1, textures, 0);
        GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
    }

    /**
     * 加载本地图片
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap loadBitmap(String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = decodeSampledBitmapFromFile(path, reqWidth, reqHeight);
        int orientation = getPhotoOrientation(path);
        return rotateBitmap(bitmap, orientation);
    }

    /**
     * 旋转 Bitmap
     *
     * @param bitmap
     * @param orientation
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (orientation == 90 || orientation == 180 || orientation == 270) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * 获取图片的方向
     *
     * @param path
     * @return
     */
    public static int getPhotoOrientation(String path) {
        int orientation = 0;
        int tagOrientation = 0;
        try {
            tagOrientation = new ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        } catch (IOException e) {
            Log.e(TAG, "getPhotoOrientation: ", e);
        }
        if (tagOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            orientation = 90;
        } else if (tagOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            orientation = 180;
        } else if (tagOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            orientation = 270;
        }
        return orientation;
    }

    /**
     * load本地图片
     *
     * @param path
     * @param screenWidth
     * @return
     */
    public static Bitmap loadBitmap(String path, int screenWidth) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;
        opt.inSampleSize = 1;
        // 根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picHeight > screenWidth) {
                opt.inSampleSize = picHeight / screenWidth;
            }
        } else {
            if (picWidth > screenWidth) {
                opt.inSampleSize = picWidth / screenWidth;
            }
        }
        opt.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opt);
        int orientation = getPhotoOrientation(path);
        bitmap = rotateBitmap(bitmap, orientation);
        return bitmap;
    }

    /**
     * bitmap 转 NV21 数据
     *
     * @param inputWidth
     * @param inputHeight
     * @param scaled
     * @return
     */
    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[inputWidth * inputHeight];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        byte[] yuv = new byte[inputHeight * inputWidth + 2 * (int) Math.ceil((float) inputHeight / 2) * (int) Math.ceil((float) inputWidth / 2)];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        scaled.recycle();
        return yuv;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Why using double inputStream? one is for decoding bounds, and other is for decoding bitmap.
     *
     * @param isForBounds
     * @param isForData
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromStream(InputStream isForBounds, InputStream isForData, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isForBounds, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(isForData, null, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String imagePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources resource, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resource, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resource, resId, options);
    }

    /**
     * ARGB 转 NV21 数据
     *
     * @param yuv420sp
     * @param argb
     * @param width
     * @param height
     */
    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int A, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                A = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                index++;
            }
        }
    }

    /**
     * 获取 Bitmap 的宽高
     *
     * @param path
     * @return
     */
    public static Point getBitmapSize(String path) {
        Point point = new Point();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        point.x = opt.outWidth;
        point.y = opt.outHeight;
        return point;
    }

    /**
     * 获取 Bitmap 的 RGBA 字节数组
     *
     * @param bitmap
     * @return
     */
    public static byte[] copyRgbaByteFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        byte[] bytes = new byte[bitmap.getByteCount()];
        ByteBuffer rgbaBuffer = ByteBuffer.wrap(bytes);
        bitmap.copyPixelsToBuffer(rgbaBuffer);
        return bytes;
    }

    public interface OnReadBitmapListener {
        /**
         * 读取图片完成
         *
         * @param bitmap
         */
        void onReadBitmapListener(Bitmap bitmap);
    }

    /**
     * Return the clipped bitmap.
     *
     * @param src     The source of bitmap.
     * @param x       The x coordinate of the first pixel.
     * @param y       The y coordinate of the first pixel.
     * @param width   The width.
     * @param height  The height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the clipped bitmap
     */
    public static Bitmap clip(final Bitmap src,
                              final int x,
                              final int y,
                              final int width,
                              final int height,
                              final boolean recycle) {
        if (isEmptyBitmap(src))
            return null;
        Bitmap ret = Bitmap.createBitmap(src, x, y, width, height);
        if (recycle && !src.isRecycled() && ret != src)
            src.recycle();
        return ret;
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

}
