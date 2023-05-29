package com.pixispace.elocauth.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.callbacks.VoidCallback;
import com.pixispace.elocauth.data.DataHelper;
import com.pixispace.elocauth.data.HttpHelper;
import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.data.UserProfile;
import com.pixispace.elocauth.databinding.ActivityProfileBinding;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity implements MediaActivity {
    private ActivityProfileBinding binding;
    private UserAccountViewModel viewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePicker;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri photoUri = null;
    private String originalProfilePicture;

    private enum ProfileField {
        profilePicture,
    }

    private ProfileField currentField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        binding.avatarImageView.setDefaultImageResId(R.drawable.person);
        binding.avatarImageView.setErrorImageResId(R.drawable.person);
        setContentView(binding.getRoot());
        setViewModel();
        setToolbar();
        setItemTitles();
        setLaunchers();
        setListeners();
        updateUI(false);
        viewModel.getProfile();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void takePhoto() {
        photoUri = DataHelper.getTempFileUri();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(intent);
    }

    @Override
    public void pickImage() {
        imagePicker.launch(ActivityHelper.getPickImageRequest());
    }

    @Override
    public void setImage(Uri src) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), src);
            bitmap = downScaleAvatar(bitmap);
            if (bitmap != null) {
                updateUI(true);
                binding.avatarImageView.setImageBitmap(bitmap);
                viewModel.uploadProfilePicture(bitmap, this::profilePictureUploadCompleted);
            }
        } catch (IOException ignore) {
        }
    }

    private void profilePictureUploadCompleted(String url) {
        if ((url == null) || url.isEmpty()) {
            ActivityHelper.showModalAlert(
                    this,
                    getString(R.string.oops),
                    getString(R.string.upload_profile_picture_error),
                    () -> binding.avatarImageView.setImageUrl(
                            originalProfilePicture,
                            HttpHelper.getInstance().getImageLoader()
                    )
            );
        } else {
            currentField = ProfileField.profilePicture;
            updateField(url);
        }
    }

    private void setToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);
        viewModel.watchProfile().observe(this, this::setItemValues);
    }

    private void setItemTitles() {
        binding.userIdItem.titleTextView.setText(R.string.user_id);
        binding.userIdItem.chevron.setVisibility(View.GONE);
        binding.userIdItem.button.setBackground(null);
    }

    private void setItemValues(UserProfile profile) {
        updateUI(false);
        originalProfilePicture = profile.getProfilePictureUrl();
        binding.avatarImageView.setImageUrl(
                profile.getProfilePictureUrl(),
                HttpHelper.getInstance().getImageLoader()
        );
        binding.userIdItem.valueTextView.setText(profile.getUserId());
    }

    private void updateField(String value) {
        VoidCallback completedCallback = () -> {
            updateUI(false);
            viewModel.getProfile();
        };
        updateUI(true);
        if (currentField == ProfileField.profilePicture) {
            viewModel.updateProfilePicture(value, completedCallback);
        }
        currentField = null;
    }

    private void updateUI(boolean showProgress) {
        if (showProgress) {
            binding.fieldsLayout.setVisibility(View.GONE);
            binding.progressLayout.setVisibility(View.VISIBLE);
        } else {
            binding.fieldsLayout.setVisibility(View.VISIBLE);
            binding.progressLayout.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        binding.cameraButton.setOnClickListener(v -> takePhoto());
        binding.galleryButton.setOnClickListener(v -> pickImage());
    }

    private void setLaunchers() {

        imagePicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), result -> {
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
}