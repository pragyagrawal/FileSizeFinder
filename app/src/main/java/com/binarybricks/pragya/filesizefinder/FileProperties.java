package com.binarybricks.pragya.filesizefinder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by PRAGYA on 9/12/2016.
 */
public class FileProperties implements Parcelable {

    private String fileName;
    private Long fileSize;
    private String fileExtention;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtention() {
        return fileExtention;
    }

    public void setFileExtention(String fileExtention) {
        this.fileExtention = fileExtention;
    }

    protected FileProperties(Parcel in) {
        fileName = in.readString();
        fileSize = in.readByte() == 0x00 ? null : in.readLong();
        fileExtention = in.readString();
    }

    public FileProperties(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        if (fileSize == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(fileSize);
        }
        dest.writeString(fileExtention);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FileProperties> CREATOR = new Parcelable.Creator<FileProperties>() {
        @Override
        public FileProperties createFromParcel(Parcel in) {
            return new FileProperties(in);
        }

        @Override
        public FileProperties[] newArray(int size) {
            return new FileProperties[size];
        }
    };
}
