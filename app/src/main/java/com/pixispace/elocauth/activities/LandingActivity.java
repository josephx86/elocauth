package com.pixispace.elocauth.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.databinding.ActivityLandingBinding;

public class LandingActivity extends AppCompatActivity {
    private ActivityLandingBinding binding;
    private
    UserAccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);
        setObservers();
        setListeners();
        showInfo();
    }

    private void setListeners() {
        binding.signOutButton.setOnClickListener(
                v -> {
                    viewModel.signOut();
                    onSignOut();
                });
    }

    void onSignOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showInfo() {
        //String text = viewModel.getAuthType() == UserAccountRepository.AuthType.google ? "Signed in with: Google" : "Signed in with: Email/password";
        binding.accountType.setText("text");
        binding.emailAddressLabel.setText(viewModel.getEmailAddress());
    }

    private void setObservers() {
        viewModel.getDisplayName().observe(this, s -> binding.displayNameLabel.setText(s));
    }
}