package me.ccrama.redditslide.Toolbox;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.InvalidScopeException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Activities.Reauthenticate;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;

/**
 * Misc UI stuff for toolbox - usernote display, removal display, etc.
 */
public class ToolboxUI {

    /**
     * Shows a removal reason dialog
     *
     * @param context Context
     * @param thing   Submission or Comment being removed
     */
    public static void showRemoval(final Context context, final PublicContribution thing,
            final CompletedRemovalCallback callback) {
        final RemovalReasons removalReasons;
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        // Set the dialog title
        if (thing instanceof Comment) {
            builder.title(context.getResources().getString(R.string.toolbox_removal_title,
                    ((Comment) thing).getSubredditName()));
            removalReasons = Toolbox.getConfig(((Comment) thing).getSubredditName()).getRemovalReasons();
        } else if (thing instanceof Submission) {
            builder.title(context.getResources().getString(R.string.toolbox_removal_title,
                    ((Submission) thing).getSubredditName()));
            removalReasons = Toolbox.getConfig(((Submission) thing).getSubredditName()).getRemovalReasons();
        } else {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogContent = inflater.inflate(R.layout.toolbox_removal_dialog, null);

        final CheckBox headerToggle = dialogContent.findViewById(R.id.toolbox_header_toggle);
        final TextView headerText = dialogContent.findViewById(R.id.toolbox_header_text);
        final LinearLayout reasonsList = dialogContent.findViewById(R.id.toolbox_reasons_list);
        final CheckBox footerToggle = dialogContent.findViewById(R.id.toolbox_footer_toggle);
        final TextView footerText = dialogContent.findViewById(R.id.toolbox_footer_text);
        final RadioGroup actions = dialogContent.findViewById(R.id.toolbox_action);
        final CheckBox actionSticky = dialogContent.findViewById(R.id.sticky_comment);
        final CheckBox actionModmail = dialogContent.findViewById(R.id.pm_modmail);
        final CheckBox actionLock = dialogContent.findViewById(R.id.lock);
        final EditText logReason = dialogContent.findViewById(R.id.toolbox_log_reason);

        // Check if removal should be logged and set related views
        final boolean log = !removalReasons.getLogSub().isEmpty();
        if (log) {
            dialogContent.findViewById(R.id.none).setVisibility(View.VISIBLE);
            if (removalReasons.getLogTitle().contains("{reason}")) {
                logReason.setVisibility(View.VISIBLE);
                logReason.setText(removalReasons.getLogReason());
            }
        }

        // Hide lock option if removing a comment
        if (thing instanceof Comment) {
            actionLock.setVisibility(View.GONE);
        }

        // Set up the header and footer options
        headerText.setText(replaceTokens(removalReasons.getHeader(), thing));
        if (removalReasons.getHeader().isEmpty()) {
            ((View) headerToggle.getParent()).setVisibility(View.GONE);
        }
        footerText.setText(replaceTokens(removalReasons.getFooter(), thing));
        if (removalReasons.getFooter().isEmpty()) {
            ((View) footerToggle.getParent()).setVisibility(View.GONE);
        }

        // Set up the removal reason list
        for (RemovalReasons.RemovalReason reason : removalReasons.getReasons()) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setMaxLines(2);
            checkBox.setEllipsize(TextUtils.TruncateAt.END);
            final TypedValue tv = new TypedValue();
            final boolean found = context.getTheme().resolveAttribute(R.attr.fontColor, tv, true);
            checkBox.setTextColor(found ? tv.data : Color.WHITE);
            checkBox.setText(reason.getTitle().isEmpty() ? reason.getText() : reason.getTitle());
            reasonsList.addView(checkBox);
        }

        // Set default states of checkboxes/radiobuttons
        if (SettingValues.toolboxMessageType == SettingValues.ToolboxRemovalMessageType.COMMENT.ordinal()) {
            ((RadioButton) actions.findViewById(R.id.comment)).setChecked(true);
        } else if (SettingValues.toolboxMessageType == SettingValues.ToolboxRemovalMessageType.PM.ordinal()) {
            ((RadioButton) actions.findViewById(R.id.pm)).setChecked(true);
        } else if (SettingValues.toolboxMessageType == SettingValues.ToolboxRemovalMessageType.BOTH.ordinal()) {
            ((RadioButton) actions.findViewById(R.id.both)).setChecked(true);
        } else {
            ((RadioButton) actions.findViewById(R.id.none)).setChecked(true);
        }
        actionSticky.setChecked(SettingValues.toolboxSticky);
        actionModmail.setChecked(SettingValues.toolboxModmail);
        actionLock.setChecked(SettingValues.toolboxLock);

        // Set up dialog buttons
        builder.customView(dialogContent, false);
        builder.positiveText(R.string.mod_btn_remove);
        builder.negativeText(R.string.btn_cancel);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                StringBuilder removalString = new StringBuilder();
                StringBuilder flairText = new StringBuilder();
                StringBuilder flairCSS = new StringBuilder();

                // Add the header to the removal message
                if (headerToggle.isChecked()) {
                    removalString.append(removalReasons.getHeader());
                    removalString.append("\n\n");
                }
                // Add the removal reasons
                for (int i = 0; i < reasonsList.getChildCount(); i++) {
                    if (((CheckBox) reasonsList.getChildAt(i)).isChecked()) {
                        removalString.append(removalReasons.getReasons().get(i).getText());
                        removalString.append("\n\n");

                        flairText.append(flairText.length() > 0 ? " " : "");
                        flairText.append(removalReasons.getReasons().get(i).getFlairText());

                        flairCSS.append(flairCSS.length() > 0 ? " " : "");
                        flairCSS.append(removalReasons.getReasons().get(i).getFlairCSS());
                    }
                }
                // Add the footer
                if (footerToggle.isChecked()) {
                    removalString.append(removalReasons.getFooter());
                }
                // Add PM footer
                if (actions.getCheckedRadioButtonId() == R.id.pm || actions.getCheckedRadioButtonId() == R.id.both) {
                    removalString.append("\n\n---\n[[Link to your {kind}]({url})]");
                }
                // Remove the item and send the message if desired
                new AsyncRemoveTask(callback).execute(
                        thing,                                                      // thing
                        actions.getCheckedRadioButtonId(),                          // action ID
                        replaceTokens(removalString.toString(), thing),             // removal reason
                        replaceTokens(removalReasons.getPmSubject(), thing),        // removal PM subject
                        actionModmail.isChecked(),                                  // modmail?
                        actionSticky.isChecked(),                                   // sticky?
                        actionLock.isChecked(),                                     // lock?
                        log,                                                        // log the removal?
                        replaceTokens(removalReasons.getLogTitle(), thing)          // log post title
                                .replace("{reason}", logReason.getText()),
                        removalReasons.getLogSub(),                                 // log sub
                        new String[] { flairText.toString(), flairCSS.toString() }  // flair text and css
                );
            }
        });

        builder.build().show();
    }

    /**
     * Checks if a Toolbox removal dialog can be shown for a subreddit
     *
     * @param subreddit Subreddit
     * @return whether a toolbox removal dialog can be shown
     */
    public static boolean canShowRemoval(String subreddit) {
        return SettingValues.toolboxEnabled
                && Toolbox.getConfig(subreddit) != null
                && Toolbox.getConfig(subreddit).getRemovalReasons() != null;
    }

    /**
     * Replace toolbox tokens with the appropriate replacements
     * Does NOT include log-related tokens, those must be handled after logging.
     *
     * @param reason    String to be parsed
     * @param parameter Item being acted upon
     * @return String with replacements made
     */
    public static String replaceTokens(String reason, PublicContribution parameter) {
        if (parameter instanceof Comment) {
            Comment thing = (Comment) parameter;
            return reason.replace("{subreddit}", thing.getSubredditName())
                    .replace("{author}", thing.getAuthor())
                    .replace("{kind}", "comment")
                    .replace("{mod}", Authentication.name)
                    .replace("{title}", "")
                    .replace("{url}", "https://www.reddit.com"
                            + thing.getDataNode().get("permalink").asText())
                    .replace("{domain}", "")
                    .replace("{link}", "undefined");
        } else if (parameter instanceof Submission) {
            Submission thing = (Submission) parameter;
            return reason.replace("{subreddit}", thing.getSubredditName())
                    .replace("{author}", thing.getAuthor())
                    .replace("{kind}", "submission")
                    .replace("{mod}", Authentication.name)
                    .replace("{title}", thing.getTitle())
                    .replace("{url}", "https://www.reddit.com"
                            + thing.getDataNode().get("permalink").asText())
                    .replace("{domain}", thing.getDomain())
                    .replace("{link}", thing.getUrl());
        } else {
            throw new IllegalArgumentException("Must be passed a submission or comment!");
        }
    }

    /**
     * Shows a user's usernotes in a dialog
     *
     * @param context     context
     * @param author      user to show usernotes for
     * @param subreddit   subreddit to get usernotes from
     * @param currentLink Link, in Toolbox format, for the current item - used for adding usernotes
     */
    public static void showUsernotes(final Context context, String author, String subreddit, String currentLink) {
        final UsernoteListAdapter adapter = new UsernoteListAdapter(context, subreddit, author);
        new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.mod_usernotes_title, author))
                .adapter(adapter, null)
                .neutralText(R.string.mod_usernotes_add)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // set up layout for add note dialog
                        final LinearLayout layout = new LinearLayout(context);
                        final Spinner spinner = new Spinner(context);
                        final EditText noteText = new EditText(context);

                        layout.addView(spinner);
                        layout.addView(noteText);

                        noteText.setHint(R.string.toolbox_note_text_placeholder);

                        layout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        spinner.setLayoutParams(params);
                        noteText.setLayoutParams(params);

                        // create list of types, add default "no type" type
                        List<CharSequence> types = new ArrayList<>();
                        SpannableStringBuilder defaultType = new SpannableStringBuilder(
                                " " + context.getString(R.string.toolbox_note_default) + " ");
                        defaultType.setSpan(new BackgroundColorSpan(Color.parseColor("#808080")),
                                0, defaultType.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        defaultType.setSpan(new ForegroundColorSpan(Color.WHITE), 0, defaultType.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        types.add(defaultType);

                        // add additional types
                        ToolboxConfig config = Toolbox.getConfig(subreddit);

                        final Map<String, Map<String, String>> typeMap;
                        if (config != null
                                && config.getUsernoteTypes() != null
                                && config.getUsernoteTypes().size() > 0) {
                            typeMap = Toolbox.getConfig(subreddit).getUsernoteTypes();
                        } else {
                            typeMap = Toolbox.DEFAULT_USERNOTE_TYPES;
                        }

                        for (Map<String, String> stringStringMap : typeMap.values()) {
                            SpannableStringBuilder typeString =
                                    new SpannableStringBuilder(" [" + stringStringMap.get("text") + "] ");
                            typeString.setSpan(new BackgroundColorSpan(Color.parseColor(stringStringMap.get("color"))),
                                    0, typeString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            typeString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, typeString.length(),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            types.add(typeString);
                        }

                        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item,
                                types));

                        // show add note dialog
                        new MaterialDialog.Builder(context)
                                .customView(layout, true)
                                .autoDismiss(false)
                                .positiveText(R.string.btn_add)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (noteText.getText().length() == 0) {
                                            noteText.setError(context.getString(R.string.toolbox_note_text_required));
                                            return;
                                        }
                                        int selected = spinner.getSelectedItemPosition();
                                        new AsyncAddUsernoteTask(context).execute(
                                                subreddit,
                                                author,
                                                noteText.getText().toString(),
                                                currentLink,
                                                selected - 1 >= 0 ? typeMap.keySet().toArray()[selected - 1].toString()
                                                        : null
                                        );
                                        dialog.dismiss();
                                    }
                                })
                                .negativeText(R.string.btn_cancel)
                                .onNegative((dialog1, which1) -> dialog1.dismiss())
                                .show();
                    }
                })
                .positiveText(R.string.btn_close)
                .show();
    }

    /**
     * Appends a usernote to builder if a usernote in the subreddit is available, and the current
     * user has it enabled.
     *
     * @param context Android context
     * @param builder The builder to append the usernote to
     * @param subreddit The subreddit to look for notes in
     * @param user The user to look for
     */
    public static void appendToolboxNote(Context context, SpannableStringBuilder builder,
            String subreddit, String user) {
        if (!SettingValues.toolboxEnabled || !Authentication.mod) {
            return;
        }

        Usernotes notes = Toolbox.getUsernotes(subreddit);
        if (notes == null) {
            return;
        }

        List<Usernote> notesForUser = notes.getNotesForUser(user);
        if (notesForUser == null || notesForUser.isEmpty()) {
            return;
        }

        SpannableStringBuilder noteBuilder =
                new SpannableStringBuilder("\u00A0" + notes.getDisplayNoteForUser(user) + "\u00A0");

        noteBuilder.setSpan(
                new RoundedBackgroundSpan(context.getResources().getColor(R.color.white),
                        notes.getDisplayColorForUser(user), false, context), 0,
                noteBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" ");
        builder.append(noteBuilder);
    }

    public static class UsernoteListAdapter extends ArrayAdapter<UsernoteListItem> {
        public UsernoteListAdapter(@NonNull Context context, String subreddit, String user) {
            super(context, R.layout.usernote_list_item, R.id.usernote_note_text);

            final Usernotes usernotes = Toolbox.getUsernotes(subreddit);

            if (usernotes != null && usernotes.getNotesForUser(user) != null) {
                for (Usernote note : usernotes.getNotesForUser(user)) {
                    String dateString = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
                            .format(new Date(note.getTime()));

                    SpannableStringBuilder authorDateText = new SpannableStringBuilder(
                            usernotes.getModNameFromModIndex(note.getMod()) + "\n" + dateString);
                    authorDateText.setSpan(new RelativeSizeSpan(.92f), authorDateText.length() - dateString.length(),
                            authorDateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    SpannableStringBuilder noteText = new SpannableStringBuilder(
                            usernotes.getWarningTextFromWarningIndex(note.getWarning(), true));
                    noteText.setSpan(new ForegroundColorSpan(
                                    usernotes.getColorFromWarningIndex(note.getWarning())),
                            0, noteText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (noteText.length() > 0) {
                        noteText.append(" ");
                    }
                    noteText.append(note.getNoteText());

                    String link = note.getLinkAsURL(subreddit);

                    this.add(new UsernoteListItem(authorDateText, noteText, link, note, subreddit, user));
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

            view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AsyncRemoveUsernoteTask(item.getNote(), getContext())
                            .execute(item.getSubreddit(), item.getUser());
                    remove(item);
                }
            });

            return view;
        }
    }

    public static class UsernoteListItem {
        private CharSequence authorDatetime;
        private CharSequence noteText;
        private String link;
        private Usernote note;
        private String subreddit;
        private String user;

        public UsernoteListItem(CharSequence authorDatetime, CharSequence noteText, String link, Usernote note,
                String subreddit, String user) {
            this.authorDatetime = authorDatetime;
            this.noteText = noteText;
            this.link = link;
            this.note = note;
            this.subreddit = subreddit;
            this.user = user;
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

        public Usernote getNote() {
            return note;
        }

        public String getSubreddit() {
            return subreddit;
        }

        public String getUser() {
            return user;
        }
    }

    /**
     * Removes a post/comment, optionally locking first if a post.
     * Parameters are: thing (extends PublicContribution),
     * action ID (int),
     * removal reason (String),
     * removal subject (String),
     * modmail (boolean),
     * sticky (boolean),
     * lock (boolean),
     * log (boolean),
     * logtitle (String),
     * logsub (String)
     * flair (String[] - [text, css])
     */
    public static class AsyncRemoveTask extends AsyncTask<Object, Void, Boolean> {
        CompletedRemovalCallback callback;

        public AsyncRemoveTask(CompletedRemovalCallback callback) {
            this.callback = callback;
        }

        /**
         * Runs the removal and necessary action(s)
         *
         * @param objects ...
         * @return Success
         */
        @Override
        protected Boolean doInBackground(Object... objects) {
            PublicContribution thing = (PublicContribution) objects[0];
            int action = (int) objects[1];
            String removalString = (String) objects[2];
            String pmSubject = (String) objects[3];
            boolean modmail = (boolean) objects[4];
            boolean sticky = (boolean) objects[5];
            boolean lock = (boolean) objects[6];
            boolean log = (boolean) objects[7];
            String logTitle = (String) objects[8];
            String logSub = (String) objects[9];
            String[] flair = (String[]) objects[10];

            boolean success = true;

            String logResult = "";
            if (log) {
                // Log the removal
                Submission s = logRemoval(logSub, logTitle, "https://www.reddit.com"
                        + thing.getDataNode().get("permalink").asText());
                if (s != null) {
                    logResult = "https://www.reddit.com" + s.getDataNode().get("permalink").asText();
                } else {
                    success = false;
                }
            }

            // Check what the desired action is and perform it
            switch (action) {
                case R.id.comment:
                    success &= postRemovalComment(thing, removalString.replace("{loglink}", logResult), sticky);
                    break;
                case R.id.pm:
                    if (thing instanceof Comment) {
                        success &= sendRemovalPM(
                                modmail ? ((Comment) thing).getSubredditName() : "",
                                ((Comment) thing).getAuthor(),
                                pmSubject.replace("{loglink}", logResult),
                                removalString);
                    } else {
                        success &= sendRemovalPM(
                                modmail ? ((Submission) thing).getSubredditName() : "",
                                ((Submission) thing).getAuthor(),
                                pmSubject.replace("{loglink}", logResult),
                                removalString);
                    }
                    break;
                case R.id.both:
                    success &= postRemovalComment(thing, removalString.replace("{loglink}", logResult), sticky);
                    if (thing instanceof Comment) {
                        success &= sendRemovalPM(
                                modmail ? ((Comment) thing).getSubredditName() : "",
                                ((Comment) thing).getAuthor(),
                                pmSubject.replace("{loglink}", logResult),
                                removalString);
                    } else {
                        success &= sendRemovalPM(
                                modmail ? ((Submission) thing).getSubredditName() : "",
                                ((Submission) thing).getAuthor(),
                                pmSubject.replace("{loglink}", logResult),
                                removalString);
                    }
                    break;
                // case R.id.none is unnecessary as we don't do anything on none.
            }

            // Remove the item and lock/apply necessary flair
            try {
                new ModerationManager(Authentication.reddit).remove((PublicContribution) objects[0], false);
                if (lock && thing instanceof Submission) {
                    new ModerationManager(Authentication.reddit).setLocked(thing);
                }
                if ((flair[0].length() > 0 || flair[1].length() > 0) && thing instanceof Submission) {
                    new ModerationManager(Authentication.reddit).setFlair(((Submission) thing).getSubredditName(),
                            (Submission) thing, flair[0], flair[1]);
                }
            } catch (ApiException | NetworkException e) {
                success = false;
            }

            return success;
        }

        /**
         * Run the callback
         *
         * @param success Whether doInBackground was a complete success
         */
        @Override
        protected void onPostExecute(Boolean success) {
            // Run the callback on the UI thread
            callback.onComplete(success);
        }

        /**
         * Send a removal PM
         *
         * @param from    empty string if from user, sub name if from sub
         * @param to      recipient
         * @param subject subject
         * @param body    body
         * @return success
         */
        private boolean sendRemovalPM(String from, String to, String subject, String body) {
            try {
                new InboxManager(Authentication.reddit).compose(from, to, subject, body);
                return true;
            } catch (ApiException | NetworkException e) {
                return false;
            }
        }

        /**
         * Post a removal comment
         *
         * @param thing   thing to reply to
         * @param comment comment text
         * @param sticky  whether to sticky the comment
         * @return success
         */
        private boolean postRemovalComment(PublicContribution thing, String comment, boolean sticky) {
            try {
                // Reply with a comment and get that comment's ID
                String id = new AccountManager(Authentication.reddit).reply(thing, comment);

                // Sticky or distinguish the posted comment
                if (sticky) {
                    new ModerationManager(Authentication.reddit)
                            .setSticky((Comment) Authentication.reddit.get("t1_" + id).get(0), true);
                } else {
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(
                            Authentication.reddit.get("t1_" + id).get(0), DistinguishedStatus.MODERATOR);
                }
                return true;
            } catch (ApiException | NetworkException e) {
                return false;
            }
        }

        /**
         * Log a removal to a logsub
         *
         * @param logSub name of log sub
         * @param title  title of post
         * @return resulting submission
         */
        private Submission logRemoval(String logSub, String title, String link) {
            try {
                return new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(
                        new URL(link),
                        logSub,
                        title
                ));
            } catch (MalformedURLException | ApiException | NetworkException e) {
                return null;
            }
        }

        /**
         * Convenience method to execute the task with the correct parameters
         *
         * @param thing         Thing being removed
         * @param action        Action to take
         * @param removalReason Removal reason
         * @param pmSubject     Removal PM subject
         * @param modmail       Whether to send PM as modmail
         * @param sticky        Whether to sticky removal comment
         * @param lock          Whether to lock removed thread
         * @param log           Whether to log the removal
         * @param logTitle      Log post title
         * @param logSub        Log subreddit
         * @param flair         Flair [text, CSS]
         */
        public void execute(PublicContribution thing, int action, String removalReason, String pmSubject,
                boolean modmail, boolean sticky, boolean lock, boolean log, String logTitle, String logSub,
                String[] flair) {
            super.execute(thing, action, removalReason, pmSubject, modmail, sticky, lock, log, logTitle, logSub, flair);
        }
    }

    /**
     * Add a usernote for a subreddit
     * Parameters are:
     * subreddit
     * user
     * note text
     * link
     * type
     */
    public static class AsyncAddUsernoteTask extends AsyncTask<String, Void, Boolean> {
        private WeakReference<Context> contextRef;

        AsyncAddUsernoteTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String reason;

            Toolbox.downloadUsernotes(strings[0]);
            if (Toolbox.getUsernotes(strings[0]) == null) {
                Toolbox.createUsernotes(strings[0]);
                reason = "create usernotes config";
            } else {
                reason = "create new note on user " + strings[1];
            }
            Toolbox.getUsernotes(strings[0]).createNote(
                    strings[1], // user
                    strings[2], // note text
                    strings[3], // link
                    System.currentTimeMillis(), // time
                    Authentication.name, // mod
                    strings[4] // type
            );
            try {
                Toolbox.uploadUsernotes(strings[0], reason);
            } catch (InvalidScopeException e) { // we don't have wikiedit scope, need to reauth to get it
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                final Context context = contextRef.get();
                if (context == null) {
                    return;
                }
                new MaterialDialog.Builder(context)
                        .title(R.string.toolbox_wiki_edit_reauth)
                        .content(R.string.toolbox_wiki_edit_reauth_question)
                        .negativeText(R.string.misc_maybe_later)
                        .positiveText(R.string.btn_yes)
                        .onPositive((dialog1, which1) -> context.startActivity(
                                new Intent(context, Reauthenticate.class)))
                        .show();
            }
        }
    }

    /**
     * Remove a usernote from a subreddit
     * Parameters are:
     * subreddit
     * user
     */
    public static class AsyncRemoveUsernoteTask extends AsyncTask<String, Void, Boolean> {
        private Usernote note;
        private WeakReference<Context> contextRef;

        AsyncRemoveUsernoteTask(Usernote note, Context context) {
            this.note = note;
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            Toolbox.downloadUsernotes(strings[0]);
            Toolbox.getUsernotes(strings[0]).removeNote(strings[1], note);
            try {
                Toolbox.uploadUsernotes(strings[0], "delete note " + note.getTime() + " on user " + strings[1]);
            } catch (InvalidScopeException e) { // we don't have wikiedit scope, need to reauth to get it
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                final Context context = contextRef.get();
                if (context == null) {
                    return;
                }
                new MaterialDialog.Builder(context)
                        .title(R.string.toolbox_wiki_edit_reauth)
                        .content(R.string.toolbox_wiki_edit_reauth_question)
                        .negativeText(R.string.misc_maybe_later)
                        .positiveText(R.string.btn_yes)
                        .onPositive((dialog1, which1) -> context.startActivity(
                                new Intent(context, Reauthenticate.class)))
                        .show();
            }
        }
    }

    /**
     * A callback for code to be run on the UI thread after removal.
     */
    public interface CompletedRemovalCallback {
        /**
         * Called when the removal is completed
         *
         * @param success Whether the removal and reason-sending process was 100% successful or not
         */
        void onComplete(boolean success);
    }
}
