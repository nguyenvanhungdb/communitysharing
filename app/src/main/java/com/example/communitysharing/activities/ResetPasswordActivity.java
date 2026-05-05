package com.example.communitysharing.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.communitysharing.R;
import com.example.communitysharing.utils.LocaleManager;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSendReset;
    private TextView tvError, tvSuccess;
    private LinearLayout llBackToLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.applySavedLocale(this);
        setContentView(R.layout.activity_reset_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        etEmail       = findViewById(R.id.etEmail);
        btnSendReset  = findViewById(R.id.btnSendReset);
        tvError       = findViewById(R.id.tvError);
        tvSuccess     = findViewById(R.id.tvSuccess);
        llBackToLogin = findViewById(R.id.llBackToLogin);

        // Nút Back to Login
        llBackToLogin.setOnClickListener(v -> finish());

        // Nút Send Reset Link
        btnSendReset.setOnClickListener(v -> sendResetEmail());
    }
    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(email)) {
            showError(getString(R.string.reset_email_required));
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.reset_valid_email_required));
            etEmail.requestFocus();
            return;
        }

        // Ẩn thông báo cũ, disable nút
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
        btnSendReset.setEnabled(false);
        btnSendReset.setText(getString(R.string.reset_sending));
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Gửi thành công
                        showSuccess(getString(R.string.reset_success, email));
                        btnSendReset.setText(getString(R.string.reset_resend_link));
                        btnSendReset.setEnabled(true);
                        etEmail.setEnabled(false); // Khoá ô email sau khi gửi
                    } else {
                        // Thất bại (email không tồn tại, v.v.)
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.reset_failed);
                        showError(msg);
                        btnSendReset.setEnabled(true);
                        btnSendReset.setText(getString(R.string.reset_send_link));
                    }
                });
    }
    private void showError(String message) {
        tvSuccess.setVisibility(View.GONE);
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String message) {
        tvError.setVisibility(View.GONE);
        tvSuccess.setText("✓  " + message);
        tvSuccess.setVisibility(View.VISIBLE);
    }
}
