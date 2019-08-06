/*
 * Copyright (c) 2019 Marco Stornelli
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.balda.flipper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class MediaStoreCompat {

    private Context context;
    private String folder;
    private String subFolder;
    private MediaStoreCompatListener listener;

    public interface MediaStoreCompatListener {
        void onSave(OutputStream os, Uri result);
    }

    /**
     * Create a new publisher
     *
     * @param c  The content
     * @param f  One of Directory defined in Environment class
     * @param l  The operation listener
     */
    public MediaStoreCompat(@NonNull Context c, @NonNull String f, @NonNull MediaStoreCompatListener l) {
        context = c;
        folder = f;
        listener = l;
    }

    /**
     * Optional custom sub folder
     *
     * @param sub The sub folder name
     */
    public void setSubFolder(String sub) {
        subFolder = sub;
    }

    public void saveImage(@NonNull String name, @NonNull String mimeType) throws MediaStoreCompatException {
        saveMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, name, mimeType);
    }

    public void saveVideo(@NonNull String name, @NonNull String mimeType) throws MediaStoreCompatException {
        saveMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, name, mimeType);
    }

    public void saveAudio(@NonNull String name, @NonNull String mimeType) throws MediaStoreCompatException {
        saveMedia(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, name, mimeType);
    }

    private void saveMedia(Uri uri, String name, String mimeType) throws MediaStoreCompatException {
        ContentResolver resolver = context.getContentResolver();
        OutputStream os;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            String imagesDir = folder;
            if (!TextUtils.isEmpty(subFolder)) {
                imagesDir += File.separator + subFolder;
            }
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, imagesDir);
            contentValues.put(MediaStore.MediaColumns.OWNER_PACKAGE_NAME, context.getPackageName());
            Uri imageUri = resolver.insert(uri, contentValues);
            if (imageUri == null)
                throw new MediaStoreCompatException(new RemoteException());
            try {
                os = resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                throw new MediaStoreCompatException(e);
            }
            if (os == null)
                throw new MediaStoreCompatException(new FileNotFoundException());
            listener.onSave(os, imageUri);
        } else {
            File extDir = Environment.getExternalStoragePublicDirectory(folder);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment
                        .getExternalStorageState(extDir))) {
                    throw new MediaStoreCompatException("External storage not currently available");
                }
            } else {
                if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
                    throw new MediaStoreCompatException("External storage not currently available");
                }
            }

            String extPath = extDir.toString();
            if (!TextUtils.isEmpty(subFolder)) {
                extPath += File.separator + subFolder;
            }
            File path = new File(extPath);
            if (!path.exists()) {
                //noinspection ResultOfMethodCallIgnored
                path.mkdir();
            }
            File image = new File(path, name + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType));
            try {
                os = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                throw new MediaStoreCompatException(e);
            }
            listener.onSave(os, Uri.fromFile(image));
        }
    }
}
