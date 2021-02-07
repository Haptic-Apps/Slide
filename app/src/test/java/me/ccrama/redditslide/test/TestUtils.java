package me.ccrama.redditslide.test;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class TestUtils {

    public static String getResource(String path) throws IOException {
        return IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream(path), "utf-8");
    }

    public static class MockPreferences implements SharedPreferences {
        private String pinned;

        public MockPreferences(String pinned) {
            this.pinned = pinned;
        }

        //Only method we care about
        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return pinned;
        }

        @Override
        public Map<String, ?> getAll() {
            return null;
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            return defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            return defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            return defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            return defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return defValue;
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public Editor edit() {
            return null;
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(
                OnSharedPreferenceChangeListener listener) {
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(
                OnSharedPreferenceChangeListener listener) {
        }
    }
}
