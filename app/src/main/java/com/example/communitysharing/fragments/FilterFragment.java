package com.example.communitysharing.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.communitysharing.R;

public class FilterFragment extends Fragment {

    private String selectedCategory = "all";

    private TextView chip1, chip2, chip3, chip4, chip5, chip6, chip7, chip8, chip9;
    private TextView btnApply, btnReset,btnBack;
    private TextView time1, time3, time7;
    private int selectedDays = 1;
    private String selectedStatus = "available";
    private int radius = 5;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        // ===== ÁNH XẠ =====
        chip1 = view.findViewById(R.id.chip1);
        chip2 = view.findViewById(R.id.chip2);
        chip3 = view.findViewById(R.id.chip3);
        chip4 = view.findViewById(R.id.chip4);
        chip5 = view.findViewById(R.id.chip5);
        chip6 = view.findViewById(R.id.chip6);
        chip7 = view.findViewById(R.id.chip7);
        chip8 = view.findViewById(R.id.chip8);
        chip9 = view.findViewById(R.id.chip9);

        TextView btnAvailable = view.findViewById(R.id.btnAvailable);
        TextView btnNeeded = view.findViewById(R.id.btnNeeded);

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        TextView tvMiles = view.findViewById(R.id.tvMiles);
        View layoutTime = view.findViewById(R.id.layoutTime);

        TextView tvTime = view.findViewById(R.id.tvTimeSelected);

        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);

        // ===== CLICK CHIP =====
        chip1.setOnClickListener(v -> selectChip(chip1, "furniture"));
        chip2.setOnClickListener(v -> selectChip(chip2, "food"));
        chip3.setOnClickListener(v -> selectChip(chip3, "clothes"));
        chip4.setOnClickListener(v -> selectChip(chip4, "tool"));
        chip5.setOnClickListener(v -> selectChip(chip5, "electronic"));
        chip6.setOnClickListener(v -> selectChip(chip6, "kitchen"));
        chip7.setOnClickListener(v -> selectChip(chip7, "garden"));
        chip8.setOnClickListener(v -> selectChip(chip8, "book"));
        chip9.setOnClickListener(v -> selectChip(chip9, "others"));
        layoutTime.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), layoutTime);

            popup.getMenu().add(getString(R.string.filter_last_24_hours));
            popup.getMenu().add(getString(R.string.filter_last_3_days));
            popup.getMenu().add(getString(R.string.filter_last_7_days));

            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();
                tvTime.setText(selected);

                if (selected.equals(getString(R.string.filter_last_24_hours))) selectedDays = 1;
                else if (selected.equals(getString(R.string.filter_last_3_days))) selectedDays = 3;
                else if (selected.equals(getString(R.string.filter_last_7_days))) selectedDays = 7;

                return true;
            });

            popup.show();
        });

        // button back//
        ImageView btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ===== CLICK BUTTON STATUS =====
        btnAvailable.setOnClickListener(v -> {
            selectedStatus = "available";

            btnAvailable.setBackgroundResource(R.drawable.bg_button_primary);
            btnAvailable.setTextColor(getResources().getColor(R.color.colorPrimary));

            btnNeeded.setBackgroundResource(R.drawable.bg_button_secondary);
            btnNeeded.setTextColor(getResources().getColor(R.color.colorTextDark));
        });

        btnNeeded.setOnClickListener(v -> {
            selectedStatus = "needed";

            btnNeeded.setBackgroundResource(R.drawable.bg_button_primary);
            btnNeeded.setTextColor(getResources().getColor(R.color.colorPrimary));

            btnAvailable.setBackgroundResource(R.drawable.bg_button_secondary);
            btnAvailable.setTextColor(getResources().getColor(R.color.colorTextDark));
        });
        // ===== APPLY =====
        btnApply.setOnClickListener(v -> {

            FilterResultFragment fragment = new FilterResultFragment();

            Bundle bundle = new Bundle();
            bundle.putString("category", selectedCategory);
            bundle.putString("status", selectedStatus);
            bundle.putInt("radius", radius);
            bundle.putInt("days", selectedDays);

            fragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ===== RADIUS =====


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress;

                if (radius == 0) radius = 1;

                tvMiles.setText(getString(R.string.filter_radius_miles, radius));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // ===== TIME =====

            // ===== RESET =====
        btnReset.setOnClickListener(v -> {
            selectedCategory = "all";
            resetAllChips();
        });

        return view;
    }


    // ===== CHỌN CHIP =====
    private void selectChip(TextView chip, String category) {
        selectedCategory = category;

        resetAllChips();

        chip.setBackgroundResource(R.drawable.bg_button_primary);
        chip.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    //seek radius//


    // ===== RESET CHIP =====
    private void resetAllChips() {
        TextView[] chips = {
                chip1, chip2, chip3, chip4, chip5,
                chip6, chip7, chip8, chip9
        };

        for (TextView c : chips) {
            c.setBackgroundResource(R.drawable.bg_button_secondary);
            c.setTextColor(getResources().getColor(R.color.colorTextDark));
        }
    }
}
