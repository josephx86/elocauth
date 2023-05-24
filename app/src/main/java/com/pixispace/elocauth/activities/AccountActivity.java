package com.pixispace.elocauth.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.data.UserProfile;
import com.pixispace.elocauth.databinding.ActivityAccountBinding;

public class AccountActivity extends AppCompatActivity {
    private ActivityAccountBinding binding;
    private UserAccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setViewModel();
        setToolbar();
        setItemTitles();
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
            actionBar.setTitle(R.string.manage_account);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);
        viewModel.watchProfile().observe(this, this::setItemValues);
    }

    private void setItemTitles() {
        binding.changeEmailAddressItem.titleTextView.setText(R.string.email_address);

        binding.changePasswordItem.titleTextView.setText(R.string.password);
        binding.changePasswordItem.valueTextView.setText(R.string.change);

        binding.deleteAccountItem.titleTextView.setText(R.string.delete_account);
    }

    private void setItemValues(UserProfile profile) {
        binding.changeEmailAddressItem.valueTextView.setText(profile.getEmailAddress());
    }

    private void setListeners() {
        binding.changeEmailAddressItem.button.setOnClickListener(v -> openEmailAddressEditor());
        binding.changePasswordItem.button.setOnClickListener(v -> openPasswordEditor());
        binding.deleteAccountItem.button.setOnClickListener(v -> openAccountDeleter());
    }

    private void openEmailAddressEditor() {
        ActivityHelper.open(this, ChangeEmailAddressActivity.class, false);
    }

    private void openPasswordEditor() {
        ActivityHelper.open(this, ChangePasswordActivity.class, false);
    }

    private void openAccountDeleter() {
        ActivityHelper.open(this, DeleteAccountActivity.class, false);
    }
}