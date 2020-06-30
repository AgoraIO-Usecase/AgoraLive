package com.faceunity.entity;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.faceunity.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 保存到数据库的表情动图的数据
 *
 * @author Richie on 2018.12.12
 */
public class LivePhoto implements Parcelable {

    private int id = -1;
    // 背景图的宽
    private int width;
    // 背景图的高
    private int height;
    /**
     * 五官贴图的点位，以视图左上角为原点
     */
    private double[] groupPoints;
    private double[] groupType;
    // 五官点位 JSONArray
    private String groupPointsStr = "";
    // 五官类型 JSONArray
    private String groupTypeStr = "";
    // 背景图
    private String templateImagePath = "";
    // 五官贴纸路径 JSONArray
    private String stickerImagePathStr = "";
    // 变换矩阵 3*3，JSONArray
    private String transformMatrixStr = "";
    // 调整过的点位，JSONArray
    private String adjustedPointsStr = "";

    public LivePhoto() {
    }

    public LivePhoto(int width, int height, double[] groupPoints, double[] groupType, String templateImagePath,
                     String[] stickerImagePaths, float[] transformMatrix, float[] adjustPoints) {
        this.width = width;
        this.height = height;
        if (templateImagePath == null) {
            templateImagePath = "";
        }
        this.templateImagePath = templateImagePath;
        setGroupType(groupType);
        setGroupPoints(groupPoints);
        setStickerImagePath(stickerImagePaths);
        setMatrixF(transformMatrix);
        setAdjustPointsF(adjustPoints);
    }

    public LivePhoto(int id, int width, int height, String groupPointsStr, String groupTypeStr,
                     String templateImagePath, String stickerImagePathStr, String transformMatrixStr,
                     String adjustedPointsStr) {
        this.id = id;
        this.width = width;
        this.height = height;
        if (groupPointsStr == null) {
            groupPointsStr = "";
        }
        this.groupPointsStr = groupPointsStr;
        if (groupTypeStr == null) {
            groupTypeStr = "";
        }
        this.groupTypeStr = groupTypeStr;
        if (templateImagePath == null) {
            templateImagePath = "";
        }
        this.templateImagePath = templateImagePath;
        if (stickerImagePathStr == null) {
            stickerImagePathStr = "";
        }
        this.stickerImagePathStr = stickerImagePathStr;
        if (transformMatrixStr == null) {
            transformMatrixStr = "";
        }
        this.transformMatrixStr = transformMatrixStr;
        if (adjustedPointsStr == null) {
            adjustedPointsStr = "";
        }
        this.adjustedPointsStr = adjustedPointsStr;
    }

