package me.ccrama.redditslide.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

/**
 * Created by Fernando Barillas on 5/2/16.
 * Allows easier validation of EditText input via the use of an InputFilter. This way invalid text
 * is not allowed to be input.
 */
public class EditTextValidator {

    private EditTextValidator() {
    }

    /**
     * Validates EditTexts intended for reddit username input. Valid characters include:
     * A-Z, a-z
     * 0-9
     * - (hyphen)
     * _ (underscore)
     *
     * @param editText The EditText to validate a username for
     */
    public static void validateUsername(final EditText editText) {
        if (editText == null) return;
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (!Character.isLetterOrDigit(character)
                            && character != '_'
                            && character != '-') {
                        return "";
                    }
                }

                return null;
            }
        };

        editText.setFilters(new InputFilter[]{filter});
    }

    // TODO: Add validation for subreddits/multireddits
}
