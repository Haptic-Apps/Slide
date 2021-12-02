package me.ccrama.redditslide.util.preference;

import me.ccrama.redditslide.R;

public class PrefKeys {
    // Root menu settings
    public static final int PREF_ROOT_GENERAL = R.string.prefKey_general;
    public static final int PREF_ROOT_MULTICOLUMN = R.string.prefKey_multiColumn;
    public static final int PREF_ROOT_MANAGE_SUBREDDITS = R.string.prefKey_manageSubreddits;
    public static final int PREF_ROOT_MANAGE_OFFLINE_CONTENT = R.string.prefKey_manageOfflineContent;
    public static final int PREF_ROOT_MODERATION = R.string.prefKey_moderation;
    public static final int PREF_ROOT_MAIN_THEME = R.string.prefKey_mainTheme;
    public static final int PREF_ROOT_POST_LAYOUT = R.string.prefKey_postLayout;
    public static final int PREF_ROOT_SUBREDDIT_THEMES = R.string.prefKey_subredditThemes;
    public static final int PREF_ROOT_FONT = R.string.prefKey_font;
    public static final int PREF_ROOT_COMMENTS = R.string.prefKey_comments;
    public static final int PREF_ROOT_LINK_HANDLING = R.string.prefKey_linkHandling;
    public static final int PREF_ROOT_HISTORY = R.string.prefKey_history;
    public static final int PREF_ROOT_DATA_SAVING = R.string.prefKey_dataSaving;
    public static final int PREF_ROOT_FILTER_LIST = R.string.prefKey_filterList;
    public static final int PREF_ROOT_REDDIT_CONTENT = R.string.prefKey_redditContent;
    public static final int PREF_ROOT_BACKUP_RESTORE = R.string.prefKey_backupRestore;
    public static final int PREF_ROOT_SYNCCIT = R.string.prefKey_synccit;
    public static final int PREF_ROOT_PRO_UPGRADE = R.string.prefKey_proUpgrade;
    public static final int PREF_ROOT_DONATE = R.string.prefKey_donate;
    public static final int PREF_ROOT_ABOUT = R.string.prefKey_about;


    // Multi-column settings
    public static final int PREF_LANDSCAPE_COLUMN_NUMBER = R.string.prefKey_multicolumn_landscapeColumnNumber;
    public static final int PREF_SINGLE_COLUMN_MULTI_WINDOW = R.string.prefKey_multicolumn_singleColumnMultiWindow;//"singleColumnMultiWindow"
    public static final int PREF_PORTRAIT_MODE_DUAL_COLUMNS = R.string.prefKey_multicolumn_portraitModeDualColumns;//"dualPortrait"
    public static final int PREF_FORCE_FULL_COMMENT_VIEW = R.string.prefKey_multicolumn_forceFullCommentView;//"fullCommentOverride"


    // Moderation settings
    public static final int PREF_REMOVAL_REASON_TYPE = R.string.prefKey_moderation_removalReasonType;//"removalReasonType"
    public static final int PREF_REMOVAL_REASON_TYPE_SLIDE = R.string.prefKey_moderation_removalReasonType_slide;
    public static final int PREF_REMOVAL_REASON_TYPE_TOOLBOX = R.string.prefKey_moderation_removalReasonType_toolbox;
    public static final int PREF_ENABLE_TOOLBOX = R.string.prefKey_moderation_enableToolbox;//"toolboxEnabled"
    public static final int PREF_SENDING_METHOD = R.string.prefKey_moderation_sendingMethod;//"toolboxMessageType"
    public static final int PREF_SENDING_METHOD_COMMENT = R.string.prefKey_moderation_sendingMethod_comment;
    public static final int PREF_SENDING_METHOD_PM = R.string.prefKey_moderation_sendingMethod_pm;
    public static final int PREF_SENDING_METHOD_BOTH = R.string.prefKey_moderation_sendingMethod_both;
    public static final int PREF_SENDING_METHOD_NONE = R.string.prefKey_moderation_sendingMethod_none;
    public static final int PREF_SEND_AS_SUBREDDIT = R.string.prefKey_moderation_sendAsSubreddit;//"toolboxModmail"
    public static final int PREF_STICKY_REMOVAL_COMMENTS = R.string.prefKey_moderation_stickyRemovalComments;//"toolboxSticky"
    public static final int PREF_LOCK_THREAD_AFTER_POST = R.string.prefKey_moderation_lockThreadAfterPost;//"toolboxLock"
    public static final int PREF_REFRESH_TOOLBOX_DATA = R.string.prefKey_moderation_refreshToolboxData;


