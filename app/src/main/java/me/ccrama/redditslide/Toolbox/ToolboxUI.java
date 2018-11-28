package me.ccrama.redditslide.Toolbox;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Misc UI stuff for toolbox - usernote display, removal display, etc.
 */
public class ToolboxUI {

    /**
     * Shows a user's usernotes in a dialog
     *
     * @param context   context
     * @param author    user to show usernotes for
     * @param subreddit subreddit to get usernotes from
     */
    public static void showUsernotes(final Context context, String author, String subreddit) {
        final UsernoteListAdapter adapter = new UsernoteListAdapter(context, subreddit, author);
        new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.mod_usernotes_title, author))
                .adapter(adapter, null)
                .neutralText(R.string.mod_usernotes_add)
                //.onNeutral(...) // TODO: add usernotes
                .positiveText(R.string.btn_close)
                .show();
    }

    public static class UsernoteListAdapter extends ArrayAdapter<UsernoteListItem> {
        public UsernoteListAdapter(@NonNull Context context, String subreddit, String user) {
            super(context, R.layout.usernote_list_item, R.id.usernote_note_text);

            final Usernotes usernotes = Toolbox.getUsernotesForSubreddit(subreddit);

            if (usernotes != null && usernotes.getNotesForUser(user) != null) {
                for (Usernote note : usernotes.getNotesForUser(user)) {
                    String dateString = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
                            .format(new Date(note.getTime()));

                    SpannableStringBuilder noteText = new SpannableStringBuilder(
                            usernotes.getModNameFromModIndex(note.getMod()) + "\n" + dateString);
                    noteText.setSpan(new RelativeSizeSpan(.92f), noteText.length() - dateString.length(),
                            noteText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    SpannableStringBuilder warningText = new SpannableStringBuilder(
                            usernotes.getWarningTextFromWarningIndex(note.getWarning(), true));
                    warningText.setSpan(new ForegroundColorSpan(
                                    Color.parseColor(usernotes.getColorFromWarningIndex(note.getWarning()))),
                            0, warningText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (warningText.length() > 0) {
                        warningText.append(" ");
                    }
                    warningText.append(note.getNoteText());

                    String link = note.getLinkAsURL(subreddit);

                    this.add(new UsernoteListItem(noteText, warningText, link));
                }
            }
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final UsernoteListItem item = getItem(position);

            TextView authorDatetime = view.findViewById(R.id.usernote_author_datetime);
            authorDatetime.setText(item.getAuthorDatetime());

            TextView noteText = view.findViewById(R.id.usernote_note_text);
            noteText.setText(item.getNoteText());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.getLink() != null) {
                        OpenRedditLink.openUrl(view.getContext(), item.getLink(), true);
                    }
                }
            });

            return view;
        }
    }

    public static class UsernoteListItem {
        private CharSequence authorDatetime;
        private CharSequence noteText;
        private String link;

        public UsernoteListItem(CharSequence authorDatetime, CharSequence noteText, String link) {
            this.authorDatetime = authorDatetime;
            this.noteText = noteText;
            this.link = link;
        }

        public CharSequence getAuthorDatetime() {
            return authorDatetime;
        }

        public CharSequence getNoteText() {
            return noteText;
        }

        public String getLink() {
            return link;
        }
    }
}
