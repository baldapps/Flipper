package com.balda.flipper;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

@SuppressWarnings("unused")
public class FileDescription implements Parcelable {

    private String name;
    private String mime;
    @Nullable
    private Uri uri;

    public FileDescription(@NonNull String name, @NonNull String mime) {
        this.name = name;
        this.mime = mime;
    }

    public FileDescription(@NonNull Context context, @NonNull Uri file) throws IllegalArgumentException {
        //noinspection ConstantConditions
        this(DocumentFile.fromSingleUri(context, file));
    }

    public FileDescription(@NonNull DocumentFile file) throws IllegalArgumentException {
        if (!file.isFile())
            throw new IllegalArgumentException();
        this.name = file.getName();
        this.mime = file.getType();
        this.uri = file.getUri();
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
        if (in.readInt() == 1) {
            uri = in.readParcelable(Uri.class.getClassLoader());
        }
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

    @Nullable
    public Uri getUri() {
        return uri;
    }

    public void setUri(@Nullable Uri uri) {
        this.uri = uri;
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
        if (uri != null) {
            parcel.writeInt(1);
            parcel.writeParcelable(uri, i);
        } else
            parcel.writeInt(0);
    }
}
