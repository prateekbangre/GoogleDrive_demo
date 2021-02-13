package com.dreamappsstore.googledrive_demo;

import android.os.Parcel;
import android.os.Parcelable;

// GoogleDriveFileHolder class for storing the google drive file metadata
public class GoogleDriveFileHolder implements Parcelable {

    private String id;
    private String name;

    public GoogleDriveFileHolder(){}

    private GoogleDriveFileHolder(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<GoogleDriveFileHolder> CREATOR
            = new Parcelable.Creator<GoogleDriveFileHolder>() {
        public GoogleDriveFileHolder createFromParcel(Parcel in) {
            return new GoogleDriveFileHolder(in);
        }

        public GoogleDriveFileHolder[] newArray(int size) {
            return new GoogleDriveFileHolder[size];
        }
    };
}
