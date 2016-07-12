package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;

/**
 * Created by carlo_000 on 10/18/2015.
 */
public class DoEditorActions {

    public static void doActions(final EditText editText, final View baseView, final FragmentManager fm, final Activity a) {
        baseView.findViewById(R.id.bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.hasSelection()) {
                    wrapString("**", editText); //If the user has text selected, wrap that text in the symbols
                } else {
                    //If the user doesn't have text selected, put the symbols around the cursor's position
                    int pos = editText.getSelectionStart();
                    editText.getText().insert(pos, "**");
                    editText.getText().insert(pos + 1, "**");
                    editText.setSelection(pos + 2); //put the cursor between the symbols
                }
            }
        });
        baseView.findViewById(R.id.italics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.hasSelection()) {
                    wrapString("*", editText); //If the user has text selected, wrap that text in the symbols
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
                    wrapString("~~", editText); //If the user has text selected, wrap that text in the symbols
                } else {
                    //If the user doesn't have text selected, put the symbols around the cursor's position
                    int pos = editText.getSelectionStart();
                    editText.getText().insert(pos, "~~");
                    editText.getText().insert(pos + 1, "~~");
                    editText.setSelection(pos + 2); //put the cursor between the symbols
                }
            }
        });
        baseView.findViewById(R.id.savedraft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.v("Saving draft");
                Drafts.addDraft(editText.getText().toString());
                Snackbar.make(baseView.findViewById(R.id.savedraft), "Draft saved", Snackbar.LENGTH_SHORT).show();
            }
        });
        baseView.findViewById(R.id.draft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> drafts = Drafts.getDrafts();
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
                    new AlertDialogWrapper.Builder(a)
                            .setTitle(R.string.choose_draft)
                            .setItems(draftText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editText.setText(editText.getText().toString() + draftText[which]);
                                }
                            })
                            .setNeutralButton(R.string.btn_cancel, null)
                            .setPositiveButton(R.string.manage_drafts, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final boolean[] selected = new boolean[drafts.size()];
                                    new AlertDialogWrapper.Builder(a)
                                            .setTitle(R.string.choose_draft)
                                            .setNeutralButton(R.string.btn_cancel, null)
                                            .alwaysCallMultiChoiceCallback()
                                            .setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new AlertDialogWrapper.Builder(a).setTitle(R.string.really_delete_drafts)
                                                            .setCancelable(false)
                                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    ArrayList<String> draf = new ArrayList<>();
                                                                    for (int i = 0; i < draftText.length; i++) {
                                                                        if (!selected[i]) {
                                                                            draf.add(draftText[i]);
                                                                        }
                                                                    }
                                                                    Drafts.save(draf);
                                                                }
                                                            }).setNegativeButton(R.string.btn_no, null)
                                                            .show();
                                                }
                                            })
                                            .setMultiChoiceItems(draftText, selected, new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                    selected[which] = isChecked;
                                                }
                                            }).show();
                                }
                            }).show();
                }
            }
        });
       /*todo baseView.findViewById(R.id.strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrapString("~~", editText);
            }
        });*/
        baseView.findViewById(R.id.imagerep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (a instanceof MainActivity) {
                    LogUtil.v("Running on main");
                    ((MainActivity) a).doImage = new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.v("Running");
                            if (((MainActivity) a).data != null) {
                                Uri selectedImageUri = ((MainActivity) a).data.getData();
                                Log.v(LogUtil.getTag(), "WORKED! " + selectedImageUri.toString());
                                try {
                                    File f = new File(selectedImageUri.getPath());
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(editText.getContext().getContentResolver(), selectedImageUri);
                                    new UploadImgur(editText, f != null && f.getName().contains(".jpg") || f.getName().contains(".jpeg")).execute(bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    a.startActivityForResult(Intent.createChooser(intent, Integer.toString(R.string.editor_select_img)), 3333);
                } else {
                    Fragment auxiliary = new Fragment() {
                        @Override
                        public void onActivityResult(int requestCode, int resultCode, Intent data) {
                            super.onActivityResult(requestCode, resultCode, data);

                            if (data != null) {
                                Uri selectedImageUri = data.getData();
                                Log.v(LogUtil.getTag(), "WORKED! " + selectedImageUri.toString());
                                try {
                                    File f = new File(selectedImageUri.getPath());
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(editText.getContext().getContentResolver(), selectedImageUri);
                                    new UploadImgur(editText, f != null && f.getName().contains(".jpg") || f.getName().contains(".jpeg")).execute(bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                fm.beginTransaction().remove(this).commit();
                            }
                        }
                    };
                    fm.beginTransaction().add(auxiliary, "IMAGE_CHOOSER").commit();
                    fm.executePendingTransactions();

                    auxiliary.startActivityForResult(Intent.createChooser(intent, Integer.toString(R.string.editor_select_img)), 3333);
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

                insertBefore("> ", editText);
            }
        });
        baseView.findViewById(R.id.bulletlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBefore("* ", editText);
            }
        });
        baseView.findViewById(R.id.numlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBefore("1. ", editText);
            }
        });
        baseView.findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Extension> extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create());
                Parser parser = Parser.builder().extensions(extensions).build();
                HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
                Node document = parser.parse(editText.getText().toString());
                String html = renderer.render(document);
                LayoutInflater inflater = a.getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.parent_comment_dialog, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(a);
                setViews(html, "NO sub", (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.firstTextView), (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow));
                builder.setView(dialoglayout);
                builder.show();
            }
        });
        baseView.findViewById(R.id.link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = LayoutInflater.from(a);
                final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.insert_link, null);

                int[] attrs = {R.attr.font};

                TypedArray ta = baseView.getContext().obtainStyledAttributes(new ColorPreferences(baseView.getContext()).getFontStyle().getBaseId(), attrs);
                ta.recycle();

                final MaterialDialog dialog = new MaterialDialog.Builder(editText.getContext())
                        .title(R.string.editor_title_link)
                        .customView(layout, false)
                        .positiveColorAttr(R.attr.tint)
                        .positiveText(R.string.editor_action_link)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                final EditText titleBox = (EditText) dialog.findViewById(R.id.title_box);
                                final EditText descriptionBox = (EditText) dialog.findViewById(R.id.description_box);
                                dialog.dismiss();

                                String s = "[" + descriptionBox.getText().toString() + "](" + titleBox.getText().toString() + ")";
                                int start = Math.max(editText.getSelectionStart(), 0);
                                int end = Math.max(editText.getSelectionEnd(), 0);
                                editText.getText().insert(Math.max(start, end), s);
                            }
                        }).build();

                //Tint the hint text if the base theme is Sepia
                if (SettingValues.currentTheme == 5) {
                    ((EditText) dialog.findViewById(R.id.title_box))
                            .setHintTextColor(ContextCompat.getColor(dialog.getContext(), R.color.md_grey_600));
                    ((EditText) dialog.findViewById(R.id.description_box))
                            .setHintTextColor(ContextCompat.getColor(dialog.getContext(), R.color.md_grey_600));
                }
                dialog.show();
            }
        });
    }

    public static void wrapString(String wrapText, EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        editText.getText().insert(Math.min(start, end), wrapText);
        editText.getText().insert(Math.max(start, end) + wrapText.length(), wrapText);
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

    public static String getImageLink(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Not sure whether this should be jpeg or png, try both and see which works best
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private static class UploadImgur extends AsyncTask<Bitmap, Void, JSONObject> {

        final Context c;
        final EditText editText;
        private final ProgressDialog dialog;
        public Bitmap b;
        boolean jpg;

        public UploadImgur(EditText editText, boolean jpg) {
            this.c = editText.getContext();
            this.editText = editText;
            dialog = new ProgressDialog(c);
            this.jpg = jpg;
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            dialog.dismiss();
            try {
                int[] attrs = {R.attr.font};
                TypedArray ta = editText.getContext().obtainStyledAttributes(new ColorPreferences(editText.getContext()).getFontStyle().getBaseId(), attrs);
                final String url = result.getJSONObject("data").getString("link");
                LinearLayout layout = new LinearLayout(editText.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                final TextView titleBox = new TextView(editText.getContext());
                titleBox.setText(url);
                layout.addView(titleBox);
                titleBox.setEnabled(false);
                titleBox.setTextColor(ta.getColor(0, Color.WHITE));

                final EditText descriptionBox = new EditText(editText.getContext());
                descriptionBox.setHint(R.string.editor_title);
                descriptionBox.setEnabled(true);
                descriptionBox.setTextColor(ta.getColor(0, Color.WHITE));


                ta.recycle();
                layout.setPadding(16, 16, 16, 16);
                layout.addView(descriptionBox);
                new AlertDialogWrapper.Builder(editText.getContext()).setTitle(R.string.editor_title_link)
                        .setView(layout).setPositiveButton(R.string.editor_action_link, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String s = "[" + descriptionBox.getText().toString() + "](" + url + ")";
                        int start = Math.max(editText.getSelectionStart(), 0);
                        int end = Math.max(editText.getSelectionEnd(), 0);
                        editText.getText().insert(Math.max(start, end), s);
                    }
                }).show();

            } catch (Exception e) {
                new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title).setMessage(R.string.editor_err_msg).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(c.getString(R.string.editor_uploading_image));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Bitmap... sub) {
            Bitmap bitmap = sub[0];
            URL url;

            // Creates Byte Array from picture
            try {
                url = new URL("https://imgur-apiv3.p.mashape.com/3/image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("X-Mashape-Key", SecretConstants.getImgurApiKey(c));
                conn.addRequestProperty("Authorization", "Client-ID " + "bef87913eb202e9");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                conn.connect();
                OutputStream output = conn.getOutputStream();
                if (jpg)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                else
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

                output.close();

                StringBuilder stb = new StringBuilder();

                // Get the response
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    stb.append(line).append("\n");
                }
                rd.close();
                return new JSONObject(stb.toString());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subredditName);
            firstTextView.setLinkTextColor(new ColorPreferences(firstTextView.getContext()).getColor(subredditName));
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
}
