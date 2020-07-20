package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Editable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.text.StringEscapeUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import me.ccrama.redditslide.Activities.Draw;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by carlo_000 on 10/18/2015.
 */
public class DoEditorActions {

    public static void doActions(final EditText editText, final View baseView,
            final FragmentManager fm, final Activity a, final String oldComment,
            @Nullable final String[] authors) {
        baseView.findViewById(R.id.bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.hasSelection()) {
                    wrapString("**",
                            editText); //If the user has text selected, wrap that text in the symbols
                } else {
                    //If the user doesn't have text selected, put the symbols around the cursor's position
                    int pos = editText.getSelectionStart();
                    editText.getText().insert(pos, "**");
                    editText.getText().insert(pos + 1, "**");
                    editText.setSelection(pos + 2); //put the cursor between the symbols
                }
            }
        });

        if (baseView.findViewById(R.id.author) != null) {
            if (authors != null && authors.length > 0) {
                baseView.findViewById(R.id.author).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (authors.length == 1) {
                            String author =  "/u/" + authors[0];
                            insertBefore(author, editText);
                        } else {
                            new AlertDialogWrapper.Builder(a).setTitle(R.string.authors_above)
                                    .setItems(authors, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String author =  "/u/" + authors[which];
                                            insertBefore(author, editText);
                                        }
                                    })
                                    .setNeutralButton(R.string.btn_cancel, null)
                                    .show();
                        }
                    }
                });
            } else {
                baseView.findViewById(R.id.author).setVisibility(View.GONE);
            }
        }

        baseView.findViewById(R.id.italics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.hasSelection()) {
                    wrapString("*",
                            editText); //If the user has text selected, wrap that text in the symbols
                } else {
                    //If the user doesn't have text selected, put the symbols around the cursor's position
                    int pos = editText.getSelectionStart();
                    editText.getText().insert(pos, "*");
                    editText.getText().insert(pos + 1, "*");
                    editText.setSelection(pos + 1); //put the cursor between the symbols
                }
            }
        });

        baseView.findViewById(R.id.strike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.hasSelection()) {
                    wrapString("~~",
                            editText); //If the user has text selected, wrap that text in the symbols
                } else {
                    //If the user doesn't have text selected, put the symbols around the cursor's position
                    int pos = editText.getSelectionStart();
                    editText.getText().insert(pos, "~~");
                    editText.getText().insert(pos + 2, "~~");
                    editText.setSelection(pos + 2); //put the cursor between the symbols
                }
            }
        });

        baseView.findViewById(R.id.spoiler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.hasSelection()) {
                    wrapString(">!", "!<", editText); //If the user has text selected, wrap that text in the symbols
                } else {
                    //If the user doesn't have text selected, put the symbols around the cursor's position
                    int pos = editText.getSelectionStart();
                    editText.getText().insert(pos, ">!");
                    editText.getText().insert(pos + 2, "!<");
                    editText.setSelection(pos + 2); //put the cursor between the symbols
                }
            }
        });

        baseView.findViewById(R.id.savedraft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drafts.addDraft(editText.getText().toString());
                Snackbar s = Snackbar.make(baseView.findViewById(R.id.savedraft), "Draft saved",
                        Snackbar.LENGTH_SHORT);
                View view = s.getView();
                TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                s.setAction(R.string.btn_discard, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Drafts.deleteDraft(Drafts.getDrafts().size() - 1);
                    }
                });
                s.show();
            }
        });
        baseView.findViewById(R.id.draft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> drafts = Drafts.getDrafts();
                Collections.reverse(drafts);
                final String[] draftText = new String[drafts.size()];
                for (int i = 0; i < drafts.size(); i++) {
                    draftText[i] = drafts.get(i);
                }
                if (drafts.isEmpty()) {
                    new AlertDialogWrapper.Builder(a).setTitle(R.string.dialog_no_drafts)
                            .setMessage(R.string.dialog_no_drafts_msg)
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                } else {
                    new AlertDialogWrapper.Builder(a).setTitle(R.string.choose_draft)
                            .setItems(draftText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editText.setText(
                                            editText.getText().toString() + draftText[which]);
                                }
                            })
                            .setNeutralButton(R.string.btn_cancel, null)
                            .setPositiveButton(R.string.manage_drafts,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final boolean[] selected = new boolean[drafts.size()];
                                            new AlertDialogWrapper.Builder(a).setTitle(
                                                    R.string.choose_draft)
                                                    .setNeutralButton(R.string.btn_cancel, null)
                                                    .alwaysCallMultiChoiceCallback()
                                                    .setNegativeButton(R.string.btn_delete,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialog,
                                                                        int which) {
                                                                    new AlertDialogWrapper.Builder(
                                                                            a).setTitle(
                                                                            R.string.really_delete_drafts)
                                                                            .setCancelable(false)
                                                                            .setPositiveButton(
                                                                                    R.string.btn_yes,
                                                                                    new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(
                                                                                                DialogInterface dialog,
                                                                                                int which) {
                                                                                            ArrayList<String>
                                                                                                    draf =
                                                                                                    new ArrayList<>();
                                                                                            for (int
                                                                                                    i =
                                                                                                    0;
                                                                                                    i
                                                                                                            < draftText.length;
                                                                                                    i++) {
                                                                                                if (!selected[i]) {
                                                                                                    draf.add(
                                                                                                            draftText[i]);
                                                                                                }
                                                                                            }
                                                                                            Drafts.save(
                                                                                                    draf);
                                                                                        }
                                                                                    })
                                                                            .setNegativeButton(
                                                                                    R.string.btn_no,
                                                                                    null)
                                                                            .show();
                                                                }
                                                            })
                                                    .setMultiChoiceItems(draftText, selected,
                                                            new DialogInterface.OnMultiChoiceClickListener() {
                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialog,
                                                                        int which,
                                                                        boolean isChecked) {
                                                                    selected[which] = isChecked;
                                                                }
                                                            })
                                                    .show();
                                        }
                                    })
                            .show();
                }
            }
        });
        baseView.findViewById(R.id.imagerep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e = editText.getText();

                sStart = editText.getSelectionStart();
                sEnd = editText.getSelectionEnd();

                TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(editText.getContext())
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(List<Uri> uri) {
                                handleImageIntent(uri, editText, a);
                            }
                        })
                        .setLayoutResource(R.layout.image_sheet_dialog)
                        .setTitle("Choose a photo")
                        .create();

                tedBottomPicker.show(fm);
                InputMethodManager imm = (InputMethodManager) editText.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        });

        baseView.findViewById(R.id.draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.isPro) {
                    doDraw(a, editText, fm);
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(a).setTitle(
                            R.string.general_cropdraw_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                a.startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse("market://details?id="
                                                                + a.getString(
                                                                R.string.ui_unlock_package))));
                                            } catch (ActivityNotFoundException e) {
                                                a.startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id="
                                                                        + a.getString(
                                                                        R.string.ui_unlock_package))));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                    if (SettingValues.previews > 0) {
                        b.setNeutralButton(
                                a.getString(R.string.pro_previews, SettingValues.previews),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SettingValues.prefs.edit()
                                                .putInt(SettingValues.PREVIEWS_LEFT,
                                                        SettingValues.previews - 1)
                                                .apply();
                                        SettingValues.previews = SettingValues.prefs.getInt(
                                                SettingValues.PREVIEWS_LEFT, 10);
                                        doDraw(a, editText, fm);
                                    }
                                });
                    }
                    b.show();
                }
            }
        });
       /*todo baseView.findViewById(R.id.superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBefore("^", editText);
            }
        });*/
        baseView.findViewById(R.id.size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBefore("#", editText);
            }
        });

        baseView.findViewById(R.id.quote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (oldComment != null) {
                    final TextView showText = new TextView(a);
                    showText.setText(StringEscapeUtils.unescapeHtml4(oldComment)); // text we get is escaped, we don't want that
                    showText.setTextIsSelectable(true);
                    int sixteen = Reddit.dpToPxVertical(24);
                    showText.setPadding(sixteen, 0, sixteen, 0);
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(a);
                    builder.customView(showText, false)
                            .title(R.string.editor_actions_quote_comment)
                            .cancelable(true)
                            .positiveText(a.getString(R.string.btn_select))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    String selected = showText.getText()
                                            .toString()
                                            .substring(showText.getSelectionStart(), showText.getSelectionEnd());
                                    if (selected.equals("")) {
                                        selected = StringEscapeUtils.unescapeHtml4(oldComment);
                                    }
                                    insertBefore("> " + selected.replaceAll("\n", "\n> ") + "\n\n", editText);
                                }
                            })
                            .negativeText(a.getString(R.string.btn_cancel))
                            .show();
                    InputMethodManager imm = (InputMethodManager) editText.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                } else {
                    insertBefore("> ", editText);
                }
            }
        });

        baseView.findViewById(R.id.bulletlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                String selected = editText.getText().toString().substring(Math.min(start, end), Math.max(start, end));
                if (!selected.equals("")) {
                    selected = selected.replaceFirst("^[^\n]", "* $0").replaceAll("\n", "\n* ");
                    editText.getText().replace(Math.min(start, end), Math.max(start, end), selected);
                } else {
                    insertBefore("* ", editText);
                }
            }
        });

        baseView.findViewById(R.id.numlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                String selected = editText.getText().toString().substring(Math.min(start, end), Math.max(start, end));
                if (!selected.equals("")) {
                    selected = selected.replaceFirst("^[^\n]", "1. $0").replaceAll("\n", "\n1. ");
                    editText.getText().replace(Math.min(start, end), Math.max(start, end), selected);
                } else {
                    insertBefore("1. ", editText);
                }
            }
        });

        baseView.findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Extension> extensions =
                        Arrays.asList(TablesExtension.create(), StrikethroughExtension.create());
                Parser parser = Parser.builder().extensions(extensions).build();
                HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
                Node document = parser.parse(editText.getText().toString());
                String html = renderer.render(document);
                LayoutInflater inflater = a.getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.parent_comment_dialog, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(a);
                setViews(html, "NO sub",
                        (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.firstTextView),
                        (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow));
                builder.setView(dialoglayout);
                builder.show();
            }
        });

        baseView.findViewById(R.id.link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = LayoutInflater.from(a);
                final LinearLayout layout =
                        (LinearLayout) inflater.inflate(R.layout.insert_link, null);

                int[] attrs = {R.attr.fontColor};

                TypedArray ta = baseView.getContext()
                        .obtainStyledAttributes(
                                new ColorPreferences(baseView.getContext()).getFontStyle()
                                        .getBaseId(), attrs);
                ta.recycle();

                String selectedText = "";
                //if the user highlighted text before inputting a URL, use that text for the descriptionBox
                if (editText.hasSelection()) {
                    final int startSelection = editText.getSelectionStart();
                    final int endSelection = editText.getSelectionEnd();

                    selectedText =
                            editText.getText().toString().substring(startSelection, endSelection);
                }

                final boolean selectedTextNotEmpty = !selectedText.isEmpty();

                final MaterialDialog dialog =
                        new MaterialDialog.Builder(editText.getContext()).title(
                                R.string.editor_title_link)
                                .customView(layout, false)
                                .positiveColorAttr(R.attr.tintColor)
                                .positiveText(R.string.editor_action_link)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog,
                                            @NonNull DialogAction which) {
                                        final EditText urlBox =
                                                (EditText) dialog.findViewById(R.id.url_box);
                                        final EditText textBox =
                                                (EditText) dialog.findViewById(R.id.text_box);
                                        dialog.dismiss();

                                        final String s = "[".concat(textBox.getText().toString())
                                                .concat("](")
                                                .concat(urlBox.getText().toString())
                                                .concat(")");

                                        int start = Math.max(editText.getSelectionStart(), 0);
                                        int end = Math.max(editText.getSelectionEnd(), 0);

                                        editText.getText().insert(Math.max(start, end), s);

                                        //delete the selected text to avoid duplication
                                        if (selectedTextNotEmpty) {
                                            editText.getText().delete(start, end);
                                        }
                                    }
                                })
                                .build();

                //Tint the hint text if the base theme is Sepia
                if (SettingValues.currentTheme == 5) {
                    ((EditText) dialog.findViewById(R.id.url_box)).setHintTextColor(
                            ContextCompat.getColor(dialog.getContext(), R.color.md_grey_600));
                    ((EditText) dialog.findViewById(R.id.text_box)).setHintTextColor(
                            ContextCompat.getColor(dialog.getContext(), R.color.md_grey_600));
                }

                //use the selected text as the text for the link
                if (!selectedText.isEmpty()) {
                    ((EditText) dialog.findViewById(R.id.text_box)).setText(selectedText);
                }

                dialog.show();
            }
        });

        try {
            ((ImageInsertEditText) editText).setImageSelectedCallback(
                    new ImageInsertEditText.ImageSelectedCallback() {
                        @Override
                        public void onImageSelected(final Uri content, String mimeType) {
                            e = editText.getText();

                            sStart = editText.getSelectionStart();
                            sEnd = editText.getSelectionEnd();
                            handleImageIntent(new ArrayList<Uri>() {{
                                add(content);
                            }}, editText, a);
                        }
                    });
        } catch (Exception e) {
            //if thrown, there is likely an issue implementing this on the user's version of Android. There shouldn't be an issue, but just in case
        }
    }

    public static Editable e;
    public static int      sStart, sEnd;

    public static void doDraw(final Activity a, final EditText editText, final FragmentManager fm) {
        final Intent intent = new Intent(a, Draw.class);
        InputMethodManager imm = (InputMethodManager) editText.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        e = editText.getText();
        TedBottomPicker tedBottomPicker =
                new TedBottomPicker.Builder(editText.getContext()).setOnImageSelectedListener(
                        new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(List<Uri> uri) {
                                Draw.uri = uri.get(0);
                                Fragment auxiliary = new AuxiliaryFragment();

                                sStart = editText.getSelectionStart();
                                sEnd = editText.getSelectionEnd();

                                fm.beginTransaction().add(auxiliary, "IMAGE_UPLOAD").commit();
                                fm.executePendingTransactions();

                                auxiliary.startActivityForResult(intent, 3333);
                            }
                        })
                        .setLayoutResource(R.layout.image_sheet_dialog)
                        .setTitle("Choose a photo")
                        .create();

        tedBottomPicker.show(fm);
    }

    public static class AuxiliaryFragment extends Fragment {
        @Override
        public void onActivityResult(int requestCode, int resultCode, final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(data != null && data.getData() != null) {
                handleImageIntent(new ArrayList<Uri>() {{
                    add(data.getData());
                }}, e, getContext());

                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            }

        }
    }

    public static String getImageLink(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100,
                baos); // Not sure whether this should be jpeg or png, try both and see which works best
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static void insertBefore(String wrapText, EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        editText.getText().insert(Math.min(start, end), wrapText);
    }

    /* not using this method anywhere ¯\_(ツ)_/¯ */
