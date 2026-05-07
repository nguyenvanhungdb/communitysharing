package com.example.communitysharing.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.communitysharing.R;
import com.example.communitysharing.utils.LocaleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private TextView tvForgotPassword, tvError;
    private ImageView ivTogglePassword;

    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.applySavedLocale(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Nếu đã đăng nhập rồi → vào thẳng Home
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToHome();
            return;
        }
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        btnGoToRegister  = findViewById(R.id.btnGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvError          = findViewById(R.id.tvError);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        // Nút Login
        btnLogin.setOnClickListener(v -> loginUser());

        // Nút Sign Up → sang Register
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Forgot Password → sang ResetPassword
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });

        // Toggle password
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance());
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance());
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void loginUser() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(email)) {
            showError(getString(R.string.login_enter_email_error));
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showError(getString(R.string.login_enter_password_error));
            etPassword.requestFocus();
            return;
        }

        // Disable nút tránh bấm 2 lần
        btnLogin.setEnabled(false);
        btnLogin.setText(getString(R.string.login_logging_in));
        tvError.setVisibility(View.GONE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        goToHome();
                    } else {
                        // Sai email hoặc password
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.login_failed);
                        showError(msg);
                        btnLogin.setEnabled(true);
                        btnLogin.setText(getString(R.string.login_button));
                    }
                });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
