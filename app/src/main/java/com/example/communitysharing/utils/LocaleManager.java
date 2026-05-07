package com.example.communitysharing.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LocaleManager {
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_VIETNAMESE = "vi";

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "language";

    private LocaleManager() {
    }

    public static void applySavedLocale(Context context) {
        if (!hasSavedLanguage(context)) {
            return;
        }
        String language = getLanguageCode(context);
        String current = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (!language.equals(current)) {
            AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language));
        }
    }

    public static void setLanguage(Context context, String languageCode) {
        String normalizedCode = normalizeLanguageCode(languageCode);
        getPreferences(context)
                .edit()
                .putString(KEY_LANGUAGE, normalizedCode)
                .apply();
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(normalizedCode));
    }

    public static String getLanguageCode(Context context) {
        String languageCode = getPreferences(context)
                .getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
        return normalizeLanguageCode(languageCode);
    }

    public static boolean hasSavedLanguage(Context context) {
        return getPreferences(context).contains(KEY_LANGUAGE);
    }

    public static int getLanguageIndex(Context context, String[] languageCodes) {
        String currentLanguage = getLanguageCode(context);
        for (int i = 0; i < languageCodes.length; i++) {
            if (currentLanguage.equals(languageCodes[i])) {
                return i;
            }
        }
        return 0;
    }

    public static String normalizeLanguageCode(String languageCode) {
        if (LANGUAGE_VIETNAMESE.equals(languageCode)) {
            return LANGUAGE_VIETNAMESE;
        }
        return LANGUAGE_ENGLISH;
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
