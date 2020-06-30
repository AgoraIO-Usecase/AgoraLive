package com.faceunity.param;

/**
 * 美颜道具参数，包含红润、美白、磨皮、滤镜、变形、亮眼、美牙等功能。
 *
 * @author Richie on 2019.07.18
 */
public class BeautificationParam {
    /**
     * 美颜参数全局开关，0 关，1 开，默认 1
     */
    public static final String IS_BEAUTY_ON = "is_beauty_on";
    /**
     * 滤镜名称，默认 origin
     */
    public static final String FILTER_NAME = "filter_name";
    /**
     * 滤镜程度，范围 [0-1]，默认 1
     */
    public static final String FILTER_LEVEL = "filter_level";
    /**
     * 美白程度，范围 [0-1]，默认 0.2
     */
    public static final String COLOR_LEVEL = "color_level";
    /**
     * 红润程度，范围 [0-1]，默认 0.5
     */
    public static final String RED_LEVEL = "red_level";
    /**
     * 磨皮程度，范围 [0-6]，默认 6
     */
    public static final String BLUR_LEVEL = "blur_level";
    /**
     * 磨皮类型，0 清晰磨皮，1 重度磨皮，2 精细磨皮
     */
    public static final String BLUR_TYPE = "blur_type";
    /**
     * 肤色检测开关，0 关，1 开，默认 0
     */
    public static final String SKIN_DETECT = "skin_detect";
    /**
     * 肤色检测开启后，非肤色区域的融合程度，范围 [0-1]，默认 0.45
     */
    public static final String NONSKIN_BLUR_SCALE = "nonskin_blur_scale";
    /**
     * 磨皮类型，0 清晰磨皮，1 重度磨皮，默认 1
     */
    public static final String HEAVY_BLUR = "heavy_blur";
    /**
     * 亮眼程度，范围 [0-1]，默认 1
     */
    public static final String EYE_BRIGHT = "eye_bright";
    /**
     * 美牙程度，范围 [0-1]，默认 1
     */
    public static final String TOOTH_WHITEN = "tooth_whiten";
    /**
     * 变形选择，0 女神，1 网红，2 自然，3 默认，4 精细变形，默认 3
     */
    public static final String FACE_SHAPE = "face_shape";
    /**
     * 变形程度，0-1，默认 1
     */
    public static final String FACE_SHAPE_LEVEL = "face_shape_level";
    /**
     * 大眼程度，范围 [0-1]，默认 0.5
     */
    public static final String EYE_ENLARGING = "eye_enlarging";
    /**
     * 瘦脸程度，范围 [0-1]，默认 0
     */
    public static final String CHEEK_THINNING = "cheek_thinning";
    /**
     * 窄脸程度，范围 [0-1]，默认 0
     */
    public static final String CHEEK_NARROW = "cheek_narrow";
    /**
     * 小脸程度，范围 [0-1]，默认 0
     */
    public static final String CHEEK_SMALL = "cheek_small";
    /**
     * V脸程度，范围 [0-1]，默认 0
     */
    public static final String CHEEK_V = "cheek_v";
    /**
     * 瘦鼻程度，范围 [0-1]，默认 0
     */
    public static final String INTENSITY_NOSE = "intensity_nose";
    /**
     * 嘴巴调整程度，范围 [0-1]，默认 0.5
     */
    public static final String INTENSITY_MOUTH = "intensity_mouth";
    /**
     * 额头调整程度，范围 [0-1]，默认 0.5
     */
    public static final String INTENSITY_FOREHEAD = "intensity_forehead";
    /**
     * 下巴调整程度，范围 [0-1]，默认 0.5
     */
    public static final String INTENSITY_CHIN = "intensity_chin";
    /**
     * 变形渐变调整参数，0 渐变关闭，大于 0 渐变开启，值为渐变需要的帧数
     */
    public static final String CHANGE_FRAMES = "change_frames";

    /**
     * 女神
     */
    public static final int FACE_SHAPE_GODDESS = 0;
    /**
     * 网红
     */
    public static final int FACE_SHAPE_NET_RED = 1;
    /**
     * 自然
     */
    public static final int FACE_SHAPE_NATURE = 2;
    /**
     * 默认
     */
    public static final int FACE_SHAPE_DEFAULT = 3;
    /**
     * 精细变形
     */
    public static final int FACE_SHAPE_CUSTOM = 4;
}
