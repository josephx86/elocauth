package com.pixispace.elocauth.data.firebase;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pixispace.elocauth.callbacks.StringCallback;
import com.pixispace.elocauth.data.DataHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageHelper {
    private static final String REF_PROFILE_PICTURES = "profile_pictures";
    private static StorageHelper instance;

    public static StorageHelper getInstance() {
        if (instance == null) {
            instance = new StorageHelper();
        }
        return instance;
    }

    private StorageReference profilePicturesFolder;

    private StorageHelper() {
        profilePicturesFolder = FirebaseStorage.getInstance().getReference(REF_PROFILE_PICTURES);
    }

    public void uploadProfilePicture(String id, Bitmap bitmap, StringCallback callback) {
        File temp = DataHelper.getTempFile();

        boolean saved = false;
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException ignore) {

        }

        if (saved) {
            Uri tempUri = DataHelper.getUriForFile(temp);
            final StorageReference remoteProfilePicture = profilePicturesFolder.child(id);
            remoteProfilePicture.putFile(tempUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    remoteProfilePicture.getDownloadUrl().addOnCompleteListener(urlTask -> {
                        String downloadUrl = "";
                        if (urlTask.isSuccessful()) {
                            Uri uri = urlTask.getResult();
                            if (uri != null) {
                                downloadUrl = uri.toString();
                            }
                        }
                        if (callback != null) {
                            callback.handler(downloadUrl);
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.handler("");
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.handler("");
            }
        }
    }
}
