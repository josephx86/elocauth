package com.pixispace.elocauth.data;

import android.content.Intent;
import android.graphics.Bitmap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixispace.elocauth.callbacks.BooleanCallback;
import com.pixispace.elocauth.callbacks.ProfileCallback;
import com.pixispace.elocauth.callbacks.StringCallback;
import com.pixispace.elocauth.callbacks.VoidCallback;
import com.pixispace.elocauth.data.firebase.AuthHelper;
import com.pixispace.elocauth.data.firebase.FirestoreHelper;
import com.pixispace.elocauth.data.firebase.StorageHelper;

import java.util.HashMap;

public class UserAccountRepository {

    private final FirestoreHelper firestoreHelper;
    private final AuthHelper authHelper;
    private final StorageHelper storageHelper;
    private static UserAccountRepository instance;

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

    void getProfile(ProfileCallback callback) {
        String id = authHelper.getUserId();
        String emailAddress = getEmailAddress();
        firestoreHelper.getProfile(id, emailAddress, callback);
    }

    void updateDisplayName(String name, VoidCallback callback) {
        String id = authHelper.getUserId();
        firestoreHelper.updateDisplayName(name, id, callback);
    }

    void uploadProfilePicture(Bitmap bitmap, StringCallback callback) {
        String id = authHelper.getUserId();
        storageHelper.uploadProfilePicture(id, bitmap, callback);
    }

    void updateProfilePicture(String url, final VoidCallback callback) {
        String id = authHelper.getUserId();
        firestoreHelper.updateProfilePicture(url, id, callback);
    }
}
