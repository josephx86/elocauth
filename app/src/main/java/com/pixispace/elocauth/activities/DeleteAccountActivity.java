package com.pixispace.elocauth.activities;

import android.os.Bundle;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.callbacks.VoidCallback;
import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.databinding.ActivityDeleteAccountBinding;

public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;
    private UserAccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);

        setToolbar();
        setListeners();
        updateUI(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private void updateUI(boolean locked) {
        if (locked) {
            binding.passwordLayout.setEnabled(false);
            binding.submitButton.setEnabled(false);
            binding.progressHorizontal.setVisibility(View.VISIBLE);
        } else {
            binding.passwordLayout.setEnabled(true);
            binding.submitButton.setEnabled(true);
            binding.progressHorizontal.setVisibility(View.GONE);
        }
    }

    private void setToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.delete_account);
        }
    }

    private void setListeners() {
        binding.root.setOnClickListener(v -> ActivityHelper.hideKeyboard(this));
        binding.submitButton.setOnClickListener(v -> submit());
        binding.passwordTextInput.addTextChangedListener(new TextInputWatcher(binding.passwordLayout));
    }

    private void submit() {
        ActivityHelper.hideKeyboard(this);
        binding.passwordLayout.setError(null);

        String password = "";
        Editable editable = binding.passwordTextInput.getEditableText();
        if (editable != null) {
            password = editable.toString();
        }
        if (password.isEmpty()) {
            binding.passwordLayout.setError(getString(R.string.required));
            return;
        }

        updateUI(true);
        viewModel.verifyPassword(password, this::onPasswordVerificationCompleted);
    }

    private void onPasswordVerificationCompleted(String error) {
        if (error == null) {
            error = "";
        }
        error = error.trim();
        if (error.isEmpty()) {
            viewModel.deleteRemoteFiles(this::onRemoteFilesDeleted);
        } else {
            ActivityHelper.showModalAlert(
                    this,
                    getString(R.string.account),
                    error,
                    () -> updateUI(false)
            );
        }
    }

    private void onRemoteFilesDeleted(boolean success) {
        if (success) {
            viewModel.deleteProfile(this::onProfileDeleted);
        } else {
            ActivityHelper.showModalAlert(
                    this,
                    getString(R.string.oops),
                    getString(R.string.failed_to_remove_remote_files),
                    () -> updateUI(false)
            );
        }
    }

    private void onProfileDeleted(boolean success) {
        if (success) {
            viewModel.deleteAuthAccount(this::onAuthAccountDeleted);
        } else {
            ActivityHelper.showModalAlert(
                    this,
                    getString(R.string.oops),
                    getString(R.string.failed_to_clear_profile),
                    () -> updateUI(false)
            );
        }
    }

    private void onAuthAccountDeleted(boolean success) {
        if (success) {
            ActivityHelper.showModalAlert(
                    this,
                    getString(R.string.account),
                    "Your account has been deleted",
                    () -> {
                        viewModel.signOut();
                        ActivityHelper.open(this, LoginActivity.class, true);
                    }
            );
        } else {
            ActivityHelper.showModalAlert(
                    this,
                    getString(R.string.oops),
                    getString(R.string.failed_to_remove_account),
                    () -> updateUI(false)
            );
        }
    }
}