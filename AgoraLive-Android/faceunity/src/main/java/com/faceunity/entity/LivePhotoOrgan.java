package com.faceunity.entity;

import java.util.List;

/**
 * @author Richie on 2019.04.11
 * 表情动图五官点位
 */
public class LivePhotoOrgan {

    /**
     * leye : [41,247,116,179,242,131,357,163,466,249,386,323,245,367,117,321]
     * reye : [457,250,384,180,250,130,127,167,36,249,118,329,245,368,385,321]
     * nose : [317,80,302,209,385,387,350,439,252,460,142,442,118,385,201,218,179,76,309,438,193,441,250,357]
     * mouth : [462,240,394,197,310,162,253,185,197,161,121,187,39,236,75,273,142,315,247,339,350,318,411,284,345,250,249,275,150,251,154,236,249,249,336,231]
     * lbrow : [40,250,238,188,452,241,457,319,365,284,177,249]
     * rbrow : [465,260,258,184,48,242,43,313,165,274,310,249]
     * width : 500
     * height : 500
     * name : client_points
     */

    private int width;
    private int height;
    private String name;
    private List<Float> leye;
    private List<Float> reye;
    private List<Float> nose;
    private List<Float> mouth;
    private List<Float> lbrow;
    private List<Float> rbrow;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Float> getLeye() {
        return leye;
    }

    public void setLeye(List<Float> leye) {
        this.leye = leye;
    }

    public List<Float> getReye() {
        return reye;
    }

    public void setReye(List<Float> reye) {
        this.reye = reye;
    }

    public List<Float> getNose() {
        return nose;
    }

    public void setNose(List<Float> nose) {
        this.nose = nose;
    }

    public List<Float> getMouth() {
        return mouth;
    }

    public void setMouth(List<Float> mouth) {
        this.mouth = mouth;
    }

    public List<Float> getLbrow() {
        return lbrow;
    }

    public void setLbrow(List<Float> lbrow) {
        this.lbrow = lbrow;
    }

    public List<Float> getRbrow() {
        return rbrow;
    }

    public void setRbrow(List<Float> rbrow) {
        this.rbrow = rbrow;
    }

    @Override
    public String toString() {
        return "LivePhotoOrgan{" +
                "width=" + width +
                ", height=" + height +
                ", name='" + name + '\'' +
                ", leye=" + leye +
                ", reye=" + reye +
                ", nose=" + nose +
                ", mouth=" + mouth +
                ", lbrow=" + lbrow +
                ", rbrow=" + rbrow +
                '}';
    }
}
