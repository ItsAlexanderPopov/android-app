package com.example.easysale.userpage;

import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import com.example.easysale.viewmodel.UserViewModel;

public class EditUserValidations {

    public interface ValidationCallback {
        void onValidationComplete(boolean isValid);
    }

    public static void validateInputs(Context context, String firstName, String lastName, String email,
                                      TextInputEditText firstNameEditText, TextInputEditText lastNameEditText, TextInputEditText emailEditText,
                                      int currentUserId, UserViewModel userViewModel, ValidationCallback callback) {
        boolean isValid = validateName(context, firstName, firstNameEditText, "First name") &&
                validateName(context, lastName, lastNameEditText, "Last name") &&
                validateEmail(context, email, emailEditText);

        // If inputs are valid, check if email is unique
        if (isValid) {
            userViewModel.isEmailUnique(email, currentUserId, isUnique -> {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (!isUnique) {
                            emailEditText.setError("Email is already taken");
                            callback.onValidationComplete(false);
                        } else {
                            emailEditText.setError(null);
                            callback.onValidationComplete(true);
                        }
                    });
                }
            });
        } else {
            callback.onValidationComplete(false);
        }
    }

    // Special characters, numbers and different languages are allowed; we fix spaces and limit by lengths.
    public static boolean validateName(Context context, String name, TextInputEditText editText, String fieldName) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(() -> {
                if (name.isEmpty()) {
                    editText.setError(fieldName + " is required");
                } else if (name.length() < 2) {
                    editText.setError(fieldName + " must be at least 2 characters long");
                } else if (name.length() > 35) {
                    editText.setError(fieldName + " must not exceed 35 characters");
                } else {
                    editText.setError(null);
                    editText.setText(name);
                }
            });
        }
        return !name.isEmpty() && name.length() >= 2 && name.length() <= 35;
    }

    public static boolean validateEmail(Context context, String email, TextInputEditText editText) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(() -> {
                if (email.isEmpty()) {
                    editText.setError("Email is required");
                } else if (!isValidEmail(email)) {
                    editText.setError("Invalid email format");
                } else {
                    editText.setError(null);
                }
            });
        }
        return !email.isEmpty() && isValidEmail(email);
    }

    // Regular expression for email validation
    public static boolean isValidEmail(String email) {
        String emailPattern = "(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
        return email.matches(emailPattern);
    }

    // Remove extra spaces
    public static String cleanName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}