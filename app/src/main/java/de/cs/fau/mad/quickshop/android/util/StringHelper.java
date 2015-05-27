package de.cs.fau.mad.quickshop.android.util;

public class StringHelper {


    public static boolean isNullOrWhiteSpace(CharSequence value) {
        return value == null || isNullOrWhiteSpace(value.toString());
    }

    public static boolean isNullOrWhiteSpace(String value) {
        if (value == null) {
            return true;
        } else {
            return value.trim().length() == 0;
        }
    }
}
