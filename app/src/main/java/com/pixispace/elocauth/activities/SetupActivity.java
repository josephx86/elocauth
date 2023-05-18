package com.pixispace.elocauth.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.data.DataHelper;
import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.data.firebase.FirestoreHelper;
import com.pixispace.elocauth.databinding.ActivitySetupBinding;

import java.io.IOException;
import java.util.HashMap;

public class SetupActivity extends AppCompatActivity {
    private ActivitySetupBinding binding;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePicker;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Bitmap avatar = null;
    private Uri photoUri = null;
    private UserAccountViewModel viewModel;
    HashMap<String, Object> profile = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);
        setLaunchers();
        setListeners();
        setTextWatchers();
        updateUI(false);
    }

    private void setTextWatchers() {
        binding.displayNameTextInput.addTextChangedListener(new TextInputWatcher(binding.displayNameLayout));
        binding.userIdTextInput.addTextChangedListener(new TextInputWatcher(binding.userIdLayout));
    }

    private void setListeners() {
        binding.galleryButton.setOnClickListener(v -> pickImage());
        binding.cameraButton.setOnClickListener(v -> takePhoto());
        binding.root.setOnClickListener(v -> ActivityHelper.hideKeyboard(this));
        binding.doneButton.setOnClickListener(v -> saveProfile());
    }

    private void takePhoto() {
        photoUri = DataHelper.getTempFileUri();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(intent);
    }

    private void pickImage() {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build();
        imagePicker.launch(request);
    }

    private void setLaunchers() {

        imagePicker = registerForActivityResult(new PickVisualMedia(), result -> {
            if (result != null) {
                setImage(result);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if ((result != null) && (result.getResultCode() == RESULT_OK)) {
                if (photoUri != null) {
                    setImage(photoUri);
                    photoUri = null;
                }
            }
        });
    }

    private void setImage(Uri src) {
        avatar = null;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), src);
            bitmap = downScaleAvatar(bitmap);
            if (bitmap != null) {
                avatar = bitmap;
            }
        } catch (IOException ignore) {
        }
        binding.avatarImageView.setImageBitmap(avatar);
    }

    private Bitmap downScaleAvatar(Bitmap bitmap) {
        final int maxDimension = 800;
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        int bigDimension = Math.max(bitmapWidth, bitmapHeight);
        if (bigDimension > maxDimension) {
            float scaleFactor = (float) maxDimension / bigDimension;
            Matrix matrix = new Matrix();
            matrix.setScale(scaleFactor, scaleFactor);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
        }
        return bitmap;
    }

    private void updateUI(boolean lock) {
        if (lock) {
            binding.cameraButton.setEnabled(false);
            binding.galleryButton.setEnabled(false);
            binding.displayNameLayout.setEnabled(false);
            binding.userIdLayout.setEnabled(false);
            binding.progressHorizontal.setVisibility(View.VISIBLE);
            binding.doneButton.setVisibility(View.INVISIBLE);
            binding.galleryButton.setAlpha(0.25f);
            binding.cameraButton.setAlpha(0.25f);
        } else {
            binding.cameraButton.setEnabled(true);
            binding.galleryButton.setEnabled(true);
            binding.displayNameLayout.setEnabled(true);
            binding.userIdLayout.setEnabled(true);
            binding.progressHorizontal.setVisibility(View.GONE);
            binding.doneButton.setVisibility(View.VISIBLE);
            binding.galleryButton.setAlpha(1f);
            binding.cameraButton.setAlpha(1f);
        }
    }

    private void saveProfile() {
        binding.displayNameLayout.setError(null);
        binding.userIdLayout.setError(null);

        Editable editable = binding.displayNameTextInput.getEditableText();
        String displayName = "";
        if (editable != null) {
            displayName = editable.toString().trim();
        }
        if (displayName.isEmpty()) {
            binding.displayNameLayout.setError(getString(R.string.set_your_name));
            return;
        }

        editable = binding.userIdTextInput.getEditableText();
        String userId = "";
        if (editable != null) {
            userId = editable.toString().trim();
        }
        if (userId.isEmpty()) {
            binding.userIdLayout.setError(getString(R.string.set_your_desired_user_id));
            return;
        } else if (userId.length() < 6) {
            binding.userIdLayout.setError(getString(R.string.user_id_is_too_short));
            return;
        } else if (!userIdHasValidChars(userId)) {
            binding.userIdLayout.setError(getString(R.string.user_id_has_invalid_characters));
            return;
        }

        updateUI(true);
        profile.put(FirestoreHelper.FIELD_USER_ID, userId);
        profile.put(FirestoreHelper.FIELD_DISPLAY_NAME, displayName);
        profile.put(FirestoreHelper.FIELD_PROFILE_PICTURE, "");
        viewModel.userIdExists(userId, this::onUserIdChecked);
    }

    private boolean userIdHasValidChars(String userId) {
        if (userId != null) {
            final int len = userId.length();
            for (int i = 0; i < len; i++) {
                char c = userId.charAt(i);
                if ((!Character.isLetterOrDigit(c)) && (c != '_')) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void onUserIdChecked(boolean exists) {
        if (exists) {
            binding.userIdLayout.setError(getString(R.string.user_id_is_already_taken));
            updateUI(false);
        } else {
            if (avatar != null) {
                viewModel.uploadProfilePicture(avatar, this::onProfilePictureUploaded);
            } else {
                uploadProfile(profile);
            }
        }
    }

    private void onProfilePictureUploaded(String url) {
        if (url == null) {
            url = "";
        }
        url = url.trim();
        if (url.isEmpty()) {
            updateUI(false);
            ActivityHelper.showModalAlert(this, getString(R.string.oops), getString(R.string.profile_picture_upload_error));
            return;
        }
        profile.put(FirestoreHelper.FIELD_PROFILE_PICTURE, url);
        uploadProfile(profile);
    }

    private void uploadProfile(HashMap<String, Object> data) {
        viewModel.updateProfile(data, this::onProfileUploadCompleted);
    }

    private void onProfileUploadCompleted(boolean success) {
        updateUI(false);
        if (success) {
            ActivityHelper.open(this, LandingActivity.class, true);
        } else {
            ActivityHelper.showModalAlert(this, getString(R.string.oops), getString(R.string.something_went_wrong));
        }
    }
}