package com.balda.flipper;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;

import java.io.File;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class FileDescription implements Parcelable {

    private String name;
    private String mime;

    public FileDescription(@NonNull String name, @NonNull String mime) {
        this.name = name;
        this.mime = mime;
    }

    public FileDescription(@NonNull File file) {
        String fileName = file.getName();
        int len = fileName.indexOf(".");
        if (len == -1) {
            this.name = fileName;
            this.mime = "*/*";
        } else {
            this.name = fileName.substring(0, len);
            this.mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(len));
        }
    }

    protected FileDescription(Parcel in) {
        name = in.readString();
        mime = in.readString();
    }

    public static final Creator<FileDescription> CREATOR = new Creator<FileDescription>() {
        @Override
        public FileDescription createFromParcel(Parcel in) {
            return new FileDescription(in);
        }

        @Override
        public FileDescription[] newArray(int size) {
            return new FileDescription[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null)
            this.name = name;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        if (mime != null)
            this.mime = mime;
    }

    /**
     * It returns the name with extension according to mime type set
     *
     * @return The full name example myimage.png
     */
    public String getFullName() {
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        if (ext != null)
            return name + "." + ext;
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(mime);
    }
}