    // Comments settings
    public static final int PREF_CROP_IMAGE = R.string.prefKey_comments_cropImage;//"cropImage"
    public static final int PREF_COLOR_COMMENT_DEPTH = R.string.prefKey_comments_colorCommentDepth;//"colorCommentDepth"
    public static final int PREF_HIGHLIGHT_COMMENT_OP = R.string.prefKey_comments_highlightCommentOp;//"commentOP"
    public static final int PREF_WIDE_DEPTH = R.string.prefKey_comments_wideDepth;//"largeDepth"
    public static final int PREF_SHOW_COMMENT_FAB = R.string.prefKey_comments_showCommentFab;//"commentFab"
    public static final int PREF_RIGHT_HANDED_COMMENT_MENU = R.string.prefKey_comments_rightHandedComments;//"rightHandedCommentMenu"
    public static final int PREF_SHOW_UPVOTE_PERCENTAGE = R.string.prefKey_comments_showUpvotePercentage;//"upvotePercentage"
    public static final int PREF_COLORED_TIME_BUBBLE = R.string.prefKey_comments_coloredTimeBubble;//"highlightTime"
    public static final int PREF_HIDE_COMMENT_AWARDS = R.string.prefKey_comments_hideCommentAwards;//"hideCommentAwards"
    public static final int PREF_PARENT_COMMENT_NAV = R.string.prefKey_comments_parentCommentNav;//"Fastscroll"
    public static final int PREF_AUTOHIDE_COMMENT_NAVBAR = R.string.prefKey_comments_autohideCommentNavbar;//"autohideComments"
    public static final int PREF_SHOW_COLLAPSE_EXPAND_BUTTON = R.string.prefKey_comments_showCollapseExpandButton;//"showCollapseExpandButton"
    public static final int PREF_VOLUME_NAV_COMMENTS = R.string.prefKey_comments_volumeNavComments;//"commentVolumeNav"
    public static final int PREF_NAVBAR_VOTE_GESTURES = R.string.prefKey_comments_navbarVoteGestures;//"voteGestures"
    public static final int PREF_SWAP_LONGPRESS_TAP = R.string.prefKey_comments_swapLongpressTap;//"Swap"
    public static final int PREF_FULLY_COLLAPSE_COMMENTS = R.string.prefKey_comments_fullyCollapseComments;//"collapseCOmments"
    public static final int PREF_COLLAPSE_CHILD_COMMENTS = R.string.prefKey_comments_collapseChildComments;//"collapseCommentsDefault"
    public static final int PREF_COLLAPSE_DELETED_COMMENTS = R.string.prefKey_comments_collapseDeletedComments;//"collapseDeletedComments"


    // History settings
    public static final int PREF_STORE_HISTORY = R.string.prefKey_history_storeHistory;//"storehistory"
    public static final int PREF_STORE_NSFW_HISTORY = R.string.prefKey_history_storeNsfwHistory;//"storensfw"
    public static final int PREF_SCROLL_SEEN = R.string.prefKey_history_scrollSeen;//"scrollSeen"
    public static final int PREF_CLEAR_SUBMISSION_HISTORY = R.string.prefKey_history_clearSubmissionHistory;
    public static final int PREF_CLEAR_SUBREDDIT_HISTORY = R.string.prefKey_history_clearSubredditHistory;


    // Data-saving settings
    public static final int PREF_ENABLE_DATA_SAVING = R.string.prefKey_datasaving_enableDataSaving;
    public static final int PREF_ENABLE_DATA_SAVING_NEVER = R.string.prefKey_datasaving_enableDataSaving_never;
    public static final int PREF_ENABLE_DATA_SAVING_MOBILE = R.string.prefKey_datasaving_enableDataSaving_mobile;//"lowRes"
    public static final int PREF_ENABLE_DATA_SAVING_ALWAYS = R.string.prefKey_datasaving_enableDataSaving_always;//"lowResAlways"
    public static final int PREF_IMAGE_QUALITY = R.string.prefKey_datasaving_imageQuality;
    public static final int PREF_IMAGE_QUALITY_NEVERLOAD = R.string.prefKey_datasaving_imageQuality_neverLoad;//"noImages"
    public static final int PREF_IMAGE_QUALITY_LOW = R.string.prefKey_datasaving_imageQuality_low;//"lqLow"
    public static final int PREF_IMAGE_QUALITY_MEDIUM = R.string.prefKey_datasaving_imageQuality_medium;//"lqMid"
    public static final int PREF_IMAGE_QUALITY_HIGH = R.string.prefKey_datasaving_imageQuality_high;//"lqHigh"
    public static final int PREF_LOW_QUALITY_VIDEOS = R.string.prefKey_datasaving_lowQualityVideos;//"lqVideos"


    // About settings
    public static final int PREF_ABOUT_REPORT_BUGS = R.string.prefKey_about_reportBugs;
    public static final int PREF_ABOUT_CHANGELOG = R.string.prefKey_about_changelog;
    public static final int PREF_ABOUT_RATE = R.string.prefKey_about_rate;
    public static final int PREF_ABOUT_SUBREDDIT = R.string.prefKey_about_subreddit;
    public static final int PREF_ABOUT_LIBRARIES = R.string.prefKey_about_libraries;
    public static final int PREF_ABOUT_VERSION = R.string.prefKey_about_version;
}
