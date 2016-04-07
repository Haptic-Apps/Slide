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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.github.rjeschke.txtmark.Processor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;

/**
 * Created by carlo_000 on 10/18/2015.
 */
public class DoEditorActions {
    /*
                android:id="@+id/imagerep"

                android:id="@+id/link"

             */
    public static void doActions(final EditText editText, final View baseView, final FragmentManager fm, final Activity a) {
        baseView.findViewById(R.id.bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrapString("**", editText);
            }
        });

        baseView.findViewById(R.id.italics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrapString("*", editText);
            }
        });

        baseView.findViewById(R.id.italics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrapString("*", editText);
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
                Fragment auxiliary = new Fragment() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                        super.onActivityResult(requestCode, resultCode, data);

                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            Log.v(LogUtil.getTag(), "WORKED! " + selectedImageUri.toString());
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(editText.getContext().getContentResolver(), selectedImageUri);
                                new UploadImgur(editText).execute(bitmap);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            fm.beginTransaction().remove(this).commit();
                        }
                    }
                };
                fm.beginTransaction().add(auxiliary, "IMAGE_CHOOSER").commit();
                fm.executePendingTransactions();

                if(a instanceof MainActivity){
                    ((MainActivity)a).doImage = new Runnable() {
                        @Override
                        public void run() {
                            if (((MainActivity)a).data != null) {
                                Uri selectedImageUri = ((MainActivity)a).data.getData();
                                Log.v(LogUtil.getTag(), "WORKED! " + selectedImageUri.toString());
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(editText.getContext().getContentResolver(), selectedImageUri);
                                    new UploadImgur(editText).execute(bitmap);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                }

                auxiliary.startActivityForResult(Intent.createChooser(intent, Integer.toString(R.string.editor_select_img)), 3333);
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
                insertBefore(">", editText);
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
                String html =  Processor.process(editText.getText().toString());
                LayoutInflater inflater = (a).getLayoutInflater();
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
                LinearLayout layout = new LinearLayout(editText.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                int[] attrs = {R.attr.font};

                TypedArray ta = baseView.getContext().obtainStyledAttributes(new ColorPreferences(baseView.getContext()).getFontStyle().getBaseId(), attrs);

                final EditText titleBox = new EditText(editText.getContext());
                titleBox.setHint(R.string.editor_url);
                titleBox.setTextColor(ta.getColor(0, Color.WHITE));
                layout.addView(titleBox);

                final EditText descriptionBox = new EditText(editText.getContext());
                descriptionBox.setHint(R.string.editor_text);
                descriptionBox.setTextColor(ta.getColor(0, Color.WHITE));
                layout.addView(descriptionBox);
                layout.setPadding(16, 16, 16, 16);

                ta.recycle();
                new AlertDialogWrapper.Builder(editText.getContext())
                        .setTitle(R.string.editor_title_link)
                        .setView(layout)
                        .setPositiveButton(R.string.editor_action_link,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        String s = "[" + descriptionBox.getText().toString() + "](" + titleBox.getText().toString() + ")";
                                        int start = Math.max(editText.getSelectionStart(), 0);
                                        int end = Math.max(editText.getSelectionEnd(), 0);
                                        editText.getText().insert(Math.max(start, end), s);
                                    }
                                }).show();

            }
        });
        baseView.findViewById(R.id.size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBefore("#", editText);
            }
        });
    }

    public static void wrapString(String wrapText, EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        editText.getText().insert(Math.min(start, end), wrapText);
        editText.getText().insert(Math.max(start, end) + wrapText.length(), wrapText);

    }

    public static void wrapNewline(String wrapText, EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        String s = editText.getText().toString().substring(Math.min(start, end), Math.max(start, end));
        s = s.replace("\n", "\n" + wrapText);
        editText.getText().replace(Math.min(start, end), Math.max(start, end), s);
    }

    public static void insertBefore(String wrapText, EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        editText.getText().insert(Math.min(start, end), wrapText);

    }

    public static String getImageLink(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, baos); // Not sure whether this should be jpeg or png, try both and see which works best

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private static class UploadImgur extends AsyncTask<Bitmap, Void, JSONObject> {

        final Context c;
        final EditText editText;
        private final ProgressDialog dialog;
        public Bitmap b;
        public UploadImgur(EditText editText) {
            this.c = editText.getContext();
            this.editText = editText;
            dialog = new ProgressDialog(c);
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
                titleBox.setTextColor(ta.getColor(0, Color.WHITE));


                final EditText descriptionBox = new EditText(editText.getContext());
                descriptionBox.setHint(R.string.editor_title);
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
            b = bitmap;

// Creates Byte Array from picture
            URL url;
            try {

                url = new URL("https://imgur-apiv3.p.mashape.com/3/image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                String data = URLEncoder.encode("image", "UTF-8") + "="
                        + URLEncoder.encode(getImageLink(bitmap), "UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("X-Mashape-Key", SecretConstants.getImgurApiKey(c));
                conn.addRequestProperty("Authorization", "Client-ID " + "bef87913eb202e9");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                conn.connect();
                StringBuilder stb = new StringBuilder();
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the response
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    stb.append(line).append("\n");
                }
                wr.close();
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
