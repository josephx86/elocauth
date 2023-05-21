package com.pixispace.elocauth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.MenuItem;

import com.pixispace.elocauth.R;
import com.pixispace.elocauth.databinding.ActivityTextEditorBinding;

public class TextEditorActivity extends AppCompatActivity {

    public static final String EXTRA_DATA = "field_data";
    public static final String RESULT_MESSAGE = "result_message";
    private ActivityTextEditorBinding binding;
    private
    FieldEditorArgs fieldData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setData();
        setToolbar();
        setListeners();
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
            if (fieldData != null) {
                actionBar.setTitle(fieldData.getFieldName());
            }
        }
    }

    private void setData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                fieldData = extras.getParcelable(EXTRA_DATA, FieldEditorArgs.class);
            } else {
                fieldData = extras.getParcelable(EXTRA_DATA);
            }
        }
        if (fieldData == null) {
            String message = getString(R.string.invalid_field_data);
            goBack(message, false);
        } else {
            binding.currentValueTextView.setText(fieldData.getCurrentValue());
            binding.textEditLayout.setCounterMaxLength(fieldData.getMaxLength());
            InputFilter[] filters = binding.editText.getFilters();
            for (int i = 0; i < filters.length; i++) {
                if (filters[i] instanceof InputFilter.LengthFilter) {
                    filters[i] = new InputFilter.LengthFilter(fieldData.getMaxLength());
                }
            }
            binding.editText.setFilters(filters);
        }
    }

    private void setListeners() {
        binding.root.setOnClickListener(v -> ActivityHelper.hideKeyboard(this));
        binding.saveButton.setOnClickListener(v -> save());
        binding.editText.addTextChangedListener(new TextInputWatcher(binding.textEditLayout));
    }

    private void save() {
        ActivityHelper.hideKeyboard(this);
        binding.textEditLayout.setError(null);

        String newValue = "";
        Editable editable = binding.editText.getEditableText();
        if (editable != null) {
            newValue = editable.toString().trim();
        }
        if (newValue.isEmpty()) {
            binding.textEditLayout.setError(getString(R.string.required));
            return;
        }

        goBack(newValue, true);
    }

    private void goBack(String data, boolean success) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_MESSAGE, data);
        setResult(success ? RESULT_OK : RESULT_CANCELED, intent);
        onBackPressed();
    }
}