    public static List<LivePhoto> getDefaultLivePhotos(Context context) {
        List<LivePhoto> livePhotos = new ArrayList<>(4);
        File livePhotoDir = FileUtils.getLivePhotoDir(context);
        File[] files = livePhotoDir.listFiles();
        if (files != null) {
            LivePhoto livePhoto;
            for (File file : files) {
                File[] mf = file.listFiles();
                if (mf != null) {
                    livePhoto = new LivePhoto();
                    for (File f : mf) {
                        String name = f.getName();
                        if (name.endsWith(".json")) {
                            try {
                                String s = FileUtils.readStringFromFile(f);
                                JSONObject jsonObject = new JSONObject(s);
                                int width = jsonObject.optInt("width");
                                int height = jsonObject.optInt("height");
                                JSONArray pointsArray = jsonObject.optJSONArray("group_points");
                                int len = pointsArray.length();
                                double[] groupPoints = new double[len];
                                for (int i = 0; i < len; i++) {
                                    groupPoints[i] = pointsArray.optDouble(i);
                                }
                                JSONArray typeArray = jsonObject.optJSONArray("group_type");
                                len = typeArray.length();
                                double[] groupType = new double[len];
                                for (int i = 0; i < len; i++) {
                                    groupType[i] = typeArray.optDouble(i);
                                }
                                livePhoto.setWidth(width);
                                livePhoto.setHeight(height);
                                livePhoto.setGroupPoints(groupPoints);
                                livePhoto.setGroupType(groupType);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (name.endsWith(".jpg") || name.endsWith(".png")) {
                            livePhoto.setTemplateImagePath(f.getAbsolutePath());
                        }
                    }
                    livePhotos.add(livePhoto);
                }
            }
        }
        return livePhotos;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double[] getGroupPoints() {
        if (groupPoints == null) {
            if (groupPointsStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(groupPointsStr);
                    int size = jsonArray.length();
                    groupPoints = new double[size];
                    for (int i = 0; i < size; i++) {
                        groupPoints[i] = jsonArray.optDouble(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return groupPoints;
    }

    public void setGroupPoints(double[] groupPoints) {
        if (groupPoints != null) {
            JSONArray jsonArray = new JSONArray();
            for (double groupPoint : groupPoints) {
                try {
                    jsonArray.put(groupPoint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.groupPointsStr = jsonArray.toString();
        }
        this.groupPoints = groupPoints;
    }

    public String[] getStickerImagePaths() {
        if (this.stickerImagePathStr != null) {
            try {
                JSONArray jsonArray = new JSONArray(this.stickerImagePathStr);
                int length = jsonArray.length();
                String[] images = new String[length];
                for (int i = 0; i < length; i++) {
                    images[i] = jsonArray.optString(i);
                }
                return images;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setStickerImagePath(String[] stickerImagePaths) {
        if (stickerImagePaths != null) {
            JSONArray jsonArray = new JSONArray();
            for (String path : stickerImagePaths) {
                jsonArray.put(path);
            }
            stickerImagePathStr = jsonArray.toString();
        }
    }

    public double[] getGroupType() {
        if (this.groupType == null) {
            if (this.groupTypeStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(this.groupTypeStr);
                    int size = jsonArray.length();
                    this.groupType = new double[size];
                    for (int i = 0; i < size; i++) {
                        this.groupType[i] = jsonArray.optDouble(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.groupType;
    }

    public void setGroupType(double[] groupType) {
        if (groupType != null) {
            JSONArray jsonArray = new JSONArray();
            for (double groupPoint : groupType) {
                try {
                    jsonArray.put(groupPoint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.groupTypeStr = jsonArray.toString();
        }
        this.groupType = groupType;
    }

    public String getTransformMatrixStr() {
        return this.transformMatrixStr;
    }

    public void setTransformMatrixStr(String transformMatrixStr) {
        this.transformMatrixStr = transformMatrixStr;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupPointsStr() {
        return this.groupPointsStr;
    }

    public void setGroupPointsStr(String groupPointsStr) {
        this.groupPointsStr = groupPointsStr;
    }

    public String getGroupTypeStr() {
        return groupTypeStr;
    }

    public void setGroupTypeStr(String groupTypeStr) {
        this.groupTypeStr = groupTypeStr;
    }

    public String getTemplateImagePath() {
        return this.templateImagePath;
    }

    public void setTemplateImagePath(String templateImagePath) {
        this.templateImagePath = templateImagePath;
    }

    public float[] getMatrixF() {
        if (this.transformMatrixStr != null) {
            try {
                JSONArray jsonArray = new JSONArray(this.transformMatrixStr);
                int length = jsonArray.length();
                float[] matrix = new float[length];
                for (int i = 0; i < length; i++) {
                    matrix[i] = (float) jsonArray.optDouble(i);
                }
                return matrix;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setMatrixF(float[] matrixF) {
        if (matrixF != null) {
            JSONArray jsonArray = new JSONArray();
            for (float mat : matrixF) {
                try {
                    jsonArray.put(mat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.transformMatrixStr = jsonArray.toString();
        }
    }

    public float[] getAdjustPointsF() {
        if (this.adjustedPointsStr != null) {
            try {
                JSONArray jsonArray = new JSONArray(this.adjustedPointsStr);
                int length = jsonArray.length();
                float[] adjustPoints = new float[length];
                for (int i = 0; i < length; i++) {
                    adjustPoints[i] = (float) jsonArray.optDouble(i);
                }
                return adjustPoints;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setAdjustPointsF(float[] adjustPointsF) {
        if (adjustPointsF != null) {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < adjustPointsF.length; i++) {
                try {
                    jsonArray.put(adjustPointsF[i]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.adjustedPointsStr = jsonArray.toString();
        }
    }

    public String getAdjustedPointsStr() {
        return adjustedPointsStr;
    }

    public void setAdjustedPointsStr(String adjustedPointsStr) {
        this.adjustedPointsStr = adjustedPointsStr;
    }


    public String getStickerImagePathStr() {
        return stickerImagePathStr;
    }

    public void setStickerImagePathStr(String stickerImagePathStr) {
        this.stickerImagePathStr = stickerImagePathStr;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(groupPoints);
        result = 31 * result + Arrays.hashCode(groupType);
        result = 31 * result + (templateImagePath != null ? templateImagePath.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LivePhoto that = (LivePhoto) o;

        if (id != that.id) {
            return false;
        }
        if (width != that.width) {
            return false;
        }
        if (height != that.height) {
            return false;
        }
        if (!Arrays.equals(groupPoints, that.groupPoints)) {
            return false;
        }
        if (!Arrays.equals(groupType, that.groupType)) {
            return false;
        }
        return templateImagePath != null ? templateImagePath.equals(that.templateImagePath) : that.templateImagePath == null;
    }

    @Override
    public String toString() {
        return "LivePhoto{" +
                "id=" + id +
                ", width=" + width +
                ", height=" + height +
                ", groupPointsStr='" + groupPointsStr + '\'' +
                ", groupTypeStr='" + groupTypeStr + '\'' +
                ", templateImagePath='" + templateImagePath + '\'' +
                ", stickerImagePathStr='" + stickerImagePathStr + '\'' +
                ", transformMatrixStr='" + transformMatrixStr + '\'' +
                ", adjustedPointsStr='" + adjustedPointsStr + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeDoubleArray(this.groupPoints);
        dest.writeDoubleArray(this.groupType);
        dest.writeString(this.groupPointsStr);
        dest.writeString(this.groupTypeStr);
        dest.writeString(this.templateImagePath);
        dest.writeString(this.stickerImagePathStr);
        dest.writeString(this.transformMatrixStr);
        dest.writeString(this.adjustedPointsStr);
    }

    protected LivePhoto(Parcel in) {
        this.id = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.groupPoints = in.createDoubleArray();
        this.groupType = in.createDoubleArray();
        this.groupPointsStr = in.readString();
        this.groupTypeStr = in.readString();
        this.templateImagePath = in.readString();
        this.stickerImagePathStr = in.readString();
        this.transformMatrixStr = in.readString();
        this.adjustedPointsStr = in.readString();
    }

    public static final Creator<LivePhoto> CREATOR = new Creator<LivePhoto>() {
        @Override
        public LivePhoto createFromParcel(Parcel source) {
            return new LivePhoto(source);
        }

        @Override
        public LivePhoto[] newArray(int size) {
            return new LivePhoto[size];
        }
    };
}