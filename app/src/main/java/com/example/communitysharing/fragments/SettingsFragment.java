package com.example.communitysharing.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.LoginActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {

    // Views
    private ImageView ivBack;
    private TextView tvEmail;
    private LinearLayout llChangePassword, llEmailAddress;
    private LinearLayout llLanguage, btnLogout;
    private Switch switchDarkMode, switchPushNotif, switchEmailDigest;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_settings, container, false);

        mAuth       = FirebaseAuth.getInstance();
        mDatabase   = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        // Ánh xạ views
        ivBack             = view.findViewById(R.id.ivBack);
        tvEmail            = view.findViewById(R.id.tvEmail);
        llChangePassword   = view.findViewById(R.id.llChangePassword);
        llEmailAddress     = view.findViewById(R.id.llEmailAddress);
        llLanguage         = view.findViewById(R.id.llLanguage);
        btnLogout          = view.findViewById(R.id.btnLogout);
        switchDarkMode     = view.findViewById(R.id.switchDarkMode);
        switchPushNotif    = view.findViewById(R.id.switchPushNotif);
        switchEmailDigest  = view.findViewById(R.id.switchEmailDigest);

        // Hiện email hiện tại
        if (currentUser != null && currentUser.getEmail() != null) {
            tvEmail.setText(currentUser.getEmail());
        }

        // Load trạng thái switch từ Firebase
        loadSettings();

        // Setup click listeners
        setupListeners();

        return view;
    }

    private void loadSettings() {
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        mDatabase.child("settings").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Lấy giá trị switch từ Firebase
                        Boolean darkMode = snapshot
                                .child("darkMode").getValue(Boolean.class);
                        Boolean pushNotif = snapshot
                                .child("pushNotif").getValue(Boolean.class);
                        Boolean emailDigest = snapshot
                                .child("emailDigest").getValue(Boolean.class);

                        // Set giá trị vào switch
                        // Dùng null check để tránh crash
                        switchDarkMode.setChecked(
                                darkMode != null && darkMode);
                        switchPushNotif.setChecked(
                                pushNotif == null || pushNotif); // mặc định true
                        switchEmailDigest.setChecked(
                                emailDigest != null && emailDigest);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupListeners() {

        // Back
        ivBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Change Password → hiện dialog
        llChangePassword.setOnClickListener(v ->
                showChangePasswordDialog());

        // Email Address → hiện dialog đổi email
        llEmailAddress.setOnClickListener(v ->
                showChangeEmailDialog());

        // Language → hiện dialog chọn ngôn ngữ
        llLanguage.setOnClickListener(v ->
                showLanguageDialog());

        // Switch Dark Mode
        switchDarkMode.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        saveSetting("darkMode", isChecked));

        // Switch Push Notifications
        switchPushNotif.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        saveSetting("pushNotif", isChecked));

        // Switch Email Digest
        switchEmailDigest.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        saveSetting("emailDigest", isChecked));

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // Lưu setting vào Firebase
    private void saveSetting(String key, boolean value) {
        if (currentUser == null) return;
        mDatabase.child("settings")
                .child(currentUser.getUid())
                .child(key)
                .setValue(value);
    }

    // ===== DIALOG ĐỔI MẬT KHẨU =====
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_change_password, null);

        EditText etCurrentPw = dialogView
                .findViewById(R.id.etCurrentPassword);
        EditText etNewPw     = dialogView
                .findViewById(R.id.etNewPassword);
        EditText etConfirmPw = dialogView
                .findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(getContext())
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String currentPw = etCurrentPw.getText()
                            .toString().trim();
                    String newPw     = etNewPw.getText()
                            .toString().trim();
                    String confirmPw = etConfirmPw.getText()
                            .toString().trim();

                    // Validate
                    if (currentPw.isEmpty() || newPw.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Please fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPw.length() < 6) {
                        Toast.makeText(getContext(),
                                "Password must be at least 6 characters",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPw.equals(confirmPw)) {
                        Toast.makeText(getContext(),
                                "Passwords do not match",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Xác thực lại rồi đổi mật khẩu
                    changePassword(currentPw, newPw);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changePassword(String currentPw, String newPw) {
        if (currentUser == null) return;

        // Phải re-authenticate trước khi đổi mật khẩu
        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(), currentPw);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Re-auth xong → đổi mật khẩu
                        currentUser.updatePassword(newPw)
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(getContext(),
                                                "Password updated successfully!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Failed to update password",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(),
                                "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ===== DIALOG ĐỔI EMAIL =====
    private void showChangeEmailDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_change_email, null);

        EditText etNewEmail  = dialogView
                .findViewById(R.id.etNewEmail);
        EditText etPassword  = dialogView
                .findViewById(R.id.etPassword);

        // Pre-fill email hiện tại
        if (currentUser != null) {
            etNewEmail.setText(currentUser.getEmail());
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Change Email")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newEmail  = etNewEmail.getText()
                            .toString().trim();
                    String password  = etPassword.getText()
                            .toString().trim();

                    if (newEmail.isEmpty() || password.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Please fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    changeEmail(newEmail, password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changeEmail(String newEmail, String password) {
        if (currentUser == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(), password);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.updateEmail(newEmail)
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        // Cập nhật email trong Database
                                        mDatabase.child("users")
                                                .child(currentUser.getUid())
                                                .child("email")
                                                .setValue(newEmail);

                                        tvEmail.setText(newEmail);
                                        Toast.makeText(getContext(),
                                                "Email updated successfully!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Failed to update email",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(),
                                "Password is incorrect",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ===== DIALOG CHỌN NGÔN NGỮ =====
    private void showLanguageDialog() {
        String[] languages = {
                "English (United States)",
                "Tiếng Việt",
                "中文",
                "日本語",
                "한국어"
        };

        new AlertDialog.Builder(getContext())
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    // Lưu ngôn ngữ vào Firebase
                    saveSetting("language", which);
                    Toast.makeText(getContext(),
                            languages[which] + " selected",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Overload saveSetting cho int
    private void saveSetting(String key, int value) {
        if (currentUser == null) return;
        mDatabase.child("settings")
                .child(currentUser.getUid())
                .child(key)
                .setValue(value);
    }

    // ===== DIALOG LOGOUT =====
    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(getContext(),
                            LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}