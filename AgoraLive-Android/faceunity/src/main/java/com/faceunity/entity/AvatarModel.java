package com.faceunity.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Richie on 2019.03.20
 */
public class AvatarModel implements Parcelable {
    private int id = -1;
    private int iconId;
    private boolean isDefault;
    private String iconPath = "";
    // 参数配置
    private String paramJson = "";
    // 界面配置
    private String uiJson = "";

    public AvatarModel(int iconId, boolean isDefault) {
        this.iconId = iconId;
        this.isDefault = isDefault;
    }

    public AvatarModel() {
    }

    public AvatarModel(int id, String iconPath, String paramJson, String uiJson) {
        this.id = id;
        this.iconPath = iconPath;
        this.paramJson = paramJson;
        this.uiJson = uiJson;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getParamJson() {
        return this.paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }

    public String getUiJson() {
        return uiJson;
    }

    public void setUiJson(String uiJson) {
        this.uiJson = uiJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AvatarModel that = (AvatarModel) o;

        if (iconId != that.iconId) {
            return false;
        }
        if (iconPath != null ? !iconPath.equals(that.iconPath) : that.iconPath != null) {
            return false;
        }
        if (paramJson != null ? !paramJson.equals(that.paramJson) : that.paramJson != null) {
            return false;
        }
        return uiJson != null ? uiJson.equals(that.uiJson) : that.uiJson == null;
    }

    @Override
    public int hashCode() {
        int result = iconId;
        result = 31 * result + (iconPath != null ? iconPath.hashCode() : 0);
        result = 31 * result + (paramJson != null ? paramJson.hashCode() : 0);
        result = 31 * result + (uiJson != null ? uiJson.hashCode() : 0);
        return result;
    }

    public AvatarModel cloneIt() {
        AvatarModel avatarModel = new AvatarModel();
        if (this.id > 0) {
            avatarModel.id = this.id;
        }
        avatarModel.iconId = this.iconId;
        // deep copy string
        avatarModel.paramJson = this.paramJson + "";
        avatarModel.uiJson = this.uiJson + "";
        avatarModel.iconPath = this.iconPath + "";
        return avatarModel;
    }

    @Override
    public String toString() {
        return "AvatarModel{" +
                "id=" + id +
                ", iconId=" + iconId +
                ", isDefault=" + isDefault +
                ", iconPath='" + iconPath + '\'' +
                ", paramJson='" + paramJson + '\'' +
                ", uiJson='" + uiJson + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.iconPath);
        dest.writeString(this.paramJson);
        dest.writeString(this.uiJson);
    }

    protected AvatarModel(Parcel in) {
        this.id = in.readInt();
        this.iconPath = in.readString();
        this.paramJson = in.readString();
        this.uiJson = in.readString();
    }

    public static final Creator<AvatarModel> CREATOR = new Creator<AvatarModel>() {
        @Override
        public AvatarModel createFromParcel(Parcel source) {
            return new AvatarModel(source);
        }

        @Override
        public AvatarModel[] newArray(int size) {
            return new AvatarModel[size];
        }
    };
}
