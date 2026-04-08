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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFullName, etEmail, etPhone, etAddress;
    private EditText etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvError, tvGoToLogin;
    private ImageView ivTogglePassword;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Biến theo dõi ẩn/hiện mật khẩu
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ view
        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etPhone           = findViewById(R.id.etPhone);
        etAddress         = findViewById(R.id.etAddress);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister       = findViewById(R.id.btnRegister);
        tvError           = findViewById(R.id.tvError);
        tvGoToLogin       = findViewById(R.id.tvGoToLogin);
        ivTogglePassword  = findViewById(R.id.ivTogglePassword);

        // Nút đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Toggle ẩn/hiện password
        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Ẩn password
                    etPassword.setTransformationMethod(
                            PasswordTransformationMethod.getInstance());
                    isPasswordVisible = false;
                } else {
                    // Hiện password
                    etPassword.setTransformationMethod(
                            HideReturnsTransformationMethod.getInstance());
                    isPasswordVisible = true;
                }
                // Giữ con trỏ ở cuối
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // Chuyển sang Login
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại LoginActivity
            }
        });
    }

    private void registerUser() {
        // Lấy dữ liệu từ form
        String fullName  = etFullName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String address   = etAddress.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        String confirmPw = etConfirmPassword.getText().toString().trim();

        // --- Validate từng trường ---
        if (TextUtils.isEmpty(fullName)) {
            showError("Please enter your full name");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showError("Please enter your email");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            showError("Please enter your phone number");
            etPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            showError("Please enter your address");
            etAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showError("Please enter a password");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPw)) {
            showError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Ẩn lỗi, disable nút để tránh bấm 2 lần
        tvError.setVisibility(View.GONE);
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");

        // --- Gọi Firebase Auth tạo tài khoản ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công → lưu thông tin vào Realtime Database
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();

                        // Tạo object User
                        Users newUser = new Users(uid, fullName, email, phone, address);

                        // Lưu vào node "users/{uid}" trên Firebase
                        mDatabase.child("users").child(uid).setValue(newUser)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this,
                                                "Welcome to The Commons, " + fullName + "!",
                                                Toast.LENGTH_SHORT).show();

                                        // Chuyển sang HomeActivity (màn hình chính)
//                                        Intent intent = new Intent(
//                                                RegisterActivity.this,
//                                                HomeActivity.class);
//                                        // Xóa back stack để không quay lại Register
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
                                    } else {
                                        showError("Failed to save user data. Try again.");
                                        btnRegister.setEnabled(true);
                                        btnRegister.setText("Create Account");
                                    }
                                });
                    } else {
                        // Đăng ký thất bại
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        showError(errorMsg);
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Create Account");
                    }
                });
    }

    // Hàm hiển thị lỗi
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

}
