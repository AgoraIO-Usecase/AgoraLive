package com.faceunity;


import com.faceunity.entity.Effect;
import com.faceunity.entity.LivePhoto;
import com.faceunity.entity.MakeupItem;

import java.util.List;
import java.util.Map;

/**
 * FURenderer与界面之间的交互接口
 */
public interface OnFUControlListener {

    /**
     * 音乐滤镜时间
     *
     * @param time
     */
    void onMusicFilterTime(long time);

    /**
     * 道具贴纸选择
     *
     * @param effectItemName 道具贴纸文件名
     */
    void onEffectSelected(Effect effectItemName);

    /**
     * 滤镜强度
     *
     * @param progress 滤镜强度
     */
    void onFilterLevelSelected(float progress);

    /**
     * 滤镜选择
     *
     * @param filterName 滤镜名称
     */
    void onFilterNameSelected(String filterName);

    /**
     * 美发颜色
     *
     * @param type
     * @param hairColorIndex 美发颜色
     * @param hairColorLevel 美发颜色强度
     */
    void onHairSelected(int type, int hairColorIndex, float hairColorLevel);

    /**
     * 调整美发强度
     *
     * @param type
     * @param hairColorIndex
     * @param hairColorLevel
     */
    void onHairLevelSelected(int type, int hairColorIndex, float hairColorLevel);

    /**
     * 精准磨皮
     *
     * @param isOpen 是否开启精准磨皮（0关闭 1开启）
     */
    void onSkinDetectSelected(float isOpen);

    /**
     * 磨皮类型
     *
     * @param blurType 0 清晰磨皮，1 重度磨皮，2 精细磨皮
     */
    void onBlurTypeSelected(float blurType);

    /**
     * 磨皮程度
     *
     * @param level 磨皮程度 [0, 6]，默认 6.0
     */
    void onBlurLevelSelected(float level);
    /**
     * 美白选择
     *
     * @param level 美白
     */
    void onColorLevelSelected(float level);

    /**
     * 红润
     */
    void onRedLevelSelected(float level);

    /**
     * 亮眼
     */
    void onEyeBrightSelected(float level);

    /**
     * 美牙
     */
    void onToothWhitenSelected(float level);

    /**
     * 大眼选择
     *
     * @param level 大眼
     */
    void onEyeEnlargeSelected(float level);

    /**
     * 瘦脸选择
     *
     * @param level 瘦脸
     */
    void onCheekThinningSelected(float level);

    /**
     * 下巴
     */
    void onIntensityChinSelected(float level);

    /**
     * 额头
     */
    void onIntensityForeheadSelected(float level);

    /**
     * 瘦鼻
     */
    void onIntensityNoseSelected(float level);


    /**
     * 嘴形
     */
    void onIntensityMouthSelected(float level);

    /**
     * 窄脸选择
     *
     * @param level
     */
    void onCheekNarrowSelected(float level);

    /**
     * 小脸选择
     *
     * @param level
     */
    void onCheekSmallSelected(float level);

    /**
     * V脸选择
     *
     * @param level
     */
    void onCheekVSelected(float level);

    /**
     * 切换海报模板
     *
     * @param tempWidth
     * @param tempHeight
     * @param temp
     * @param landmark
     */
    void onPosterTemplateSelected(int tempWidth, int tempHeight, byte[] temp, float[] landmark);

    /**
     * 海报换脸输入照片
     *
     * @param inputWidth
     * @param inputHeight
     * @param input
     * @param landmark
     */
    void onPosterInputPhoto(int inputWidth, int inputHeight, byte[] input, float[] landmark);

    /**
     * 设置风格滤镜
     *
     * @param style
     */
    void onCartoonFilterSelected(int style);

    /**
     * 调节多个妆容（轻美妆，质感美颜）
     *
     * @param makeupItems
     */
    void onLightMakeupBatchSelected(List<MakeupItem> makeupItems);

    /**
     * 妆容总体调节（轻美妆，质感美颜）
     *
     * @param level
     */
    void onLightMakeupOverallLevelChanged(float level);

    /**
     * 设置表情动图的点位和图像数据，用来驱动图像
     *
     * @param livePhoto
     */
    void setLivePhoto(LivePhoto livePhoto);

    /**
     * 选择美妆妆容
     *
     * @param paramMap
     * @param removePrevious
     */
    void selectMakeupItem(Map<String, Object> paramMap, boolean removePrevious);

    /**
     * 调节美妆妆容强度
     *
     * @param name
     * @param density
     */
    void setMakeupItemIntensity(String name, double density);

    /**
     * 设置美妆妆容颜色
     *
     * @param name
     * @param colors RGBA color
     */
    void setMakeupItemColor(String name, double[] colors);

    /**
     * 设置瘦身程度
     *
     * @param intensity
     */
    void setBodySlimIntensity(float intensity);

    /**
     * 设置长腿程度
     *
     * @param intensity
     */
    void setLegSlimIntensity(float intensity);

    /**
     * 设置细腰程度
     *
     * @param intensity
     */
    void setWaistSlimIntensity(float intensity);

    /**
     * 设置美肩程度
     *
     * @param intensity
     */
    void setShoulderSlimIntensity(float intensity);

    /**
     * 设置美臀程度
     *
     * @param intensity
     */
    void setHipSlimIntensity(float intensity);
}
