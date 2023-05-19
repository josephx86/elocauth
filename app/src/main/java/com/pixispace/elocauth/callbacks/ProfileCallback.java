package com.pixispace.elocauth.callbacks;

import com.pixispace.elocauth.data.UserProfile;

public interface ProfileCallback {
    void handler(UserProfile userProfile);
}
