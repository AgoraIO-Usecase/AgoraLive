package com.faceunity.entity;

import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

/**
 * 新版单项妆容
 *
 * @author Richie on 2019.06.11
 */
public class NewMakeupItem {
    // 默认强度
    public static final double DEFAULT_INTENSITY = 1.0;
    // 单项妆容，粉底 口红 腮红 眉毛 眼影 眼线 睫毛 高光 阴影 美瞳
    // 粉底
    public static final int FACE_MAKEUP_TYPE_FOUNDATION = 0;
    // 口红
    public static final int FACE_MAKEUP_TYPE_LIPSTICK = 1;
    // 腮红
    public static final int FACE_MAKEUP_TYPE_BLUSHER = 2;
    // 眉毛
    public static final int FACE_MAKEUP_TYPE_EYEBROW = 3;
    // 眼影
    public static final int FACE_MAKEUP_TYPE_EYE_SHADOW = 4;
    // 眼线
    public static final int FACE_MAKEUP_TYPE_EYE_LINER = 5;
    // 睫毛
    public static final int FACE_MAKEUP_TYPE_EYELASH = 6;
    // 高光
    public static final int FACE_MAKEUP_TYPE_HIGHLIGHT = 7;
    // 阴影
    public static final int FACE_MAKEUP_TYPE_SHADOW = 8;
    // 美瞳
    public static final int FACE_MAKEUP_TYPE_EYE_PUPIL = 9;

    private int nameId;
    private int type;
    private String intensityName;
    private String colorName;
    private List<double[]> colorList;
    private Drawable iconDrawable;
    private Map<String, Object> paramMap;

    public NewMakeupItem(int type, int nameId, String intensityName, String colorName, List<double[]> colorList,
                         Drawable iconDrawable, Map<String, Object> paramMap) {
        this.type = type;
        this.nameId = nameId;
        this.intensityName = intensityName;
        this.colorName = colorName;
        this.colorList = colorList;
        this.iconDrawable = iconDrawable;
        this.paramMap = paramMap;
    }

    public NewMakeupItem(int type, int nameId, String intensityName, Drawable iconDrawable, Map<String, Object> paramMap) {
        this.type = type;
        this.nameId = nameId;
        this.intensityName = intensityName;
        this.iconDrawable = iconDrawable;
        this.paramMap = paramMap;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public int getNameId() {
        return nameId;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIntensityName() {
        return intensityName;
    }

    public void setIntensityName(String intensityName) {
        this.intensityName = intensityName;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public List<double[]> getColorList() {
        return colorList;
    }

    public void setColorList(List<double[]> colorList) {
        this.colorList = colorList;
    }

    @Override
    public String toString() {
        return "NewMakeupItem{" +
                "type=" + type +
                ", nameId=" + nameId +
                ", intensityName='" + intensityName + '\'' +
                ", colorList=" + colorList +
                ", colorName='" + colorName + '\'' +
                ", iconDrawable=" + iconDrawable +
                ", paramMap=" + paramMap +
                '}';
    }
}
