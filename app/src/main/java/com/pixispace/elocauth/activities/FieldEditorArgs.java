package com.pixispace.elocauth.activities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class FieldEditorArgs implements Parcelable {
    private final int maxLength;
    private final String fieldName, currentValue;

    public FieldEditorArgs(String fieldName, String currentValue, int maxLength) {
        this.fieldName = fieldName;
        this.currentValue = currentValue;
        this.maxLength = maxLength;
    }

    protected FieldEditorArgs(Parcel in) {
        maxLength = in.readInt();
        fieldName = in.readString();
        currentValue = in.readString();
    }

    public static final Creator<FieldEditorArgs> CREATOR = new Creator<FieldEditorArgs>() {
        @Override
        public FieldEditorArgs createFromParcel(Parcel in) {
            return new FieldEditorArgs(in);
        }

        @Override
        public FieldEditorArgs[] newArray(int size) {
            return new FieldEditorArgs[size];
        }
    };

    public int getMaxLength() {
        return maxLength;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(maxLength);
        dest.writeString(fieldName);
        dest.writeString(currentValue);
    }
}
