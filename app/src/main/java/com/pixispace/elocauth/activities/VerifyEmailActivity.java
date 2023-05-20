package com.pixispace.elocauth.activities;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.callbacks.VoidCallback;
import com.pixispace.elocauth.data.UserAccountViewModel;
import com.pixispace.elocauth.databinding.ActivityVerifyEmailBinding;

public class VerifyEmailActivity extends NoActionBarActivity {
    private ActivityVerifyEmailBinding binding;
    private UserAccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(UserAccountViewModel.class);
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthState();
    }

    private void setListeners() {
        binding.verifiedButton.setOnClickListener(v -> signIn());
        binding.resendButton.setOnClickListener(v -> viewModel.sendEmailVerificationLink(this::onResendCompleted));
    }

    private void updateUI(boolean showProgress) {
        if (showProgress) {
            binding.progressHorizontal.setVisibility(View.VISIBLE);
            binding.messageLayout.setVisibility(View.GONE);
        } else {
            binding.progressHorizontal.setVisibility(View.GONE);
            binding.messageLayout.setVisibility(View.VISIBLE);
        }
    }

    private void onHasProfileCompleted(boolean hasProfile) {
        if (hasProfile) {
            ActivityHelper.open(this, LandingActivity.class, true);
        } else {
            ActivityHelper.open(this, SetupActivity.class, true);
        }
    }

    private void onResendCompleted(boolean sent) {
        String title, message;
        if (sent) {
            title = getString(R.string.link_sent);
            message = getString(R.string.resend_message);
        } else {
            title = getString(R.string.oops);
            message = getString(R.string.resend_failed);
        }
        ActivityHelper.showModalAlert(this, title, message);
    }

    private void signIn() {
        String title = getString(R.string.email_verification);
        String message = getString(R.string.redirect_to_sign_in);
        VoidCallback callback = () -> {
            viewModel.signOut();
            ActivityHelper.open(VerifyEmailActivity.this, LoginActivity.class, true);
        };
        ActivityHelper.showModalAlert(this, title, message, callback);
    }

    private void checkAuthState() {
        updateUI(true);
        if (viewModel.isSignedIn()) {
            if (viewModel.isEmailVerified()) {
                viewModel.hasProfile(this::onHasProfileCompleted);
            } else {
                String message = getString(R.string.verification_message, viewModel.getEmailAddress());
                binding.messageTextView.setText(message);
                updateUI(false);
            }
        } else {
            ActivityHelper.open(this, LoginActivity.class, true);
        }
    }
}