//    public static void wrapNewline(String wrapText, EditText editText) {
//        int start = Math.max(editText.getSelectionStart(), 0);
//        int end = Math.max(editText.getSelectionEnd(), 0);
//        String s = editText.getText().toString().substring(Math.min(start, end), Math.max(start, end));
//        s = s.replace("\n", "\n" + wrapText);
//        editText.getText().replace(Math.min(start, end), Math.max(start, end), s);
//    }

    /**
     * Wrap selected text in one or multiple characters, handling newlines and spaces properly for markdown
     * @param wrapText Character(s) to wrap the selected text in
     * @param editText EditText
     */
    public static void wrapString(String wrapText, EditText editText) {
        wrapString(wrapText, wrapText, editText);
    }

    /**
     * Wrap selected text in one or multiple characters, handling newlines, spaces, >s properly for markdown,
     * with different start and end text.
     * @param startWrap Character(s) to start wrapping with
     * @param endWrap Character(s) to close wrapping with
     * @param editText EditText
     */
    public static void wrapString(String startWrap, String endWrap, EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        String selected = editText.getText().toString().substring(Math.min(start, end), Math.max(start, end));
        // insert the wrapping character inside any selected spaces and >s because they stop markdown formatting
        // we use replaceFirst because anchors (\A, \Z) aren't consumed
        selected = selected.replaceFirst("\\A[\\n> ]*", "$0" + startWrap)
                           .replaceFirst("[\\n> ]*\\Z", endWrap + "$0");
        // 2+ newlines stop formatting, so we do the formatting on each instance of text surrounded by 2+ newlines
        /* in case anyone needs to understand this in the future:
         * ([^\n> ]) captures any character that isn't a newline, >, or space
         * (\n[> ]*){2,} captures any number of two or more newlines with any combination of spaces or >s since markdown ignores those by themselves
         * (?=[^\n> ]) performs a lookahead and ensures there's a character that isn't a newline, >, or space
         */
        selected = selected.replaceAll("([^\\n> ])(\\n[> ]*){2,}(?=[^\\n> ])", "$1" + endWrap + "$2" + startWrap);
        editText.getText().replace(start, end, selected);
    }

    private static void setViews(String rawHTML, String subredditName,
            SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subredditName);
            firstTextView.setLinkTextColor(
                    new ColorPreferences(firstTextView.getContext()).getColor(subredditName));
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subredditName);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    private static class UploadImgur extends AsyncTask<Uri, Integer, JSONObject> {

        final         Context        c;
        private final MaterialDialog dialog;
        public        Bitmap         b;

        public UploadImgur(Context c) {
            this.c = c;
            dialog = new MaterialDialog.Builder(c).title(
                    c.getString(R.string.editor_uploading_image))
                    .progress(false, 100)
                    .cancelable(false)
                    .show();
        }

        //Following methods sourced from https://github.com/Kennyc1012/Opengur, Code by Kenny Campagna
        public static File createFile(Uri uri, @NonNull Context context) {
            InputStream in;
            ContentResolver resolver = context.getContentResolver();
            String type = resolver.getType(uri);
            String extension;

            if ("image/png".equals(type)) {
                extension = ".gif";
            } else if ("image/png".equals(type)) {
                extension = ".png";
            } else {
                extension = ".jpg";
            }

            try {
                in = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                return null;
            }

            // Create files from a uri in our cache directory so they eventually get deleted
            String timeStamp = String.valueOf(System.currentTimeMillis());
            File cacheDir = ((Reddit) context.getApplicationContext()).getImageLoader()
                    .getDiskCache()
                    .getDirectory();
            File tempFile = new File(cacheDir, timeStamp + extension);

            if (writeInputStreamToFile(in, tempFile)) {
                return tempFile;
            } else {
                // If writeInputStreamToFile fails, delete the excess file
                tempFile.delete();
            }

            return null;
        }

        public static boolean writeInputStreamToFile(@NonNull InputStream in, @NonNull File file) {
            BufferedOutputStream buffer = null;
            boolean didFinish = false;

            try {
                buffer = new BufferedOutputStream(new FileOutputStream(file));
                byte[] byt = new byte[1024];
                int i;

                for (long l = 0L; (i = in.read(byt)) != -1; l += i) {
                    buffer.write(byt, 0, i);
                }

                buffer.flush();
                didFinish = true;
            } catch (IOException e) {
                didFinish = false;
            } finally {
                closeStream(in);
                closeStream(buffer);
            }

            return didFinish;
        }

        public static void closeStream(@Nullable Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ex) {
                }
            }
        }

        //End methods sourced from Opengur

        @Override
        protected JSONObject doInBackground(Uri... sub) {
            File bitmap = createFile(sub[0], c);

            final OkHttpClient client = Reddit.client;

            try {
                RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("image", bitmap.getName(),
                                RequestBody.create(MediaType.parse("image/*"), bitmap))
                        .build();

                ProgressRequestBody body =
                        new ProgressRequestBody(formBody, new ProgressRequestBody.Listener() {
                            @Override
                            public void onProgress(int progress) {
                                publishProgress(progress);
                            }
                        });


                Request request = new Request.Builder().header("Authorization",
                        "Client-ID bef87913eb202e9")
                        .url("https://api.imgur.com/3/image")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return new JSONObject(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            dialog.dismiss();
            try {
                int[] attrs = {R.attr.fontColor};
                TypedArray ta =
                        c.obtainStyledAttributes(new ColorPreferences(c).getFontStyle().getBaseId(),
                                attrs);
                final String url = result.getJSONObject("data").getString("link");
                LinearLayout layout = new LinearLayout(c);
                layout.setOrientation(LinearLayout.VERTICAL);

                final TextView titleBox = new TextView(c);
                titleBox.setText(url);
                layout.addView(titleBox);
                titleBox.setEnabled(false);
                titleBox.setTextColor(ta.getColor(0, Color.WHITE));

                final EditText descriptionBox = new EditText(c);
                descriptionBox.setHint(R.string.editor_title);
                descriptionBox.setEnabled(true);
                descriptionBox.setTextColor(ta.getColor(0, Color.WHITE));
                final InputMethodManager imm =
                        (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);

                if (DoEditorActions.e != null) {
                    descriptionBox.setText(DoEditorActions.e.toString().substring(sStart, sEnd));
                }

                ta.recycle();
                int sixteen = Reddit.dpToPxVertical(16);
                layout.setPadding(sixteen, sixteen, sixteen, sixteen);
                layout.addView(descriptionBox);
                new MaterialDialog.Builder(c).title(R.string.editor_title_link)
                        .customView(layout, false)
                        .positiveText(R.string.editor_action_link)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                String s = "["
                                        + descriptionBox.getText().toString()
                                        + "]("
                                        + url
                                        + ")";
                                if (descriptionBox.getText().toString().trim().isEmpty()) {
                                    s = url + " ";
                                }
                                int start = Math.max(sStart, 0);
                                int end = Math.max(sEnd, 0);
                                if (DoEditorActions.e != null) {
                                    DoEditorActions.e.insert(Math.max(start, end), s);
                                    DoEditorActions.e.delete(start, end);
                                    DoEditorActions.e = null;
                                }
                                sStart = 0;
                                sEnd = 0;
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();

            } catch (Exception e) {
                new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                        .setMessage(R.string.editor_err_msg)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
            LogUtil.v("Progress:" + values[0]);
        }
    }

    private static class UploadImgurAlbum extends AsyncTask<Uri, Integer, String> {

        final         Context        c;
        private final MaterialDialog dialog;
        public        Bitmap         b;

        public UploadImgurAlbum(Context c) {
            this.c = c;
            dialog = new MaterialDialog.Builder(c).title(
                    c.getString(R.string.editor_uploading_image))
                    .progress(false, 100)
                    .cancelable(false)
                    .show();
        }

        //Following methods sourced from https://github.com/Kennyc1012/Opengur, Code by Kenny Campagna
        public static File createFile(Uri uri, @NonNull Context context) {
            InputStream in;
            ContentResolver resolver = context.getContentResolver();
            String type = resolver.getType(uri);
            String extension;

            if ("image/png".equals(type)) {
                extension = ".gif";
            } else if ("image/png".equals(type)) {
                extension = ".png";
            } else {
                extension = ".jpg";
            }

            try {
                in = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                return null;
            }

            // Create files from a uri in our cache directory so they eventually get deleted
            String timeStamp = String.valueOf(System.currentTimeMillis());
            File cacheDir = ((Reddit) context.getApplicationContext()).getImageLoader()
                    .getDiskCache()
                    .getDirectory();
            File tempFile = new File(cacheDir, timeStamp + extension);

            if (writeInputStreamToFile(in, tempFile)) {
                return tempFile;
            } else {
                // If writeInputStreamToFile fails, delete the excess file
                tempFile.delete();
            }

            return null;
        }

        public static boolean writeInputStreamToFile(@NonNull InputStream in, @NonNull File file) {
            BufferedOutputStream buffer = null;
            boolean didFinish = false;

            try {
                buffer = new BufferedOutputStream(new FileOutputStream(file));
                byte[] byt = new byte[1024];
                int i;

                for (long l = 0L; (i = in.read(byt)) != -1; l += i) {
                    buffer.write(byt, 0, i);
                }

                buffer.flush();
                didFinish = true;
            } catch (IOException e) {
                didFinish = false;
            } finally {
                closeStream(in);
                closeStream(buffer);
            }

            return didFinish;
        }

        public static void closeStream(@Nullable Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ex) {
                }
            }
        }

        //End methods sourced from Opengur

        String finalUrl;

        @Override
        protected String doInBackground(Uri... sub) {
            totalCount = sub.length;
            final OkHttpClient client = Reddit.client;

            String albumurl;
            {
                Request request = new Request.Builder().header("Authorization",
                        "Client-ID bef87913eb202e9")
                        .url("https://api.imgur.com/3/album")
                        .post(new RequestBody() {
                            @Override
                            public MediaType contentType() {
                                return null;
                            }

                            @Override
                            public void writeTo(BufferedSink sink) {

                            }
                        })
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    JSONObject album = new JSONObject(response.body().string());
                    albumurl = album.getJSONObject("data").getString("deletehash");
                    finalUrl = "http://imgur.com/a/" + album.getJSONObject("data").getString("id");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            }


            try {
                MultipartBody.Builder formBodyBuilder =
                        new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (Uri uri : sub) {
                    File bitmap = createFile(uri, c);
                    formBodyBuilder.addFormDataPart("image", bitmap.getName(),
                            RequestBody.create(MediaType.parse("image/*"), bitmap));
                    formBodyBuilder.addFormDataPart("album", albumurl);
                    MultipartBody formBody = formBodyBuilder.build();

                    ProgressRequestBody body =
                            new ProgressRequestBody(formBody, new ProgressRequestBody.Listener() {
                                @Override
                                public void onProgress(int progress) {
                                    publishProgress(progress);
                                }
                            });


                    Request request = new Request.Builder().header("Authorization",
                            "Client-ID bef87913eb202e9")
                            .url("https://api.imgur.com/3/image")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final String result) {
            dialog.dismiss();
            try {
                int[] attrs = {R.attr.fontColor};
                TypedArray ta =
                        c.obtainStyledAttributes(new ColorPreferences(c).getFontStyle().getBaseId(),
                                attrs);
                LinearLayout layout = new LinearLayout(c);
                layout.setOrientation(LinearLayout.VERTICAL);

                final TextView titleBox = new TextView(c);
                titleBox.setText(finalUrl);
                layout.addView(titleBox);
                titleBox.setEnabled(false);
                titleBox.setTextColor(ta.getColor(0, Color.WHITE));

                final EditText descriptionBox = new EditText(c);
                descriptionBox.setHint(R.string.editor_title);
                descriptionBox.setEnabled(true);
                descriptionBox.setTextColor(ta.getColor(0, Color.WHITE));

                if (DoEditorActions.e != null) {
                    descriptionBox.setText(DoEditorActions.e.toString().substring(sStart, sEnd));
                }

                ta.recycle();
                int sixteen = Reddit.dpToPxVertical(16);
                layout.setPadding(sixteen, sixteen, sixteen, sixteen);
                layout.addView(descriptionBox);
                new MaterialDialog.Builder(c).title(R.string.editor_title_link)
                        .customView(layout, false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                dialog.dismiss();
                                String s = "["
                                        + descriptionBox.getText().toString()
                                        + "]("
                                        + finalUrl
                                        + ")";
                                int start = Math.max(sStart, 0);
                                int end = Math.max(sEnd, 0);
                                DoEditorActions.e.insert(Math.max(start, end), s);
                                DoEditorActions.e.delete(start, end);
                                DoEditorActions.e = null;
                                sStart = 0;
                                sEnd = 0;
                            }
                        })
                        .positiveText(R.string.editor_action_link)
                        .canceledOnTouchOutside(false)
                        .show();

            } catch (Exception e) {
                new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                        .setMessage(R.string.editor_err_msg)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                e.printStackTrace();
            }
        }

        int uploadCount;
        int totalCount;

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            if (progress < dialog.getCurrentProgress() || uploadCount == 0) {
                uploadCount += 1;
            }
            dialog.setContent("Image " + uploadCount + "/" + totalCount);
            dialog.setProgress(progress);
        }
    }

    public static void handleImageIntent(List<Uri> uris, EditText ed, Context c) {
        handleImageIntent(uris, ed.getText(), c);
    }

    public static void handleImageIntent(List<Uri> uris, Editable ed, Context c) {
        if (uris.size() == 1) {
            // Get the Image from data (single image)
            try {
                new UploadImgur(c).execute(uris.get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Multiple images
            try {
                new UploadImgurAlbum(c).execute(uris.toArray(new Uri[0]));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }


    public static class ProgressRequestBody extends RequestBody {

        protected RequestBody  mDelegate;
        protected Listener     mListener;
        protected CountingSink mCountingSink;

        public ProgressRequestBody(RequestBody delegate, Listener listener) {
            mDelegate = delegate;
            mListener = listener;
        }

        @Override
        public MediaType contentType() {
            return mDelegate.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return mDelegate.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            mCountingSink = new CountingSink(sink);
            BufferedSink bufferedSink = Okio.buffer(mCountingSink);
            mDelegate.writeTo(bufferedSink);
            bufferedSink.flush();
        }

        protected final class CountingSink extends ForwardingSink {
            private long bytesWritten = 0;

            public CountingSink(Sink delegate) {
                super(delegate);
            }

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                bytesWritten += byteCount;
                mListener.onProgress((int) (100F * bytesWritten / contentLength()));
            }
        }

        public interface Listener {
            void onProgress(int progress);
        }
    }

}
