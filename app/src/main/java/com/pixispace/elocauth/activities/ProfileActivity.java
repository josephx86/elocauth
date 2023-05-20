package com.pixispace.elocauth.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.data.UserProfile;
import com.pixispace.elocauth.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private UserAccountViewModel viewModel;
    private ActivityResultLauncher<Intent> textFieldEditorLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setViewModel();
        setToolbar();
        setItemTitles();
        setLaunchers();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        binding.displayNameItem.titleTextView.setText(R.string.your_name);
    }

    private void setItemValues(UserProfile profile) {
        binding.displayNameItem.valueTextView.setText(profile.getDisplayName());
    }

    private void setLaunchers() {
        textFieldEditorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                int resultCode = result.getResultCode();
                Intent data = result.getData();
                if ((resultCode == RESULT_CANCELED) && (data != null)) {
                    String message = data.getStringExtra(TextEditorActivity.RESULT_MESSAGE);
                    if ((message != null) && (!message.isEmpty())) {
                        ActivityHelper.showModalAlert(ProfileActivity.this, getString(R.string.oops), message);
                    }
                }
            }
        });
    }

    private void setListeners() {
        binding.displayNameItem.button.setOnClickListener(v -> openDisplayNameEditor());
    }

    private void openDisplayNameEditor() {
        Intent intent = new Intent(this, TextEditorActivity.class);
        String fieldName = getString(R.string.your_name);
        String currentValue = "";
        UserProfile profile = viewModel.watchProfile().getValue();
        if (profile != null) {
            currentValue = profile.getDisplayName();
        }
        int maxLength = getResources().getInteger(R.integer.display_name_max_length);
        FieldEditorArgs data = new FieldEditorArgs(fieldName, currentValue, maxLength);
        intent.putExtra(TextEditorActivity.EXTRA_DATA, data);
        textFieldEditorLauncher.launch(intent);
    }
}