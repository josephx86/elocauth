package com.pixispace.elocauth.data.firebase;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.pixispace.elocauth.ElocAuthApp;
import com.pixispace.elocauth.R;
import com.pixispace.elocauth.callbacks.BooleanCallback;
import com.pixispace.elocauth.callbacks.StringCallback;

public class AuthHelper {
    private GoogleSignInClient googleSignInClient;

    private static AuthHelper instance;

    public static AuthHelper getInstance() {
        if (instance == null) {
            instance = new AuthHelper();
        }
        return instance;
    }

    private AuthHelper() {
        initGoogleSignIn();
    }

    private void initGoogleSignIn() {
        Context context = ElocAuthApp.getInstance();
        String client_id = context.getString(R.string.default_web_client_id);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(client_id)
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
    }

    public void register(String email, String password, StringCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (callback != null) {
                            callback.handler("");
                        }
                    });
                }
            } else {
                Exception exception = task.getException();
                String message = ElocAuthApp.getInstance().getString(R.string.something_went_wrong);
                if (exception != null) {
                    message = exception.getMessage();
                }
                if (callback != null) {
                    callback.handler(message);
                }
            }
        });
    }

    public void signIn(String email, String password, StringCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            String error = "";
            if (!task.isSuccessful()) {
                Context context = ElocAuthApp.getInstance();
                Exception exception = task.getException();
                error = context.getString(R.string.something_went_wrong);
                if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    error = context.getString(R.string.invalid_email_address_or_password);
                } else if (exception != null) {
                    error = exception.getMessage();
                }
            }
            if (callback != null) {
                callback.handler(error);
            }
        });
    }

    public void signInWithGoogle(Intent data, BooleanCallback callback) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    String idToken = account.getIdToken();
                    if (idToken != null) {
                        GoogleAuthCredential credential = (GoogleAuthCredential) GoogleAuthProvider.getCredential(idToken, null);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(t -> {
                            googleSignInClient.signOut();
                            if (callback != null) {
                                callback.handler(true);
                            }
                        });
                    }
                }
            }
        });
    }

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void signOut() {
        googleSignInClient.signOut();
        FirebaseAuth.getInstance().signOut();
    }

    public boolean isSignedIn() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public String getEmailAddress() {
        String emailAddress = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailAddress = user.getEmail();
        }
        return emailAddress;
    }

    public String getUserId() {
        String id = "no_id";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            id = user.getUid();
        }
        return id;
    }

    public void sendResetLink(String emailAddress, BooleanCallback callback) {
        if (emailAddress == null) {
            emailAddress = "";
        }
        emailAddress = emailAddress.trim();
        if (emailAddress.isEmpty() && (callback != null)) {
            callback.handler(false);
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress).addOnCompleteListener(task -> {
                if (callback != null) {
                    callback.handler(task.isSuccessful());
                }
            });
        }
    }

    public void sendVerificationLink(BooleanCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (callback != null) {
                    callback.handler(task.isSuccessful());
                }
            });
        } else {
            if (callback != null) {
                callback.handler(false);
            }
        }
    }
}
