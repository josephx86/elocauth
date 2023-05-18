package com.pixispace.elocauth.data;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixispace.elocauth.callbacks.BooleanCallback;
import com.pixispace.elocauth.callbacks.StringCallback;
import com.pixispace.elocauth.data.firebase.AuthHelper;
import com.pixispace.elocauth.data.firebase.FirestoreHelper;
import com.pixispace.elocauth.data.firebase.StorageHelper;

import java.util.HashMap;

public class UserAccountRepository {

    private final FirestoreHelper firestoreHelper;
    private final AuthHelper authHelper;
    private final StorageHelper storageHelper;
    private static UserAccountRepository instance;
    private final MutableLiveData<String> displayName = new MutableLiveData<>();

    private long confirmationEmailTimestamp = 0;

    static UserAccountRepository getInstance() {
        if (instance == null) {
            instance = new UserAccountRepository();
        }
        return instance;
    }

    private UserAccountRepository() {
        firestoreHelper = FirestoreHelper.getInstance();
        authHelper = AuthHelper.getInstance();
        storageHelper = StorageHelper.getInstance();
    }

    LiveData<String> getDisplayName() {
        return displayName;
    }

    public void setDisplayNameCompleted(String name) {
        displayName.setValue(name);
    }

    public String getEmailAddress() {
        return authHelper.getEmailAddress();
    }

    Intent getGoogleSignInIntent() {
        return authHelper.getGoogleSignInIntent();
    }

    void signOut() {
        authHelper.signOut();
    }

    boolean isSignedIn() {
        return authHelper.isSignedIn();
    }

    boolean isUserEmailVerified() {
        boolean verified = false;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            verified = user.isEmailVerified();
        }
        return verified;
    }

    void signInWithGoogle(Intent data, BooleanCallback callback) {
        authHelper.signInWithGoogle(data, callback);
    }

    void register(String email, String password, StringCallback callback) {
        authHelper.register(email, password, callback);
    }

    void uploadProfilePicture(Bitmap bitmap, StringCallback callback) {
        String id = authHelper.getUserId();
        storageHelper.uploadProfilePicture(id, bitmap, callback);
    }

    void updateProfile(HashMap<String, Object> data, BooleanCallback callback) {
        String id = authHelper.getUserId();
        firestoreHelper.updateProfile(id, data, callback);
    }

    void hasProfile(BooleanCallback callback) {
        String id = authHelper.getUserId();
        firestoreHelper.hasProfile(id, callback);
    }

    void userIdExists(String userId, BooleanCallback callback) {
        firestoreHelper.userIdExists(userId, callback);
    }

    void signIn(String emailAddress, String password, StringCallback callback) {
        authHelper.signIn(emailAddress, password, callback);
    }

    void sendPasswordResetLink(String emailAddress, BooleanCallback callback) {
        authHelper.sendResetLink(emailAddress, callback);
    }

    void sendEmailVerificationLink(BooleanCallback callback) {
        authHelper.sendVerificationLink(callback);
    }
}
