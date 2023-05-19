package com.pixispace.elocauth.data.firebase;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;
import com.pixispace.elocauth.callbacks.BooleanCallback;
import com.pixispace.elocauth.callbacks.ProfileCallback;
import com.pixispace.elocauth.data.UserAccountRepository;
import com.pixispace.elocauth.data.UserProfile;

import java.util.HashMap;

public class FirestoreHelper {

    private final CollectionReference accountsNode;

    private static FirestoreHelper instance;
    public static String FIELD_PROFILE_PICTURE = "profile_picture";
    public static String FIELD_DISPLAY_NAME = "display_name";
    public static String FIELD_USER_ID = "user_id";

    public static FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    private FirestoreHelper() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        accountsNode = firestore.collection("accounts");
    }

    public void setDisplayName(String val, UserAccountRepository repository) {
        // todo
        if (repository == null) {
            return;
        }
        if (val == null) {
            val = "";
        }
        final String displayName = val.trim();
        HashMap<String, Object> data = new HashMap<>();
        data.put(FIELD_DISPLAY_NAME, val);
        accountsNode.document(repository.getEmailAddress()).update(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                repository.setDisplayNameCompleted(displayName);
            }
        });
    }

    public void updateProfile(String id, HashMap<String, Object> data, BooleanCallback callback) {
        if (id == null) {
            id = "";
        }
        id = id.trim();
        if (id.isEmpty() || (data == null) || data.isEmpty()) {
            if (callback != null) {
                callback.handler(false);
            }
        } else {
            SetOptions options = SetOptions.merge();
            accountsNode
                    .document(id)
                    .set(data, options)
                    .addOnCompleteListener(task -> {
                        if (callback != null) {
                            callback.handler(task.isSuccessful());
                        }
                    });
        }
    }

    public void hasProfile(String id, BooleanCallback callback) {
        if (id == null) {
            id = "";
        }
        id = id.trim();
        if (id.isEmpty()) {
            if (callback != null) {
                callback.handler(false);
            }
        } else {
            accountsNode.document(id).get(Source.SERVER).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean hasUserId = false;
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        Object o = snapshot.get(FIELD_USER_ID);
                        if (o != null) {
                            String userId = o.toString();
                            hasUserId = (!userId.isEmpty());
                        }
                    }
                    if (callback != null) {
                        callback.handler(hasUserId);
                    }
                } else {
                    if (callback != null) {
                        callback.handler(false);
                    }
                }
            });
        }
    }

    public void userIdExists(String userId, BooleanCallback callback) {
        if (userId == null) {
            userId = "";
        }
        userId = userId.trim();
        if (userId.isEmpty()) {
            if (callback != null) {
                callback.handler(false);
            }
        } else {
            accountsNode
                    .whereEqualTo(FIELD_USER_ID, userId)
                    .get(Source.SERVER)
                    .addOnCompleteListener(task -> {
                        boolean exists = false;
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            int count = snapshot.size();
                            exists = (count > 0);
                        }
                        if (callback != null) {
                            callback.handler(exists);
                        }
                    });
        }
    }

    public void getProfile(String documentId, final String emailAddress, ProfileCallback callback) {
        if (documentId == null) {
            documentId = "";
        }
        documentId = documentId.trim();
        if (documentId.isEmpty()) {
            if (callback != null) {
                callback.handler(null);
            }
        } else {
            accountsNode
                    .document(documentId)
                    .get(Source.SERVER)
                    .addOnCompleteListener(task -> {
                        UserProfile profile = null;
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            String displayName = snapshot.get(FIELD_DISPLAY_NAME, String.class);
                            String profilePictureUrl = snapshot.get(FIELD_PROFILE_PICTURE, String.class);
                            String userId = snapshot.get(FIELD_USER_ID, String.class);
                            profile = new UserProfile(userId);
                            profile.setDisplayName(displayName);
                            profile.setProfilePictureUrl(profilePictureUrl);
                            profile.setEmailAddress(emailAddress);
                        }
                        if (callback != null) {
                            callback.handler(profile);
                        }
                    });
        }
    }
}
