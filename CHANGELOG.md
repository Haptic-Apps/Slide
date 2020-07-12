6.5 / 2020-07-12
==================
* Huge Video rewrite, thank you to @timawesomeness! Contains support for DASH videos, uses newer ExoPlayer code, and cleans up some older video sites
* Support for mod locking of individual comments
* Added option to save photos to a manual folder (thanks to @Archeidos)
* Updated Fastlane metadata for F-Droid builds (thanks to @obar)
* Fixed offline mode crash when no subreddits were cached
* Fixed posting to profiles with more than 20 characters (thanks to @LostGhost1)
* Added an always show FAB option in Settings (thanks to @darshan099)
* Graceful handling of submission loading errors with new "try again" button (thanks to @ctbur)
* Fix OnePlus and Huawei media scanner intents, will show images in gallery on these devices (thanks to @Rexee)
* Fixed handling of intent links in internal browser
* Fix toolbox encoded chars in removal reason text
* Fixed startup issue on some devices

6.4 / 2020-05-24
==================
  * Support for redgifs.com
  * Banner images in subreddit sidebars
  * Fixes to Imgur upload process
  * Translation updates
  * Updated accessibility labels

6.3 / 2020-02-17
==================
  * Reduce data usage on metered connections
  * Option to hide the download button in media views
  * New back button behavior option to go to first subreddit

6.2.2 / 2019-08-06
==================
  * Fixes an issue with Twitter links loading in-app
  * Improves filter behavior with a large number of saved filters

6.2.1 / 2019-07-25
==================
  * Improvements to behavior with Android Pie keyboard focus
  * Accessibility improvements to app buttons
  * Fixed crash with "Remove with Reason"
  * Fixed various issues with new Reddit flairs

6.2 / 2019-04-05
==================
  * Added support for Reddit 2-Factor Authentication for login
  * Smoother animation interpolator
  * Translation updates
  * Better support for other string encodings in Reader mode
  * Removed "Best" as default sorting option, and only display it on the frontpage subreddit
  * Fixed an issues where Gfycats wouldn't load if they had a hyphen in the URL
  * Fixed i.redd.it and other direct GIF links not loading internally
  * Initial round of Toolbox bug fixes

6.0.1 / 2018-11-30
==================
  * Adaptive icon by Michael Cook (@cookicons)
  * Support for new Gfycat API
  * Add selectable subreddit rules for quicker reports for moderators
  * Autocorrect text in submit post screen title
  * Search FAB option
  * Guest mode has been moved to the sidebar header
  * Searchable settings menu, now you don't have to go through ten settings menus to find what you are looking for!
  * Support for native Reddit spoiler markdown
  * Improved lock/contest mode headers
  * Much more robust link handling system, your link handling settings WILL BE RESET with this version!
  * Night mode now matches automatic night times on Android M and above
  * Completely new wiki implementation that is much faster and links actually work!
  * Setting to change the items displayed in drawer (inbox, discover, multireddits, etc)
  * Option for a bottom bar button that collapses/shows all comments
  * Support for 'Best' sorting
  * Support for Reddit Silver and Platinum
  * Subscriber counts in Discover
  * App themes are now per-user (guest can have a different theme color, for example)
  * You can switch accounts from the comment reply FAB
  * Option to select any installed browser for your external browser settings (overrides your device default)
  * Internal browser fullscreen support
  * Your phone will no longer try to autofill text with passwords
  * Contest mode banner uses the correct font color
  * Fixed comment screen rotation
  * Fixed post notifications
  * Many other bug fixes and improvements!

5.8.7 / 2018-06-10
==================
  * Support for old/new.reddit.com links

5.8.6 / 2018-04-21
==================
  * Option to disable comment inbox replies
  * Added an option to make the back button open the Slide drawer (can now set it to show a quit dialog, exit immediately, or show navigation drawer, thanks to Contributor PiwwowPants)
  * Fixed crashing in Shadowbox mode with v.redd.it videos
  * Fixed "htts" link sharing
  * Confirm dialog before backing up or restore (thanks to Contributor YusefOuda)

5.8.5 / 2018-04-03
==================
  * Support for Firefox as your default Slide browser (separate from the Android default browser)
  * Fixed the Reddit flair issue that popped up a few days ago
  * Support for devices running Android P
  * Moved quote button closer to the front of the editor menu
  * Button to remove user color
  * Option to share long Reddit links instead of shortlinks
  * Many more bug fixes and improvements!

5.8 / 2018-03-02
================
  * Crosspost submissions from the submission menu, and new UI for crossposted submissions
  * Added new "@" button on the comment reply menu which shows you a history of authors in the current comment chain and allows you to easily tag users in your comment
  * Added a mute button to videos (v.redd.it with sound and .mp4s)
  * Support v.redd.it videos with sound, and downloading these videos saves an actual mp4 file to your device!
  * Implements ExoPlayer to revamp video views with new video codec support, a new video interface, improved seekbar, and more refined pause/play toggle
  * Externally opened links or notifications will open in a second task, and closing the activity won't switch you back to Slide underneath
  * Slide now shares the shortlink when sharing a submission
  * Setting for colored time indicator for new comments since last visit
  * New "deep" theme (dark purple based)
  * Added new feature to highlight top-level comment OPs with a purple line (can be disabled in settings)
  * Support for opening AMP links in Slide and improved intent link handling
  * Fixed LTR font rendering in comments
  * Fixed ending time of Night Mode bug
  * Fixed image previews in Shadowbox mode
  * Fixed Android notification sounds and 2x notifications
  * Fixed image save notification not finishing
  * Fixed bugs relating to file intents/fixed camera button for inserting images into comments and submissions
  * Target Android 8.1
  * Tons of other bug fixes and improvements!

5.7.3 / 2018-02-14
==================
  * Fixes relating to how Reddit changed up the endpoint putting an unescaped ampersand in image links

5.7.2 / 2017-12-07
==================
  * Removes dependency on third party API for gif displays in submissions and falls back to web when gif to MP4 conversion is not possible
  * Adds streaming gif support for some longer gifs
  * Adds wide color gamut support for media viewers
  * Improves gif seekbar performance on API 26 and above

5.7.1 / 2017-10-18
==================
  * The method for notification piggybacking has been changed and now Slide can remove the notification from Reddit! This means you will have to enable it again through Settings > General. Unfortunately this method will only work Jellybean and beyond
  * Some card mode layout changes to fix some awkward padding and a small fix to restore from file (pro)

5.7 / 2017-10-15
================
  * Notification piggybacking off of the Official Reddit app! When you get a message there, Slide will mirror that message and show you a new notification. Slide must have accessibility access for this to work, and only uses this access to mirror notifications from the Official Reddit App. Slide is open source, and you can view the [code](https://github.com/ccrama/Slide/blob/master/app/src/main/java/me/ccrama/redditslide/Notifications/NotificationPiggyback.java) here if you are interested in how Slide uses these permissions!
  * Ability to block users from their profile (thanks to YusefOuda)
  * Setting to swap info line and title line (will be default in a future version of Slide to match more with Slide on iOS, but the setting will remain)
  * Copy a Reddit link to your clipboard, and Slide will ask if you want to open it upon entering the app
  * Support for Android O notification channels
  * NSFW previews are now tied to Slide accounts individually
  * Insert image link as is if no caption is provided
  * Adblock is now disabled for Twitter.com to reduce likelyhood of running into issues loading Twitter on the Internal browser
  * Thumbnail settings are respected per-sub, but you can override this in content settings
  * Setting to disable NSFW previews in collections, like [/r/all](https://www.reddit.com/r/all)
  * Force image previews even if user has turned the setting off in reddit prefs (now only Slide settings affect image previews)
  * Use Reddit for some Gif conversions
  * Support for v.reddit.com links
  * Setting for single column in multi-window
  * Asks for confirmation before uploading to Imgur
  * Discard button on draft save bar
  * Fixed some drafts not saving
  * Fixed icon size in Slide notifications on some devices
  * Fixed account switching issue on Android O (Java 1.8 bug)
  * Fixed Bootloop caused by Android AAPT2 on Cyanogenmod
  * Fixed Reddit Server 403 Error some users were running into
  * Target SDK 26
  * NSFW subs work without being logged in
  * Many other bug fixes and improvements!

5.6.2 / 2017-05-23
==================
  * There was a server-side issue with the Reddit API and the Captcha system which caused sending all submissions and messages to fail, and this update fixes that by disabling Captcha checking all together (it appears as if sending messages/submitting posts doesn't need a Captcha through the API anymore)

5.6 / 2017-04-23
================
  * Adds support for setting the default subreddit sorting in subreddit sidebar
  * Temporarily switch accounts from the comment box
  * Image Flair syncing based on code by [/u/ProgramTheWorld](https://www.reddit.com/u/ProgramTheWorld) (sync from sidebar, should work on most subs but please let me know if you find one that doesn't work!), [Screenshots](http://imgur.com/a/oGwNP)
  * Option to download images to subreddit-specific subfolders
  * Adds support for "Pin" system in reorder screen (pinned subs won't reorder when alphabetizing)
  * Adds option for auto alphabetization in reorder screen for new subscriptions
  * Adds new 'Pixel' base theme based on SystemUI colors of the Pixel [screenshot](http://i.imgur.com/3XfeKzI.jpg)
  * Added Toast display with information when request fails to user
  * Setting to make comment depth indicators wider
  * Setting to open websites in Reader mode by default & Reader mode visual revamp
  * PRO: Option to only show reader mode during night mode
  * Made related submissions work again
  * Mods: send message as subreddit
  * Mods: support for adding/removing spoilers for moderators
  * Mods: moderation menu for comments in modqueue
  * Fixed issue on some devices where widget was black text on black background
  * Fixed search bug some users were running into
  * Fixed issue where it would say no multireddits are created
  * Scaled down notification images to keep Android System from running out of memory
  * Fixed the bug that no content would show after returning to app/reload after network failure
  * Fixed IPv6 Gfycat issues
  * Disabled swipe to exit on Subreddit Reorder screen
  * Fixed donation system
  * Many more bug fixes and improvements!

5.5.5 / 2017-02-17
==================
  * Fixed the issue some users were having with laggy notifications while downloading images
  * Added spoiler tag support
  * Allowed you to add [r/popular](https://www.reddit.com/r/popular) to your subreddits (much like [r/all](https://www.reddit.com/r/all))
  * Bug fixes with Immersive Mode
  * Other bug fixes and improvements

5.5.4 / 2016-12-20
==================
  * New Website header scrolling (no more double scroll header)
  * Support for direct message links (like remindme)
  * Three levels of datasaving settings thanks to [/u/touzainanboku](https://www.reddit.com/u/touzainanboku)
  * Notifications now dismiss after clicking them
  * Revamp of the Submit screen with ability to upload images, gifs, and albums (same as the edit bar on the text fields)
  * Support GIF keyboard pre-Android N
  * Improvements to immersive mode thanks to [/u/jseybold](https://www.reddit.com/u/jseybold)
  * Lots of bug fixes and improvements

5.5.3 / 2016-12-11
==================
  * Round icon support (Android 7.1.1, thanks to [/u/themichaelcook](https://www.reddit.com/u/themichaelcook)!)
  * Bundled notifications (Android N and above), each message is expandable by itself!
  * Immersive mode improvements
  * Score formatting changed to follow Reddit's new formatting (20.2k instead of 20200) with option to show full score
  * YouTube plugin updated to fix attribution link bug
  * Lots of other bug fixes and improvements!

5.5.2 / 2016-11-22
==================
  * Android 7.1 Features: [App Shortcuts](http://i.imgur.com/kNJHmdh.jpg) and [Image Keyboard](http://i.imgur.com/rbS6xuh.png) Support!
  * New activity/image/gif loading transitions
  * Option for immersive mode in Settings > General
  * Long click subs in the sidebar to open them above the main screen
  * Fixed older devices crashing when logging in
  * Offline mode fixes (works with comment pager mode now)
  * Fixed some issues with offline mode not working on certain view types
  * Many bug fixes and improvements!

5.5.1 / 2016-10-25
=================
  * Some bug fixes from the 5.5 release

5.5 / 2016-10-21
================
  * Force touch links and images, enable this experimental feature in Settings > Link Handling
  * Deepzoom single-image albums
  * Improved image saving with new notification buttons to share/delete
  * Mods can re-approve already approved content
  * View information about our Open Source libraries in Settings > About!
  * Tweaked subreddit and theme colors
  * Comment highlighting improvements
  * Table alignment now works
  * "You" sort in comment navigation
  * New image picker in the editor menu (bottom sheet with recent images, and can take images from the camera as well)
  * Animated toolbar color change when switching pages
  * Moderation works now when clicking through the widget
  * Updated translations (thank you to all who contribute on [Crowdin]
  * MANY other bug fixes and improvements!
  (https://crowdin.com/project/slide-for-reddit)!)

5.4.3.5 / 2016-09-10
====================
  * Fixes issues with content not loading after a network change
  * Fixes the inability to upload images into comments
  * Fixes some media players getting paused with gifs!

5.4.3 / 2016-09-08
==================
  * Full Tumblr post integration! Looks and acts like the Album and Vertical Album views
  * Related submissions in comment toolbar, and you can share links to Slide to view related submissions
  * Improvements to offline mode (it will show last cached comments first instead of submissions)
  * Albums have post/comment fonts
  * Upload multiple images to Imgur albums
  * Read Later button with a section under Profile > Read Later. Merged with offline save as well (thanks to [/u/mr_novembor](https://www.reddit.com/u/mr_novembor))
  * Option for larger link font size
  * New design for gilds (thanks to [/u/freundTech](https://www.reddit.com/u/freundTech))
  * Redesigned some settings screens, Made Offline content settings more accessible
  * Fixed a request bug that was making some of you guys slower, now it will be as fast as desktop all the time
  * Revamp of mod tools, view who removed/approved submissions and comments and why, lock and unlock threads, instant feedback (submission view will change when you do actions on it), ability to distinguish submissions, removed "Are you sure?" dialogs and just execute the action, ability to see removed posts and comments for reapproval, see who removed/reapproved posts and comments, mod log, removal of the "Are you sure?" dialogs, and more!
  * Child comment navigation is its own button now
  * Option to disable comment navigation bar shortcut actions
  * Hopefully fixed blank widget bug and duplicated thumbnail image bug
  * Thanks to contributor [/u/freundTech](https://www.reddit.com/u/freundTech), all the reported Android N issues were addressed/fixed
  * There is now a setting for tinting the Slide icon based on your main sub theme
  * File size in image loading
  * Improved text selection dialog
  * Made post notifications actually work (introduced a bug last version), will be reliable as well!
  * Slide is now 1.4mb smaller
  * Fixed comment text field issues
  * Fixed setting main color with colored icons enabled
  * Tweaked gild icon
  * Fixed gifs not displaying
  * Fixed all gif issues (sound and closing)
  * Fixed double bullets
  * Tweaked mail notification system
  * Long press for XKCD info/alttext
  * Improvement to gif view and controls (fixed strange glitches that let you see the view on the sides even if it wasn't open)
  * Fixed [r/random](https://www.reddit.com/r/random) and [r/randnsfw](https://www.reddit.com/r/randnsfw) bugs
  * Fixed history being counted with history turned off
  * MANY improvements to sidebar typing and subreddit selection, now it won't clear if the sub doesn't exist
  * Many other bug fixes and improvements!

5.4.2.3 / 2016-08-23
====================
  * XKCD Support!
  * Fixed all reported bugs (spoilers, crashing when opening subs when not logged in, reported to my dev console, and more)
  * New Red Shift theme
  * Sidebar button in comment overflow menu
  * Links clicked in the Website view will load in Slide views if possible (Reddit links, albums, etc)
  * Turn data saving into a 3 choice menu (v5.4.2.2)
  * Improvements to night mode menu (thanks [/u/inate71](https://www.reddit.com/u/inate71))!
  * Copy menu now uses select dialog from quote menu (v5.4.2.2)

5.4.2 / 2016-08-19
==================
  * TONS of Offline Mode improvements thanks to /u/mr_novembor including depth limit, comments to cache, and ability to cache individual posts for offline mode!
  * New post layout type: Desktop Condensed!
  * Subreddit post notifications with score threshold settings per sub!
  * PRO Ad block and cookie block in Website view
  * Reader Mode in Website view
  * PRO Comment shadowbox mode
  * Check for post notifications on app start
  * PRO: New option to crop and draw on images to upload!
  * PRO: Auto night theme with configurable times in Settings > Main Theme
  * Per-subreddit flair filter
  * Swipe from first image to exit the Album pager
  * Superscript is actually small
  * Sibling comment navigation if you highlight a comment at a certain level   and hit the arrows
  * Option to show content type next to links!
  * Overhaul of Manage Subscriptions screen with ability to subscribe/unsubscribe, more intuitive controls
  * Highlight text in comment search
  * User post filter
  * Added filter user posts and submission flair to the post flair menu
  * New Settings layout
  * Direct reply from inbox
  * Inbox "new" tag for unviewed unread messages (since last inbox visit)
  * Thread title in inbox messages
  * Album downloads now work (and can download more than one image at a time)
  * Added size to gifs in albums
  * Ability to click links in album image titles and descriptions
  * Option for vibrate and sound in Inbox notifs
  * Notification toast for Reddit API errors
  * Ability to ban from the comment menu
  * Bold titles of tables
  * Reversed draft order
  * Fixed issue where you couldn't tap or long press in code blocks/tables
  * Fixed issue where vote count would add extra if you voted on something
  * Fixed subscribing in Manage Subscriptions
  * Fixed gifs not loading all the time in albums
  * Tons of other (more than 30) bug fixes and improvements

5.4.0 / 2016-07-21
==================
  * Support for API 15 (Ice Cream Sandwich)!
  * Live thread support with inline Twitter and Imgur views, and auto-updates
  * Button in all media views to open comments directly! Shows in images, gifs, and albums you have opened from the post screen
  * Select text to quote from the editor menu
  * Mods: Ban users with reason, message, and time!
  * Add to Multireddit button from subreddit sidebars
  * Message subreddit moderators from the sidebar
  * TONS of backend improvements to image, album, and gif loading, and speed improvements all around
  * View active users in the sidebar
  * Immersive mode for images and gifs
  * Renamed "Link Karma" to "Post Karma"
  * Support for the Reddit feature of highlighting new comments since last visit
  * Download images in the background with separate notifications for each
  * Many more aesthetic tweaks and bug fixes

5.3.9 / 2016-05-27
==================
  * Brand new list view look!
  * Hide button in overflow menu if you have the FAB disabled
  * Added commas to some larger numbers
  * Improved gallery mode style, long press for link actions
  * New activity animations
  * Disabled saved categories if you don't have gold (Reddit limitation)
  * Fixed language override on screen rotation
  * Manage (and batch manage) subscriptions in the Manage Subscriptions setting page
  * Synccit fixes
  * Extra large font size
  * Strikethrough in the editor menu
  * Lots of other bug fixes and improvements

5.3.8 / 2016-05-19
==================
  * Saved categories! You can now save submissions and comments into categories and sort through your categories on the Profile screen
  * More supported markdown in previews (strikethrough and tables)
  * Share anything through NFC! Highlight a comment and tap to share comments, or share submissions and opened content
  * Make a domain always open externally in the CustomTabs and Website view
  * Gif size indicator while downloading (in kb/mb in bottom left corner)
  * Fixed double loading in media views
  * More readable blockquote bar
  * Added auto-draft to Submit screen
  * Other bug/stability fixes

5.3.7 / 2016-05-14
==================
  * Option to not load any images or force load LQ Imgur images (works in albums  and direct links too!)
  * Fixed reddituploads links not working
  * Added a Sepia base theme
  * Added a compact widget mode
  * Made history load in the sidebar instantly
  * Made setting to hide all selftext images in Settings > Data Saving
  * Made the drawer touch target bigger
  * Added content icons to Gallery mode
  * Fixed toolbar popping up with no way to close it
  * Fixed some visual bugs and other crashes

5.3.6 / 2016-05-09
==================
  * Gallery mode (3 dot menu > Gallery mode) for Pro users!
  * Customizable subreddit homescreen widget!
  * Ability to add a website as a subreddit in the Settings > Manage subs screen
  * 10 Pro free trials for Shadowbox and Gallery modes
  * Option to show upvote and comment count when actionbar is disabled
  * Option to show content type if big image is not visible
  * Long press the Multireddit item in the sidebar to go to a user's public multireddits
  * View a user's public multireddits in the profile info button
  * New selection style for current sorting
  * Improved GIF and Image cache
  * New album loading code
  * Option to long press on the subreddit title to view list of your subs
  * Check sorting type from the button in the toolbar
  * Much faster image and gif opening
  * Lots of other bug fixes and improvements

5.3.4 / 2016-05-02
==================

  * Added better username validation
  * Added on fly conversion to multi
  * Adding multi reddits from import screen
  * Disallow closing the restore dialog by clicking back
  * Enable suggestions when viewing profile
  * Fixed album crash
  * Fixed comment crash on KitKat
  * Fixed crash typing in the sidebar
  * Fixed profile margins
  * Make scroll to top faster in the drawer
  * Remove some full stops in strings

5.3.3 / 2016-05-01
==================

  * Added a 'Share Slide' button to the MainActivity overflow menu
  * Added ability to create a new folder from the image save location dialog
  * Added ability to download the base gif file
  * Added ability to edit search query from the Search activity
  * Added edit button to the card view too
  * Added friends list to the drawer under 'You'
  * Added Medium font setting for titles
  * Added more links to Settings > About
  * Added option to automatically hide the comment navigation bar
  * Added subreddit content filter for videos
  * Added support for /r/dota2 spoilers
  * Added touch ripples on largecards
  * Better scroll to top handling when far down in a list
  * Centered text inside post flair and tags
  * Color coded upvote percentages
  * Don't allow collapsing the currently editing comment
  * Enabled snackbars to be swiped away
  * Hide "Share Slide" menu item if user has Pro installed
  * Keep autocache based on history preferences
  * Moved add account out of the header area when not logged in
  * Moved rate snackbar to MainActivity
  * Normalise cases and wording on a few strings
  * Reduce lag scrolling through subreddits horizontally
  * Revamped the Donation screen
  * Show rate snackbar after 10 visits to Slide
  * Show submit text on applicable subreddits
  * Switched albums to new swiping style
  * Switched to using a horizontal loading bar for loading comments
  * Use a different icon colour for debug builds

5.3.2 / 2016-04-24
==================

  * Added a rate snackbar
  * Added a setting to view vote count percentage in the comment screen
  * Added ability to edit and view your user flair in the subreddit sidebar
  * Added more sorting types for navigating comments (link, op, gilded, time, and parent comment)
  * Don't allow photo upload if over 2mb
  * Don't clear out submissions when making room for gifs in cache
  * Don't translate font names
  * Insert top level reply into the comment tree instantly
  * Made Profile toolbar use a menu
  * Made wording more clear for offline content
  * More clear instructions while offline clicking on non-cached content
  * More specific matching for domain filters
  * Open unread directly from bottom shackbar and notification intent
  * Re-organized settings, adding more information to areas that needed it
  * Respect filetype while uploading images (png or jpg)
  * Share and recieve intents using EXTRA_SUBJECT for titles
  * Upload images using OutputStream instead of Base64 encoding
  * Uploaded the app to F-Droid!

5.3.1 / 2016-04-22
==================

  * Cache and preload info and title SpannableStringBuilders for submissions (shaves some MS off of onBindViewHolder)
  * Made leaving the comments screen go to the post you were last at
  * Make shadowbox drag area the title height
  * Show subreddit names in inbox items and mod queue

5.3.0 / 2016-04-21
================

  * Added option for bold title fonts
  * Adjustments to Profile cards
  * Don't show spinner forever if there are no posts in a subreddit
  * Hide "Mark all as read" button in Inbox when on Sent page
  * Made the time picker use the subreddit theme and show either AM or PM
  * Only load 25 posts at a time (vs 50)
  * Precache images while syncing subs for offline
  * Show popup if user is suspended
  * Stopped EditText fields from autofocusing upon entering certain Settings activities

5.2.4 / 2016-04-21
==================

  * Added Manage Offline Content section on all sidebars
  * Added ability to force an auto-cache at the current time
  * Cache the values of last seen (improves scrolling performance)
  * Made comments remember their state after being collapsed
  * Made comments since last seen work again
  * Show a opup if no offline content is found
  * Show a popup suggesting offline mode if trying to open comments without internet
  * Reduced number of calls to Synccit auth by 1
  * Removed EditText style for KitKat and below
  * Removed the hidden posts listing (not neccesary and caused RAM issues)
  * Reordered the subreddits in the offline sidebar to show subscribed subreddits first
  * Stricter link handling for opening externally

5.2.3 / 2016-04-19
==================

  * Added a button to force enter offline mode (even if internet access is unavailable)
  * Added a manage offline content section to the drawer
  * Added a method to apply the AMOLED with contrast theme to darker activities (like Shadowbox and MediaView)
  * Added a modal dialog if caching subs whilst the app is open
  * Added a new dragging layout to the MediaFragment, with the ability to vote, save, and view more options
  * Added a progress bar to albums while waiting for data
  * Added option to use the default system font in Settings > Font
  * Added the ability to autocache a list of subreddits at a certain time of day
  * Changed style of sorting strings
  * Made Album and AlbumPager use the new dark theme option
  * Made history work with the new storage system
  * Made offline mode only show cached subreddits
  * Added an option for autobackup only on Wi-Fi
  * Made the MediaFragment extend to Vid.me links and Streamable links, Made Album fragment use the new drag layout, Make sub names colored if applicable
  * Make switching accounts use apply() instead of commit()
  * Memory improvements in Shadowbox mode
  * Redesigned the profile dropdown (icons for offline mode and add account)
  * Show dropdown with cached subreddit options while in offline mode in the toolbar
  * Sidebar now uses comment font size
  * Simplified content filter dialog title
  * Updated the OfflineSubreddit model to save on a per-submission basis with the ability to store multiple states of the same subreddit

5.2.2 / 2016-04-17
==================

  * Fixed inbox popup with no messages
  * Fixed some albums not loading and fixed album buttons being behind the drag view
  * Fixed some embeds not working
  * Fixed submission layout paddings

5.2.1 / 2016-04-16
==================

  * Added option for per-subreddit selftext previews
  * Added remove post history and remove sub history buttons to History Settings
  * Added support for setting default comment sorting per subreddit
  * Made image previews work for non-image types
  * Made scrolling smoother with a change to the UIL config

5.2.0 / 2016-04-16
==================

  * Added a light font option to comments
  * Added translations for font sizes, inbox tabs, moderation tabs
  * Changed 'View' on the update snackbar to 'Changelog'
  * Enabled clicking links in selftext previews
  * Fixed crash with a 0 length thumbnail set value
  * Fixed some bugs with data saving mode
  * Fixed some issues with album buttons being stuck to the top
  * Restart Slide if pro or the Video plugin was installed

5.1.7 / 2016-04-15
==================

  * 'Go to subreddit' now sorts by subscribed items first, then history, then suggestions
  * Added download and open externally button to albums
  * Added history for direct comment links
  * Added mark all as read option to the inbox
  * Added support for right to left languages
  * Always show big previews if it's a media type link
  * Better handling of image previews
  * Changed TextViews to Buttons in edit comment view
  * Deeper integration with the Slide video plugin (always open with that if possible, despite external settings)
  * Fixed bug opening moderation before it's loaded
  * Fixed crash scrolling in Shadowbox
  * Fixed embedded links not loading
  * Fixed filtering content in the main view not filtering all posts out
  * Fixed Go to subreddit not working correctly with mixed/upper case input
  * Fixed graphical issues with text selection in reply area and edit comment
  * Fixed images getting out of order in comma separated Imgur album
  * Fixed opening links with capitalised schemes (Https rather than https)
  * Fixed some shadowbox crashes
  * Fixed tab highlight color on non-submission pages, Bug fixes
  * Follow Reddit's comment sorting selection order
  * If a item has only been gilded once, '1' is no longer shown next to the star
  * Moved Settings back to bottom in drawer
  * New bottom sheet on album images for better readability and UX
  * Reddit live links now open in an internal browser
  * Settings that aren't applicable are now shown as disabled rather than disappear
  * Sidebar buttons now use subreddit's accent
  * Some shadowbox gif loading fixes
  * Tag external links with the application name that opens them
  * Updated translations

5.1.6 / 2016-04-12
==================

  * Added a history section of the profile which has a list of all previously visited links
  * Added an option for a right-handed comment menu
  * Added support for a 'guest' account and reordering subreddits as a non logged-in user
  * Added support for comma separated Imgur albums
  * Added support for single link spoilers (without spoiler text)
  * Fixed a crash getting multireddits
  * Fixed any reddit links containing "t3" not opening
  * Fixed negative scores when downvoting
  * Fixed not loading Imgur links
  * Fixed synccit posts showing as unread upon a cold boot until a refresh was performed
  * Improved layout of FAB's for tablets
  * Improved settings organization
  * Improved the layout for replying to comments/submissions
  * Made HasSeen.addSeen store the current time for an ordered History screen
  * Made the subreddit list stick between a logged out state and when the user logs in for the first time
  * Made the theme colors extend to popups and dialogs
  * Tweaked the layout for listing accounts

  5.1.5 / 2016-04-11
  ==================

    * Added support for modmail checks
    * Fixed collapsing comments becoming uncollapsed
    * Fixed crashing KitKat loading in drawables by downgrading Gradle and the appcompat versions
    * Hide NSFW selftext previews
    * Some memory usage improvements to the MediaView

5.1.4 / 2016-04-10
==================

  * Add a comment draft if exiting the view before sending and if sending a reply failed
  * Add get title button to submit screen, Gets the URL title if possible
  * Added a new 'Drafts' feature in all the editor toolbars, Can save and restore drafts, and manage saved drafts
  * Added space between score and "pts"
  * Change "pts" to color of vote on comments
  * Externalise more mod actions, over18 prompt, search scope
  * Fix visual issue with some progress dialogs
  * Fixed a voting animation bug
  * Fixed fastscrolling comments skipping some collapsed comments
  * Fixed some issues with suggesting the title
  * Fixes that not all moderated subreddits where shown in the ModQueue Activity
  * Merged link handling and external link handling settings
  * New album icons
  * Save whether user is mod & over 18
  * Share button on profiles
  * Spoilers are now a darker version of the main color
  * Temporarily revert "Prevent a webpage to keep running (with sound) in the background when custom tab is closed"
  * Workaround for crash getting the activity from a context in SpoilerRobotoTextView

5.1.3 / 2016-04-09
==================

  * Added override for viewing a NSFW sub despite your profile settings
  * Added shadow to Settings toolbars
  * Added support for reddituploads.com links
  * Bold score text when voted
  * Changed moderator strings
  * Changed order of operations when voting
  * Check for version number in update popups
  * Don't check for friends if logged out
  * Don't do authenticated related actions if logged out
  * Fixed 'Send to inbox' not working sometimes
  * Fixed moderation not working
  * Fixed sidebar colors on light theme
  * Fixed some filter and external domain issues
  * Fixed some null pointers in authentication
  * Remove unused resources
  * Tap to remove subs from the multi creation screen
  * Updated JRAW, now it doesn't do an extraneous /me request
  * Updated translations

5.1.2 / 2016-04-08
==================

  * Add /r/myrandom to special subreddits and shorten the sort methods
  * Added a default catch in the RecyclerView LayoutManagers for IndexOutOfBoundsException (fixes a crash while changing the dataset and scrolling in a different thread)
  * Changed moderator icon in sidebar
  * Edited the view styles a little bit, added more contrast between the title and the info
  * Fixed clicking items in the sidebar not working
  * Fixed crash on KitKat opening the sidebar
  * Fixed gallery links in Shadowbox
  * Fixed opening externally for viewpager posts
  * Fixed some imgur issues in the MediaView
  * Heavily modified the authentication workflow, now it uses cached authentication data if possible (if the token isn't expired), Loading should be much faster and the calls are in a more logical order
  * Made album grid view use smaller image sizes
  * Made overflow Refresh action consistent
  * Made the inbox count, multireddits, moderated subs, and friends list load after loading the initial subreddit
  * Made the SubsamplingScaleImageView release the bitmap on the MediaFragment destruction, Switched to the MediaFragment for the rest of the Shadowbox links
  * Make domain filter and open external fields textUri inputs
  * Make the order of the overflow menu for direct viewed subreddits closer to that of a normal subreddit
  * Redesigned sidebar with flat buttons
  * Removed tooltips for sidebar buttons
  * Updated gradle

5.1.1 / 2016-04-07
==================

  * Made the inbox count, multireddits, moderated subs, and friends list load after loading the initial subreddit
  * Removed debug message
  * Use only top 500 safe for work subreddits in suggestions

5.1.0 / 2016-04-07
==================

  * Added a reply FAB to comments
  * Added frontpage and all to first start not logged in
  * Added search for multireddits
  * Added support for reddituploads.com images
  * Collapse comments when long pressing on a highlighted comment
  * Cut start-up time by many API calls (down 3 seconds in my testing)
  * Externalise and tweak strings
  * Fixed build error related to album_image.xml
  * Fixed crash creating a top-level reply
  * Fixed images flashing in subreddit sidebar
  * Fixed inserting images on the comment pane layout
  * Fixed link syncing with Reddit gold
  * Fixed many Shadowbox issues
  * Fixed some issues with navigating comments and showing the parent comment
  * Fixed some issues with the AsyncSaveTask
  * Fixed some issues with the Tutorial activity and colors
  * Fixed the activity restarting if no visual changes are made in settings
  * Hid the body text if it was just a newline in the card paragraph mode, Fixed filtering offset not working
  * Hide the FAB if replying to a comment
  * Made comments not lose collapsed state after commenting
  * Made replying to comments not reload the whole comment view, and instead insert into the current dataset (using my latest change on /ccrama/JRAW)
  * Made seekbar not show in Shadowbox mode
  * Made the Snackbar text always white
  * Replace hardcoded '/sdcard' references
  * Set FLAG_ACTIVITY_NO_HISTORY on the Chrome CustomTab Intent prior to launch. This prevents the webpage to keep running when the Activity is moved to the background.
  * Updated Gradle
  * Updated translations

5.0.4 / 2016-04-05
==================

  * Added a catch if the header height is 0 while laying out the top spacer in the Submission view
  * Added a preview markdown button to all send fields
  * Added a tiny font option in Font Settings
  * Added an announcement system which checks for stickied posts marked as 'Announcement' on /r/slideforreddit on first open
  * Added clear button to the drawer text field
  * Added setting for default comment sorting
  * Added the ability to distinguish comments and sticky comments
  * Added the new menu style to the album view images/gifs
  * Cleaned up login layout
  * Fixed album height jumping on scroll
  * Fixed crash if image is too large to display correctly
  * Fixed crash opening comments
  * Fixed gif loading issue in album view
  * Fixed images not loading first time when clicking from comments
  * Fixed inbox crash
  * Fixed incorrect font color on light theme comment menu
  * Fixed loading m.imgur.com links
  * Fixed offline mode comments not loading
  * Fixed opening file in the backup activity
  * Fixed PTR offset in comments for np.reddit links
  * Fixed sharing images directly not working
  * Fixed some hiding post bugs
  * Fixed zooming in internal browser
  * Lots of theme fixes for KitKat
  * Made Imgur content follow the SettingValues.image setting (follows whether to show images in app or not)
  * Made loading into the zooming state as fast as loading into the image state
  * Made sub history follow the Settings > History settings (and only save if the sub exists)
  * Made the announcement dialog into its own activity
  * Made the comment nav bar hide when typing a reply
  * Made the MediaView load into the zooming state by default and removed the option to load into the non-zooming state first
  * Made your subscribed subs add as history subs automatically
  * Moved the share image and share link buttons from a popup to the 3 dot menu in the MediaView
  * New AMOLED theme with more contrast
  * Now replying to a comment saves the state, and you can scroll up and down without losing your input
  * Reduced memory usage saving submissions
  * Sped up view scrolling speed (removed resetViewBeforeLoading from the UIL config)
  * Updated black/white icons for widgets
  * Use suggested comment sort for submissions from a list

5.0.3 / 2016-03-31
==================

  * Added internal Vid.me support
  * Added new buttons to the sidebar
  * Added tooltips to the sidebar buttons
  * Changed to Imgur API v3
  * Fix not being able to open comment menu
  * Fixed comment layout when comment is fully collapsed and then expanded
  * Fixed some z axis issues on KitKat

5.0.2 / 2016-03-30
==================

  * Account for code.reddit.com
  * Accurate SwipeRefreshLayout offset for all devices
  * Added a bottomsheet with actions to the Inbox
  * Added an inbox count snackbar if you have new messages since last visit
  * Added setting for first paragraph of selftext on submissions
  * Added setting to change image/gif save location
  * Added support for showing friends
  * Allow adding 21 character subreddits
  * Allow typing /r/reddit.com
  * Collapse parent comments regardless of settings
  * Correct offset for pull-to-refresh in SubmissionsView
  * Enables 'help' wiki links and reddit.com/wiki
  * Externalise and tweak some strings
  * Fixed black background on single submissions
  * Fixed failing test, add more content/link type tests
  * Fixed hiding sometimes ghosting the current view
  * Fixed login not working
  * Fixed lots of bugs with the new swiping system and the sidebar being populated with the wrong list of subs
  * Fixed moving parent comments after collapsing some
  * Fixed name of overflow menu item
  * Fixed navigating comments not working
  * Fixed NSFW Imgur links not being tagged correctly
  * Fixed posts not opening
  * Fixed removing all accounts keeping you logged in
  * Fixed some inconsistencies with the new swipe system, now it should look and act like the old system!
  * Insert new subreddits into sorted order if list was already sorted
  * Made CommentsScreen and SubredditView use a new swipe layout (using a ViewPager) instead of SwipeViewLayout in order to better handle scrolling
  * Made opening non-subscribed subreddits save history for later visiting
  * Made paragraph in card mode default off
  * Made swiping to exit comments and single subreddit look and act more like the SwipeBackLayout system
  * Made the submission vote and save animation area smaller (more stability)
  * More improvements to the new swipe to close system
  * Pull to refresh offset now starts behind toolbar in all views
  * Recoded the SubredditStorage to not use static arrays and work much faster
  * Setting for collapsing comments by default
  * Stricter URL content type checks
  * Tweaked design of profiles (font and vote color)
  * Tweaked the edit submission design (now it's a bottom sheet)
  * Updated translations

5.0.1 / 2016-03-26
==================

  * Added option to override default language
  * Added option to show the domain name in the info bar
  * LOTS of bug fixes and improvements

5.0.0 / 2016-03-25
================

  * Added /r/mod modqueue to the ModQueue activity
  * Added a 'Largest' font size for comments and submissions
  * Added a 'Show content' button to the comments screen (view content at any time)
  * Added a 'Show parent comment' button to comment menu
  * Added a Data saving section to Settings
  * Added a discover screen with popular and trending subs
  * added a flavor for no gplay version
  * Added a MediaFragment to replace the ImageFull and Gif fragments
  * Added a moderators button to the subreddit sidebar
  * Added a search button in Discover to search for subs by name or topic
  * Added a small content tag in info bar if big pic is disabled
  * Added ability to backup and restore from file (in addition to Google Drive)
  * Added ability to edit submissions
  * Added ability to force full comment view in Multicolumn settings
  * Added album titles to the text popup on the AlbumPager
  * Added an option to show more items in the subreddit toolbar
  * Added approved tag and who approved to submissions in subs you moderate
  * Added autocomplete for your subscriptions in the Submit screen
  * Added better detection of spoiler links (should support all common ones now)
  * Added editor actions to comment edit screen
  * Added grid popup for albums
  * Added method in OpenRedditLink to just show comments without option to load more
  * Added mod tools to comments
  * Added option for smaller and less obtrusive content tag on big images
  * Added option to hide header image on selftext submissions
  * Added SettingsHistory activity.
  * Added sorting to profiles
  * Added submit button to the toolbar overflow
  * Added support for 'Continue this thread'
  * Added support for flairing submissions
  * Added support for reporting comments and submissions
  * Added support for some Reddit preferences in Settings > Reddit preferences. Included is option to show thumbnails, nsfw content, and set the thumbnail type
  * Added trophies, gold expiry, and user since to the Profile info button
  * Added unmoderated panel to the Modqueue activity
  * Added user tagging
  * Addeed a new 'MediaView' which handles images, gifs, and imgur content. It displays the initial image while doing imgur checks and if it's a gif, it loads the gif or does nothing if it's an image. This saves data and makes loading content much faster
  * Changed SettingsTheme activity to use a triple choice option for subreddit tinting
  * Changed the PopulateSubmissionViewHolder method of setting info (switched to a SpannableStringBuilder, added color tags and user tags)
  * Check content type on PNG images (in case it's a gif)
  * Fixed /r/all and frontpage not being added to sub list on first load of subs
  * Fixed a big content logic bug with loading images
  * Fixed archived and locked status not showing up for direct comment links
  * Fixed black background on Synccit settings
  * Fixed choppyness in the comment swipe layout
  * Fixed comment fragment being destroyed on re-open
  * Fixed crash if search parameter contained a `:`
  * Fixed crash issues and data issues with the ModPage fragment and adapter
  * Fixed crash opening the last sub on comment swipe layout
  * Fixed crash starting first time
  * Fixed dark background in History settings
  * Fixed dataset not being changed when changing accounts
  * Fixed duplicate setting in General settings
  * Fixed flickering returning to submission view
  * Fixed i.imgur.com links opening webview on top
  * Fixed incorrect RoundedBackgroundSpan sizes
  * Fixed lag in MediaView
  * Fixed MediaView not tiling images (led to RAM use and speed issues)
  * Fixed missing content tags for some Imgur links
  * Fixed multireddit sorting
  * Fixed newline in whatsnew.xml
  * Fixed not selecting the correct tab on return from settings or sorting
  * Fixed ordered lists not showing without an unordered list present
  * Fixed posts getting out of order if hiding while loading more data
  * Fixed progress bar not showing in albums
  * Fixed reporting of comments
  * Fixed some bugs with the comment parent dialog
  * Fixed some bugs with the profile info dialog
  * Fixed some crashes
  * Fixed some galleries not showing more than 10 images
  * Fixed some images not opening
  * Fixed some incorrect content tags
  * Fixed some more bugs with the MediaView and MediaFragment
  * Fixed some Settings toolbar styles
  * Fixed sorting on multis and subreddits, Updated JRAW version to fix sorting of non-submission paginators
  * Fixed spacing in ModPage RecyclerView
  * Fixed the ripple type on media view buttons
  * Fixed threading issues with mark posts as read on scroll turned on
  * Fixed thumbnail overlay on gifs
  * Fixed visibility of smaller content tag on lightly colored backgrounds
  * Go to correct position tapping the grid item in the vertical album view
  * Handle tagging NSFW posts correctly and cleaned up ContentType
  * Made AMOLED background a tad darker
  * Made hide NSFW content work per-account
  * Made history settings work in the SubmissionView as well
  * Made it so you can't sort /r/friends (reddit limitation)
  * Made light theme text a bit lighter
  * Made linking to specific wiki pages work, Added support for /w wiki urls
  * Made links work in album descriptions
  * Made MediaView and MediaFragment play video onResume
  * Made posts show as read correctly coming back from comments
  * Made Slide load 50 submissions by default instead of 25 (alleviates the 'No more posts' issue)
  * Made submission reload on comment refresh
  * Made the refresh button and spacer height use the correct offset height value in comments
  * Made the SubsamplingScaleImageView switch to a different ImageRegionDecoder if 'Image failed to decode using JPEG decoder'
  * Made view type a multi-choice item in General Settings
  * Make surrounding spaces inside flairs nonbreaking
  * More improvements to comment reports
  * Moved comment count settings to Post Layout settings
  * Much better swipe logic for comment swipe layout
  * New comment menu with more options
  * Re-introduced color sub name only
  * Removed in-app handling of NSFW previews, instead make it follow the Reddit default. This allows it to be per-account and interfaces better with Reddit.com
  * Show selftext in 'Show content' in the CommentPage fragment
  * Support for locked submissions
  * Support links with custom search parameters
  * Switched to new code to fully kill and restart Slide using ProcessPhoenix, Should fix some issues after login and switching accounts
  * Table autosync for now
  * Tentative fix for lag in some images
  * Tons of bug fixes and improvements
  * Totally revamped submission mod tools, No longer crashes and now utilizes the bottom sheet
  * Updated the dark theme

4.7.5 / 2016-03-20
==================

  * Added animations to the flip icons in the drawer
  * Added more options to the sidebar under Profile
  * Added support for Streamable.com content piggybacking off of the GifView activity
  * Changed the icon in drawables to the new icon
  * Crash fixes
  * Fixed some index issues in Comments and Submissions
  * Fixed some streamable issues
  * Made clicking Profile in sidebar go straight to the Profile activity
  * More powerful Multireddit editor, Can now add un-subscribed subs to Multireddits
  * New icon is here
  * Removed old icons from drawables
  * Support for /f spoilers
  * Updated translations

4.7.4 / 2016-03-12
==================

  * Added a reload button to the 'No more posts' footer
  * Added setting to always open certain domains externally, ignoring other link handling settings
  * Added sidebar button to MainActivity
  * Comment, Wiki, and Submission bug fixes and improvements
  * Fixed double selection on sidebar button in MainActivity
  * Fixed FAB not showing in reorder screen if FABs are disabled
  * Fixed inability to remove domains from the always open externally Settings screen
  * Fixes to the Customtabs util
  * Hide sidebars in /r/mod and /r/friends
  * Improved comment color tags, Moved op, mod, and you tags to the author text
  * Lots of bug fixes and improvements
  * Made tags centered and look better, Merged the other spannables into the RoundedBackgroundSpan
  * Made the save button in comments and submissions show the correct state (save or unsave)
  * Promote special subreddits to the top of the list when sorting
  * Re-ordered search dialog buttons
  * Redesigned comment menu using BottomSheets
  * Removed the tooltip tutorial, Replaced it with a swipe tutorial for content (images, gifs, albums) and a different swipe tutorial for comments (more info about enabling swipe from anywhere)
  * Use default share in chrome custom tabs, open custom tabs directly if package available

4.7.3.2 / 2016-03-12
====================

  * Bugfixes, Fixed embedded content opening twice

4.7.3.1 / 2016-03-11
====================

  * Added a comments since last seen feature
  * Better handling of finding resource value in CommentOverflow
  * Converted the multiple views used for tags into spannables that look like tags (faster inflation and smoother scrolling)
  * Fix streamables opening externally
  * Fixed crash on some galleries
  * Fixed cut off 'PINNED' tag
  * Fixed description button in the AlbumPager
  * Fixed filters not re-applying after leaving the Filter Settings screen
  * Fixed heights of some tags
  * Fixed issue with tables in CommentOverflow
  * Fixed some cut off text on submission views
  * Improved album view
  * Improved gif view in album pager
  * Improvements to the submission views
  * Made buttons in AlbumPager work for the image fragment
  * Made clicking the image in a notification work
  * Made comment last visit count a setting
  * Made CommentsScreen handle orientation instead of getting it through intent
  * Made gild flair same height as the other flairs
  * Made Multireddit adapter and fragment work closer to how the Submission adapter and fragment work
  * Made Multireddits and Single Subreddit views rotate correctly
  * Made the comment view use only one TextView for the title and created the rest of the formatting with spans, Some more improvement to submission tag spans
  * Made the CommentOverflow class use the correct theme attributes
  * Merged CommentsScreenPopup with CommentsScreen
  * More album fixes
  * More comment tag fixes
  * Start of widget code for the future

4.7.2 / 2016-03-10
==================

  * Better direct linking to context support
  * Fixed clearing seen posts clearing the wrong data
  * Fixed filters filtering all posts out of a value is a space or newline
  * Fixed inability to open direct links
  * Fixed some back buttons not working, Made caching comments cancellable
  * Hopeful fix for the slow scrolling issue and high CPU usage
  * Lots of bug fixes
  * Lots of improvements to the CommentsScreen code and adapter, attempt to fix scrolling lag
  * Many comment view improvements
  * More back arrow and comment screen fixes
  * Much improved filter screen
  * Removed Video handler settings
  * Some code reformatting
  * Start of major improvements to scrolling smoothness in comments

4.7.0 / 2016-03-08
==================

  * Added a 'quick filter' button to the overflow menu, Now you can filter submissions by content type on a per-subreddit basis
  * Added a better animation to submissions views
  * Added a compose message button to inbox
  * Added a new 'Dark Blue' base theme
  * Added a reset button to the sub theme dialog that resets all the customization values (also works with multiple select)
  * Added a setting for low res image previews to save data
  * Added animation for hiding/showing children comments,
  * Added animations for voting
  * Added animations to full post submission view
  * Added checkbox item to the overflow menu to override showing big pics in a sub
  * Added content activity entrance animation
  * Added debug build target
  * Added edit time and controversial status to comments
  * Added editor actions to the send message screen
  * Added fab to SubredditView
  * Added long press to show actionbar setting
  * Added multi select to reorder subreddit screen
  * Added new library for use in emote processing.
  * Added new post style to fullscreen submission view
  * Added setting option to switch thumbnail position
  * Added style to Post Layout settings, Now uses multi select popups and more consise options for less confusion and more direct customization
  * Added submit button to navigation drawer under Profile
  * Added support for /c spoilers
  * Added tap to go to top for MainActivity, Multireddits, and SubredditView
  * Added TeslaUnread support for messages
  * Added title to url when sharing a submission
  * Added vote states to replace the bad voting code based on the submission objects. Now it respects changes between activities and is much cleaner
  * Added voting flash animation
  * Allow user to add subreddits with spaces in them
  * AutoFit text for multireddits displayed in drawer
  * Changed sub theme activity to use the RecyclerView instad of a ListView
  * Complete Shadowbox revamp, New Shadowbox view layouts, New fading animation while zooming and scrolling in Shadowbox, Removed GIF controls from Gif in Shadowbox, Added deepzoom to images in Shadowbox
  * Content activity backgrounds darkened
  * Converted all drawable resources to Vector drawables for smaller APK and less memory use
  * Fixed accent colors not saving
  * Fixed album crash if no layoutmanger is set
  * Fixed alpha on some images getting set to 0.2 on zoom out in Shadowbox
  * Fixed another crash when first starting Slide
  * Fixed back arrow on Multireddits and Search
  * Fixed bottom sheet being the wrong color
  * Fixed bottom sheet style not matching the base theme
  * Fixed bug changing the amber accent color and the dark blue theme
  * Fixed comment color disappearing on full hide
  * Fixed comment screen crashing for some users due to a view that is too big to animate
  * Fixed comment scrolling arrows (made them work again)
  * Fixed crash changing subreddit theme from the overflow menu
  * Fixed crash if checking for filter on the frontpage
  * Fixed crash in album view of Shadowbox
  * Fixed crash opening sub theme activity
  * Fixed crash opening the Submit activity
  * Fixed crash when first starting Slide
  * Fixed crashing if trying to instantiate a fragment which doesn't have data yet in the MainActivity
  * Fixed double comments if collapse fully is enabled
  * Fixed duplicate posts in main view
  * Fixed duplication of moving items to top in sort screen
  * Fixed extra spacing in recyclerview after updating support libraries
  * Fixed extra spacing on Search activity, Fixed toolbar not hiding in search results, Made FAB autohide in Subreddit Settings
  * Fixed filters (and switched away from regex which was a bit overkill)
  * Fixed first items in submission view being laggy
  * Fixed height of item separators in settings
  * Fixed height of views in Multireddit editor
  * Fixed incorrect menu in Link Handling settings
  * Fixed incorrect thumbnail placement if long press actionbar is enabled
  * Fixed multicolumn and configuration changes getting ignored
  * Fixed out of bounds error with CommentsScreenPopup
  * Fixed rotation crash
  * Fixed save button not working in full view
  * Fixed some album issues in Shadowbox
  * Fixed some image saving logic flow fixes
  * Fixed some lag in list view
  * Fixed some more spacing issues in Search's toolbar
  * Fixed some settings being disabled because of old code
  * Fixed sorting always showing hot
  * Fixed spacing issues on right side of toolbar actions in the Inbox Activity
  * Fixed submission view 'jumping' after returning from comments
  * Fixed subreddit list from getting replaced after sort
  * Fixed theme chooser for blue theme
  * Fixed toolbar not working in KitKat
  * Fixed toolbar on top of content on pre-lolipop devices
  * Forced font size in whatsnew.xml
  * HtmlSpanner didn't play well with others, so it's now removed and implemented code by searching for existing converted Spans.
  * Imgur gifs are no longer transcribed through Gfycat
  * Improved loading of data from the FragmentStatePagerAdapter (no more instantiating fragments)
  * Improved scrolling FPS in submission views
  * Improved the submission BottomSheet
  * Improvements to Font settings screen
  * Improvements to sorting mode switcher
  * Lots of scrolling smoothness improvements
  * Made a sub show up in the SubredditSettings page if it has the big pics value set
  * Made actionbar collapse after voting, saving, or hiding
  * Made actionbar show/hide animate in/out
  * Made big pic cropped setting work
  * Made browser select menu work
  * Made button to change subreddit theme in the toolbar overflow menu
  * Made centered image setting work
  * Made comment scrolling instant instead of slow scrolling
  * Made donation view scrollable
  * Made FAB in compose message screen not cover up the text
  * Made fab setting in General settings work with a popup
  * Made HasSeen respect gold viewed posts as well, Bug fixes
  * Made image links check the mime type from the content header before loading if not from Imgur or Reddit, and open the appropriate view if not an image
  * Made images and title turn opaque instead of the whole view
  * Made it possible to set the big pics enabled for multiple subs
  * Made list actionbar items correct height when actionbar is toggled
  * Made list view less cramped
  * Made popup in toolbar use the current theme style
  * Made reply area expand out in main post of comments view
  * Made returning only refresh the clicked view instead of animating all current items in the recyclerview
  * Made save and hide buttons behave properly in edit layout screen
  * Made saving work across activities using the new ActionStates class
  * Made Search toolbar autocollapse (for real this time)
  * Made Slide ask the user to choose save location for images and gifs (will fix all the content not saving issues)
  * Made tapping the thumbnail turn the content opaque like the head image
  * Made the correct dialogs show in gif error messages
  * Made the default notification time 6 hours instead of 1 hour
  * Made the FullscreenImage view use a new UIL configuration to fix resolution issues
  * Made the gif cache stay at or under 50 mb
  * Made the link insert EditText font readable on Light theme
  * Made the url unescape if loading half-quality images
  * Made toggle dark theme button open a base theme dialog with all four theme options
  * Many changes and improvements to various setting layouts and logic
  * More accurate fragment handling in MainActivity
  * More concise names for theme options in overflow menu
  * Moved list and center image cards to the new layout
  * Moved set big pics enabled for this sub to the sub theme dialog box
  * Moved to a new bottom sheet dialog for submission overflow menu
  * Multi select improvements in reorder screen
  * Re-added support for hiding the actionbar, Submission views now have a 3 dot menu to enable/disable the actionbar if it's hidden
  * Re-formatted Settings and General Settings layout to be more in line with standard preference styles, Removed some extraneous settings
  * Redid logic on how the SubmissionsView fragment should choose wether to load content or remain in a loading state
  * Refresh recyclerview when returning from comments screen (updates vote and seen status)
  * Remove youtube player .jar, open normally instead.
  * Removed alphabetical subreddit list
  * Removed alphabetical subreddit sidebar option
  * Removed option to disable animations in the main layout
  * Removed setting option to hide actionbar
  * Removed some niche or uneccessary settings
  * Removed some unused files and resources
  * Revamped look of Comment settings
  * Revert "AutoFit text for multireddits displayed in drawer"
  * Scrolling improvements, View flattening
  * Slide now only loads the currently vibile sub (less data use, less lag when populating intial data)
  * Some text value improvements for the subreddit theme dialog
  * Support loading custom sub emotes from storage. Only tested with http://dinsfire.com/projects/reddit-emotes/
  * Update translations
  * When a comment is collapsed, the user can single tap on it to expand the comment if they have swap == true
  * You can now share links and images to Slide for submitting, Fab in submit screen doesn't cover the text or editor anymore

4.6.2 / 2016-02-23
==================

  * New account switcher in sidebar
  * Added deeper zooming in image view
  * Added open external button to images and gifs
  * Added padding to bottom of scrollable content in text (scrollbar was overlapping the content)
  * Added animation to comment menu
  * Made album header transparent
  * Fixed some crashes with nested unordered lists
  * Fixed replyarea height
  * Fixed comment crash
  * Fixed loading when swiping through multireddits
  * Fixed crash when text starts with strikethrough
  * Fixed navigating up in comments
  * Improvements to the Subreddit and Multireddit adapters
  * Fixed more RecyclerView adapter crashes
  * Fixed crash in the inbox
  * Fixed inbox always showing loading icon even if reached the bottom
  * Fixed all applicable recyclers crashing because of invalid adapter positions
  * Fixed account switching, Now selects other account if logging out of current account
  * Fixed RefreshLayout offsets
  * Start of fix for name scrunching issue in comments
  * Fixes to CommentScreenPopup
  * Fixed posts going into toolbar in multicolumn mode
  * Fixed some blurry images
  * Fixed adding friend or mod to reorder screen, Ignore case on friend and mod

4.6.1 / 2016-02-17
==================

  * Added a dot seperator for title items in comment view
  * Added a menu on long press of thumbnails or big pictures, to share/copy the link address
  * Added option to make comment depth indicators monochrome instead of colourful
  * Added Synccit support - shared history accross devices
  * Added the ability to scroll using the volume keys
  * Fixed a bug with removing accounts
  * Fixed comment count bubble size
  * Fixed comments not loading if you click on the main link before they're loaded fully
  * Fixed links not working in private messages
  * Fixed shortcut creation
  * Fixed spoilers not working
  * Fixed submissions not showing images when the view is first created and when not cropping image height
  * Fixed the 'gif not found' issue
  * Hide post only for signed in users (hiding requires the user to be logged in)
  * Improved html parsing for album item titles and descriptions
  * Improved YouTube hash extraction using regex
  * Made album captions scrollable
  * Made back button work on single sub view
  * Made comment menu close after voting
  * Made link click handling work without a subreddit name
  * Made search screen use sub colors
  * Made submission card views hardware accelerated
  * Made the BottomSheet dialog match the parent theme
  * Made the useragent use the current Slide build version
  * Major logic fixes in HeaderImageLinkView (fixes default album text being shown for some links, fixes big image showing but thumbnail area having text), Direct Reddit link crash fix, Various other bug fixes
  * More post image logic changes
  * Reduced the amount of .me() calls on the Reddit API
  * Removed always show cached data option and cache settings
  * Removed overdraw on post views
  * Search default time period set to all time, Subtitle telling user the time period
  * Testing out deeper zoom in image view

4.6.0 / 2016-02-14
==================

  * Large performance improvements
    * Flattened views in both the main and comments views for scrolling performance
    * Rewrote offline mode
  * Added a new tutorial on startup using tooltips to guide the user
  * Added collections in the reorder subreddits screen
  * Added scrolling code blocks
  * Added strike-through
  * Added ordered and unordered lists
  * Added tables
  * Added a YouTube video view (via the YouTube Player API) to replace embedded content YouTube videos
  * Added shadowbox mode to multireddits
  * Added floating action button for multireddits
  * Added an option to colour subreddits automatically based on their preferences
  * Added a new sidebar header for offline mode (instead of non logged in header)
  * Added a setting option for the Horizontal Album view
  * Added Mark as read action to notification
  * Added option for solid image viewer background
  * Moved 'reorder subreddits' from the drawer to settings
  * Delayed showing progress bar on image viewer for 500 ms to stop it from flashing on quickly loading images.
  * Simplified the reorder subreddits settings, with some new actions on long press
  * Tweaked strings in settings (capitalisation, typos)
  * Removed double menus from multireddits
  * Fixed padding on cards and list
  * Fixed strange animations on the Settings activity
  * Fixed card mode text wrapping fixes
  * Fixed list mode missing divider
  * Fixed some comment pages not opening
  * Fixed main activity and comments memory leak
  * Fixed some single item album issues
  * Fixed settings activity transitions
  * Fixed Custom Tabs flashing transition
  * Fixed view creation for edit cards
  * Fixed some one image gallery issues
  * Fixed alarms firing at the same time and causing massive Reddit traffic over Marshmallow devices
  * Fixed update message always popping up
  * Fixed shadows on cards being cut off
  * Fixed shadowbox crash while trying to access the OfflineSubreddit
  * Fixed crash on homescreen when not logged in
  * Fixed multiple code blocks not rendering
  * Fixed blank sub issue in ReorderSubreddits
  * Fixed some issues with NSFW previews

4.5.1 / 2016-01-14
==================

  * Made saving gifs work
  * Small tint changes
  * Fixed filters not scrolling properly
  * Self posts no longer show a content URL when sharing
  * Disabled image-in-middle-of-card option when big picture is disabled
  * Fixed contrast issues with font colors for the most part
   * Made sidebar buttons untinted
   * Removed the lighest color variants
   * Made inflated dialog titles white
  * Made sharing link text use the util in Reddit.class
  * Fixed link sharing on internal webview
  * Fixed some card background issues
  * Made borderless card buttons on v21 and above
  * Made youtube links work from comments again
  * Added color card background matching mode to theme settings and applicable methods in CreateCardView
  * Cleanup of saving code
  * Redid the multicolumn dialog
   * Added option for dual portrait columns
  * Re-organized the edit layout activity
  * Added save from actionbar button
  * Fixed wrong color text in insert link dialog
  * Added filter system with support for the SubredditPosts dataset
   * Added settings option for filters
   * Created a helper class to create the Regex pattern from a comma separated string and matching based on title, body, or domain
  * Added progress spinner to load more comments view
  * Started working on horizontal view for Albums
  * Fixed padding between title and thumbnail
  * Fixed flash when loading/reloading comments
  * Added a no thumbnail preview if there are no associated images or previews
  * Made selftext use the same font size as comments
  * Normalized Large and Larger font sizes
  * Added 2 new font sizes, Added 3 new font options for post titles
  * Made font sizes work again for submission titles
  * Implement long click on links within comments for additional actions
  * Re-added image previews to fullscreen post view with selftext
  * Made submissions load if cache is set to always use cached data but no data is stored for that subreddit
  * Added titles and description support to imgur galleries
  * Made app totally log out if all accounts are removed
  * Made switching accounts work
  * Made login open new account
  * Fixed blank page on crash or switching accounts
  * Added dialog to remove acconts (log out)
  * Make saving comments actually save, not unsave
  * Updated to JRAW v0.8
  * Added comment saving
  * Made failed album requests open in browser
  * Fixed crash when hiding all posts in a subreddit
  * Made comment screen use full sized image if possible
  * Complete redo of card and list modes
  * Changed options for card layouts and changed the setting activity respectively
  * New thumbnail for nsfw posts
  * Replace "Add friend" and "Remove friend" with XML constants
  * Removed all instances of ActiveTextView from Slide
  * Added setting to hide the header on the navbar
  * Fixed crash going back to MainActivity from Inbox
  * Added message count bubble in sidebar
  * Fixed comments sometimes not loading after replying
  * Fixed crash when removing friend
  * Added friending from the profile screen
  * Started working on reset button from edit cards layout not working
  * Moved startup_* strings to a <string-array>
  * Fixed sharing images
  * Fixed not being able to open sidebar in single sub view
  * Rename some settings in accordance with removal of others
  * Made title font choices thicker
  * Start of comment style revamp
  * Added a setting page for comment and post title fonts
  * Rewrote me.ccrama.redditslide.util.NetworkUtil
  * Complete wiki overhaul, much faster and more reliable and gives the user more indication of loading progress
  * Recover if SubredditStorage.subredditsForHome is removed from memory
  * Fixed crash when opening search links
  * Changed the view animation to an adapter one (no more glitchy movements)
  * Fixed pull to refresh crash, Fixed double drawer on first open
  * Fixed changing of ViewDragHelper size when returning to activity (made override public not static)
  * More elegant way of setting view visibility

4.5 / 2016-01-09
================

  * Few tweaks to enter animation
  * Changed enter animation to be more subtle
  * Added override for blur check
  * Made gfycats load with caching as well
  * Added gif caching, Made offline comments work if the user cached posts, Made app start correctly if offline and no instance of Reddit or Authentication exists
  * Change version code for pre-alpha 3
  * Improved restart code
  * Fixed accumulation of old saved data in Drive backup
  * Re-added FAB settings, Made an override for some activities which should always have swipe away from anywhere (images, gifs, albums, etc), Hid Reddit settings if not logged in
  * Fixed authentication when not logged in, fixed blank activity on first open with no data (not logged in), Re-added the tutorial screen (somehow got removed), Sped up startup even more (got rid of loading activity), Fixed opening from background for no reason, Fixed crash when removing actionbar on list mode
  * Made 'Go Pro' go to the pro page instead of donate screen
  * Included SwipeBackLayout resources, Made an override for some activities which shouldn't slide from anywhere
  * Implemented @ikew0ng's code into Slide so I could intercept the ViewDragHelper and make swipe anywhere work
  * Fixed wrong VideoView being used in the gif activity
  * Fixed black backgrounds on light theme, Fixed original header color in drawer to match the first subreddit on MainActivity start
  * Improved center image on largecard_middle
  * Fixed list mode, crash on multireddits, made Reddit Preferences work, updated version numbers
  * Re added setting for animations in General Settings, disabled swipe from anywhere for now
  * Added option to mark posts as read on scroll
  * Re-added NSFW filtering because reddit returns nsfw posts if the subreddit is nsfw
  * Removed redundant NSFW checking code (now handled by the reddit api)
  * Fixed crash for cache settings
  * Added reddit preferences for NSFW posts and previews, syncs from reddit.com/preferences. Various other fixes
  * Added a secondary browser to Chrome Customtabs and an option in Link Handling settings to enable/disable it
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Making swipe anywhere work, fixed images not always loading in FullImageView
  * various fixes for the multi select (accent color)
  * More settings changes
  * Start of total settings revamp and simplification
  * Merge pull request #746 from thatJavaNerd/patch-1
  * Simplify logic in Reddit.isPackageInstalled
  * bug fixes, multi select for settings subtheme
  * Make gifs cache to storage, load gifs from storage with loading bar
  * Fixes comment double loading
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Made offline mode work again
  * Merge pull request #742 from KonradIT/master
  * Uncommented SecretConstants line, sorry
  * Removed new line symbol from Album view in Imgur
  * Merge pull request #741 from KonradIT/master
  * Moved all strings on cards to the FlowLayout so it takes up one line instead of two
  * Added more start-up strings and thanks upvote string to /r/xdacirclejerk
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Prefetch images from the SubmissionPaginator
  * bug fixes, visual improvements
  * Various things over the last few weeks
  * bug fixes, code refactoring
  * set recent bar title, color & icon; fix #590
  * visual fixes
  * fix padding issues
  * Merge pull request #732 from l3d00m/fixes_and_drawer
  * update strings
  * faster gfy loading
  * move upvotes&saved to profile, add disliked&hidden
  * bug fixes, increase default notif time to 60
  * Update vote count when you vote on posts and submissions
  * Removed test code
  * add a + fab to subreddit settings
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Lots of minor changes/fixes.
  * fixes for chrome custom tabs
  * Merge pull request #725 from Deadleg/fix/settingscrash
  * Merge pull request #724 from l3d00m/extend_baseactivity
  * Fix spoiler link not working
  * show select dialog instead of using chrome
  * extend activities from baseactivity + other fixes
  * display a message for archived posts
  * Merge pull request #722 from rosenpin/fixed-a-stupid-bug
  * Changed icon size
  * Merge pull request #723 from rosenpin/fixed-auto-theme-title-toolbar
  * Fixed auto theme title in toolbar, was 'Cache Settings' for some reason
  * Fixed a stupid bug
  * Merge pull request #719 from l3d00m/settings_update2
  * exclude slide from opening some redddit links
  * advanced settings, various setting fixes
  * Merge pull request #717 from rosenpin/added-reddit-settings
  * Merge pull request #718 from Deadleg/fix/settingscrash
  * Fix crash loading app with disabled notifications
  * Moved new string position
  * Removed unused files
  * Added reddit settings
  * add suport for links that have '?context=' at end
  * update strings from crowdin
  * externalized strings, sorted strings.xml
  * Merge pull request #714 from l3d00m/drawer_md
  * adjust sizes in drawer to follow md
  * Autotheme works for real now
  * Fixed another bug :(
  * Fixed endless loop
  * Fixed wrong layout for SettingsAutonight
  * Started Auto Time theme implementation
  * Totally redid how images are stored in FullscreenImage class
  * Merge pull request #705 from Deadleg/fix/spoilercrash
  * Add spoiler functionality
  * Search works now with a new menu, still rough around the edgesthough
  * Merge pull request #704 from Qbee1337/appcompat_and_gradle_update
  * appcompat-v7-23.1.1 and gradle - update
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Search now works
  * fix shadowbox mode crash
  * Merge pull request #700 from recalculated/menu
  * Remove hardcoded strings, use showAsAction=ifRoom
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Started working on Search, Changed menu items for MainView, Started offline comment code
  * decode html entities
  * Better DP handling from swipe back
  * Swipe back from anywhere on single mode
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bug fixes
  * Merge pull request #695 from recalculated/extend-baseactivity
  * Merge pull request #696 from recalculated/refresh
  * Merge pull request #697 from rosenpin/settings-improvements
  * Some settings would be disabled if they are useless
  * Add refresh button to menu
  * Properly color Profile activity
  * Spell 'Palette' correctly
  * Move color theming logic into BaseActivity
  * Merge pull request #688 from recalculated/status-bar-coloring
  * Properly colorize status bar in comment settings
  * replace checkboxes with switches, rename strings,
  * Removed jar from repo, use jitpack instead
  * Share link fixes, Double comment load fixes, Double auth fix
  * Fixed some regex issues
  * Bugfixes
  * Merge pull request #677 from l3d00m/color
  * Merge pull request #680 from rosenpin/option-for-fullscreen-settings
  * Merge pull request #683 from rosenpin/bottom-share-sheet
  * Removed unused file
  * Updated share dialog
  * Changed vars names to fitting ones
  * Fixed issue with previous commit causeing a checkbox to look like a button
  * Added option in general settings for fullscreen view submiisions links
  * Moved hide post button checkbox from general to layout settings
  * move color picker into colorpreferences
  * remove double margin from comments
  * fix image buttons, fix self post share
  * Merge pull request #670 from recalculated/issue-143
  * Merge pull request #668 from rosenpin/fab-fix
  * Merge pull request #667 from rosenpin/animation-length-settings
  * Allow capital letters
  * Use digits attribute instead of InputFilter (fixes #143)
  * Modified URL handling slightly
  * two small fixes
  * If fab is disabled you can't change the fab type
  * Added an option to change animation length
  * Sub theme chooser improvements
  * Merge pull request #665 from rosenpin/comments-settings
  * album fixes
  * Moved comments settings from general to its place
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bugfixes, Some setting changes
  * Merge pull request #664 from rosenpin/comments-settings
  * Merge pull request #663 from Deadleg/master
  * Merge pull request #662 from recalculated/subdomain-handling
  * Removed file
  * Added comments settings with abillity to enable/disable username click to go to user profile
  * Fix crash when all subreddit posts are loaded
  * Evaluate for permalink before regular comment link
  * Add fixme requesting exclusion of unique reddit subdomains from intent filter
  * Remove/add regex specificity as necessary
  * Handle subreddit.reddit.com links
  * Merge pull request #659 from l3d00m/fix_158_1974
  * remove arab because of missing ltr layout
  * update strings from crowdin
  * add copy text option: #315
  * fixed #148, fixed #197
  * Merge pull request #657 from recalculated/regex-fix
  * Support all language subdomains
  * Fine tune intent handling regex
  *  temp fix #655: voting on archived posts crashes slide
  * Merge pull request #656 from karlding/fix-fastscroll-recyclerview-crash
  * fix RecyclerView IndexOutOfBounds crash when CommentAdapter is empty
  * New card mode (middle card), Some settings changes
  * Merge pull request #645 from l3d00m/submit_sidebar
  * add submit button to sidebar, optimize submit class
  * Merge pull request #638 from Deadleg/master
  * Merge pull request #641 from karlding/fix-invalid-accent-colour
  * README.md: Spelling, formatting, tweaks
  * Merge pull request #637 from cyalins/patch-1
  * fix the light_green accent colour
  * Fix formatting issue with adjacent code blocks
  * Just fixing/rewording a few things in the Readme.md
  * Merge pull request #635 from recalculated/url-handling
  * Fix indenting
  * Use regex for URL handling (fixes #619)
  * readme: Added IRC badge and screenshot request
  * Merge pull request #634 from recalculated/play-services
  * Replace play-services with play-services-drive
  * Merge pull request #633 from recalculated/merge-single-mode
  * Fixed merge conflict
  * More offline mode fixes
  * Combine OverviewBase, SubredditOverview, and SubredditOverviewSingle
  * Massive improvements to offline mode, Settings for cache, Option to always show offline data
  * Merge pull request #626 from l3d00m/settings_crash
  * Merge pull request #627 from rosenpin/fix-crash
  * Fixed a crash, if adapter is null you can't use it..
  * Fixed force close
  * fix crash when changing notifications when not...
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Redid how offline mode is triggered, now only works if data returned is null (aka most likely offline)
  * Merge pull request #617 from rosenpin/settings-for-animations
  * Merge pull request #623 from karlding/fix-default-comment-sorting
  * Merge pull request #624 from karlding/fix-gild-alignment
  * fix guilded icon alignment
  * fix the comment sorting to reflect reddit defaults
  * Added option to turn off reveal animation in general settings
  * Mostly finished offline support
  * Merge pull request #614 from l3d00m/fix_166
  * Merge pull request #616 from rosenpin/circular-reveal-animation
  * Added circular reveal enter transition animation
  * catch a exception that occurs rarely
  * fixes #613
  * fix #166 and remove a space
  * Working on a multi fix
  * Start of offline support
  * Merge pull request #602 from rosenpin/fix-startup-crash
  * Merge pull request #608 from Deadleg/master
  * Fix the removal of all formatting except code
  * Fix crash on load due to IndexOutOfBoundsException
  * Indentation
  * Removed indentation for easier review
  * Fixed startup crash on slow internet connections, plus, minor code indentation
  * Merge pull request #600 from rosenpin/normalized-reorder-pins-activity-fab
  * Changed reorder your pins activity floating action button to standart one
  * Merge pull request #599 from rosenpin/fix-inbox-force-close
  * Fixed force close when opening an item from inbox
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Made initial load WAY faster
  * Merge pull request #595 from l3d00m/settings_573
  * Merge pull request #592 from rosenpin/fixed-multi-fab-inconsistency
  * Merge pull request #594 from rosenpin/finish-after-error
  * make backup dialogs uncancelable
  * fixes #573, update strings, fix crash, ... fix fab settings crash add back arrow to all settings activities fix for #573: disable doubled material ripple align done button on the right
  * Finish on dismiss too
  * If there is no internet connection the app will close after showing the dialog
  * Changed the pro icon back, someone made it a duck...
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * A few minor fixes
  * increment to 4.3.4
  * Merge pull request #588 from rosenpin/fixed-issue-579
  * Solved issu #579
  * ..
  * Fixed multireddit activity floating action inconsistency
  * Fixed force close when clicking the slide view before all content is loaded
  * Merge pull request #575 from Deadleg/master
  * Add monospace formatting to code fragments
  * derp
  * Change some icons, I think
  * Couple string changes

4.3.4 / 2015-11-17
==================

  * Made FAB settings
  * Now uses savedInstanceStates to not lose data after multitasking
  * Lots of changes, Bring up to date
  * Merge pull request #567 from karlding/fix-upvote-colouring
  * Merge pull request #564 from rosenpin/link-click-improvements
  * fixes the upvote/downvote colouring issue (ticket #565)
  * Commented git reporter back
  * Clicking on link/image/gif now marks the post as 'seen', also simplified onlink click for better code maintenance
  * add another string
  * readd two strings
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Edited some theme values
  * Merge pull request #563 from l3d00m/releasess
  * Merge pull request #555 from recalculated/material-colors
  * update strings, various fixes
  * Merge pull request #554 from ihatetothink/share_menu_475
  * Merge pull request #556 from recalculated/gradle-version-fix
  * Merge pull request #557 from ColinChartier/master
  * Merge pull request #560 from recalculated/issue-559
  * Merge pull request #562 from ihatetothink/gild_cards_561
  * more layouts, quite unsure if will look good in any case
  * adapter and viewholder
  * minor layout changes..
  * layouts
  * realised there already was a share function, used that
  * Pull up snackbar if user tries to access notification settings while not logged in
  * Sanitize gfycat json input. Fixes #553
  * Change Gradle plugin version to 1.3.1
  * Share link or image from fullscreen view
  * just didnt build
  * Density specific hide button
  * Change backup icon to a cloud
  * Remove drawables in drawable/ that have copies in drawable-[density]/. They're taking up unneeded space
  * Normalize icon colors, pick better icons for default/alt layout
  * Change light/dark colors to match MD guidelines, and make AMOLED black actually black
  * Merge branch 'settings_rework' of https://github.com/l3d00m/Slide into settings_rework
  * first part of settings rework

4.3.25 / 2015-11-16
===================

  * Fixed timeout on app start for some users
  * Fixing some stuff
  * Merge pull request #551 from rosenpin/better-dismiss-fab
  * Really fixed the issue where not all read posts would dismiss, also long press dimiss fab to hide forever
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Finally done with blank screen glitch
  * Merge pull request #549 from rosenpin/fixed-dismiss-issues
  * Fixed dismiss fab issues
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bugfixes, Return to activity blank screen fixes
  * Merge pull request #545 from l3d00m/minor_fixes
  * url fixes
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bugfixes
  * Merge pull request #544 from mightyfrog/bugfix_flair_vertical_alignment
  * fixed flair vertical alignment
  * Try to fix background issues
  * Merge pull request #543 from rosenpin/fab-dismiss
  * Fixed issue when only some post were removed
  * Fixed issue when only some post were removed
  * Merge pull request #542 from l3d00m/minor_fixes
  * debug apks have another package name than release
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Lots of bug fixes and cleaning up merges
  * Merge pull request #539 from rosenpin/fab-dismiss
  * Added an option in settings to use fab to dismiss viewed posts like in Relay
  * Update README.md
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Fixing JB Support
  * Merge pull request #532 from rosenpin/instant-read
  * Saved a line of code :P
  * Made clicked posts become gray instantaneously instead of only after restarting the app
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Moved to the old layout actions in submission fragments
  * increment to v4.3.2
  * Bugfixes, Mail sync issue fixes, Show if post was read, Remove FAB from other fragments
  * Reset extra padding if not top submission
  * Merge pull request #528 from rosenpin/toolbar-overlap-fix
  * Fixed bug where the toolbar would overlap the content in the overview
  * Fixed some issues
  * Working on getting the coordinatorlayout working again
  * Merge pull request #523 from rosenpin/indent-code
  * IDEA auto indent/reformat code
  * Merge pull request #515 from rosenpin/more-merges
  * Merged more from single to multi subredditoverviews and synced with main repo
  * Merged more from single to multi subredditoverviews and synced with main repo'
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Polished fragment FAB and actionbar UX
  * Merge pull request #513 from rosenpin/fixed-force-close-single
  * Fixecd single view sub change force close issue
  * Update History.md
  * Make FABS in the fragment instead of activity
  * Working on speeding up start times
  * Changed notification settings button from 'save' to 'done' fixes #511
  * thank you alex
  * Greatly imroved history.md
  * adding a changelog that ccrama can use instead of the play store
  * Merge pull request #505 from rosenpin/forgot-to-add-secretconstants
  * I forgot to add the secretconstants..
  * Make submission fragment inflate from correct theme
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Speed improvements, Bug fixes

4.3.2 / 2015-11-11
==================

  * Release 4.3.2

4.4.2 / 2015-11-11
==================

  * Release 4.4.2

4.4.4 / 2015-11-11
==================

  * Release 4.4.4
  * Release 4.4.3

4.4.3 / 2015-11-11
==================

  * Merge pull request #504 from rosenpin/merge-overviews
  * Merged most of the overviews classes into the base overview class
  * Merge pull request #494 from l3d00m/new_strings
  * Merge pull request #499 from TomerRosenfeld/486
  * Solved issue #486
  * allow reddit.com/u/me, ...
  * update strings from crowdin
  * Merge pull request #493 from l3d00m/gold_fix
  * gilding now links to the right url
  * Merge pull request #492 from l3d00m/ui_enhancements
  * hide button, scroll to top, fixes
  * Merge pull request #488 from TomerRosenfeld/master
  * Remove fab when Reddit.fab is false
  * Added a way to toggle the fab in the theme settings
  * Merge pull request #482 from TomerRosenfeld/master
  * Fixed force close issue with previous commit in single overview mode
  * Solved issue #421 , Added Floating Action Button to add post + some idea code reformatting
  * Just some simple idea code reformatting
  * Merge pull request #477 from TomerRosenfeld/master
  * Solves cosmetic issue #375
  * Solves cosmetic issue #375
  * Merge pull request #476 from TomerRosenfeld/master
  * Fixes issue #350
  * Merge pull request #457 from Ph0ndragX/tutorial_dots_fix
  * It fixes #449 Now clicking on a radio button displays a corresponding screen.
  * Merge pull request #454 from l3d00m/new_strigns
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Lots of bug fixes,Colorize icons when voting, Switch vote icon order
  * update strings, fix a bug
  * Merge pull request #450 from l3d00m/new_strigns
  * fix a bug where all profiles are invalid
  * Merge remote-tracking branch 'ccrama/master' into new_strigns
  * Merge pull request #445 from Ph0ndragX/subreddit_description_bug
  * update strings, fix bulletin list
  * Merge pull request #447 from Ph0ndragX/invalid_username_crash
  * This fixes #422 When trying to get information about user with invalid username, the dialog is shown and then it backs to previous screen.
  * This fixes #439 Valid public description is now shown for a subreddit.

4.3.1 / 2015-11-07
==================

  * Last bugfixes of the day
  * Merge pull request #435 from Ph0ndragX/pins_reorder_crash
  * Bugfixes, speed up start time
  * Major bug fixes
  * This fixes #423
  * Merge pull request #403 from l3d00m/new_strigns
  * update strings from crowdin
  * Bug fixes and some sidebar/KitKat crash fixes
  * Merge pull request #400 from l3d00m/various3
  * remove duplicate strings and more...

4.3 / 2015-11-07
================

  * Bug fixes and getting ready for release
  * Bug fixes and getting ready for release
  * Merge pull request #386 from Spittie/master
  * Don't allow comments with 0 childs to collapse
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bug fixes and improvements
  * Merge pull request #384 from Spittie/master
  * Wrong default for the infoBar (fixes #380)
  * Subbing and unsubbing now works
  * Fixed some layout problems in sub theme picker
  * Change space to %20 in Multireddit Creator
  * Gracefully handle no data or broken authentication
  * More bugfixes, Profile improvements
  * Support more links
  * Fixed Album Caption HTML not parsing
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bugfixes
  * Release 4.2.9

4.2.9 / 2015-11-06
==================

  * updating changelog
  * updating build tools needed, due to how build tools were updated on my system
  * deleting apk
  * Merge pull request #370 from l3d00m/stringss
  * update strings
  * Default to opening content when selftext picture is tapped
  * Bugfixes
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bugfixes
  * updating changelog
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Almost done with modtools, Bug fixes, Improved light theme
  * Merge pull request #358 from l3d00m/add_strings
  * add newest translations, add badge
  * Comment nav is here
  * Load more data improvements, Spinner at bottom of lists, Fast comment nav incoming
  * Merge pull request #342 from l3d00m/master
  * add more strings and sort strings.xml
  * merge branch ccrama/master
  * Start of Modtools
  * externalize more strings, sort strings.xml
  * remove '+' from version numbers
  * Bug fixes, Overdraw fixes, Settings improvements
  * Revert to regular drawables instead of VectorDrawables, Crash fixes
  * Fixed KK insta crash, Fix load more crash
  * Bugfixes, Imgur Gallery support, Improved some settings

4.2.5 / 2015-10-29
==================

  * Sidebar improvements
  * Account chooser improvements
  * Implemented change in dataset for mult accounts. Also added code to transition the user into the new dataset
  * Fixed some touch target backgrounds
  * Font sizes in settings, Bug fixes

4.2.2 / 2015-10-28
==================

  * Syncing subs in the background
  * Stability improvements, More bugfixes
  * Bugfixes
  * Bugfixes
  * Merge pull request #304 from l3d00m/various2
  * Added translation information to README
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Load more comments is here, Bug fixes
  * Strings.xml spelling/grammar
  * fix #226
  * fix a bug with localization, temp fix #291
  * added a empty HACKING.md file
  * I made a mistake and added another changelog that wasnt needed
  * removing the apk and putting it under releases

4.2.0.1.2 / 2015-10-28
======================

  * jesus ccrama, lets try to go for longer version numbers.  Look at vim for example
  * Merge pull request #301 from l3d00m/various
  * externalize 2 more strings
  * Fixed sorting values, Added background subreddit sync
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Merge pull request #295 from l3d00m/l3d00m-del_files2
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Default sort moving to Settings
  * merge master
  * Merge pull request #297 from l3d00m/strings
  * fix a small bug, add a missing string
  * sort strings.xml
  * externalize strings from xml files
  * extracting strings from java files
  * delete *.iml files
  * Delete misc.xml
  * RecyclerView preformance improvements, Bugfixes
  * Bug fixes, Cleanup
  * Merge pull request #290 from l3d00m/strings_update
  * externalize more strings
  * seperate some strings into extra file
  * add imports
  * Merge branch 'master' of github.com:ccrama/Slide
  * Strings now Externalized
  * Strings now Externalized
  * Crash Fixes, Fix KitKat Crash
  * Merge branch 'l3d00m-strings_without'
  * Merge branch 'strings_without' of git://github.com/l3d00m/Slide into l3d00m-strings_without
  * Merge pull request #276 from l3d00m/delete_file
  * delete files that are in .gitignore
  * Backup and Sync is here
  * Multireddit Editing and Easier Pinning
  * externalized xml-files, sorted strings.xml
  * Merge pull request #238 from GermainZ/master
  * Fixed multi accounts
  * Run optipng on PNGs
  * Saving image bug fixes
  * Fixes chameleon bug
  * Fullscreen mode on Shadowbox, Image, Gif, Video, and Album screens
  * Fullscreen mode on Shadowbox, Image, Gif, Video, and Album screens
  * Comment fixes, Orientation change crash fix, Bugfixes
  * more fixes for string externalization
  * Fixed my last commit
  * I broke some stuff on my PC, checking for changes
  * Lots of LINT filtering. Probably broke some stuff.
  * Changed from PNGs to VectorDrawables
  * fixes bugs with strings
  * Merge pull request #201 from l3d00m/lint
  * Some fixes
  * more strings
  * extracted all strings from java files
  * Merge alexendoos strings
  * merge part 2
  * Merge remote-tracking branch 'refs/remotes/ccrama/master' into lint
  * Bug fixes, Temp remove link long click listener
  * Fixed scroll reset on orientation change
  * Fixed sub colors not working (for real this time)
  * Fixed sub colors not working
  * Added liked and saved to sidebar, Fixed all 'del' from turning into 'strike' in comments, Fixed awkward padding on comments in contribution views
  * Fixed blank gifs in Shadowbox
  * Fixed scrunched subs in settings
  * Bug fixes, Input sanitization fixes
  * Bug fixes, Input sanitization fixes
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Bug fixes
  * optimize imports
  * remove redunant type casts
  * fix probable bugs, remove unused vars
  * remove unnecessary modifiers, (un)boxings
  * fix code style issues
  * Merge remote-tracking branch 'refs/remotes/ccrama/master'
  * Merge pull request #195 from l3d00m/crash_helper
  * Add slide & api version to auto reported crashes
  * Added the other space Because it truly is possible to mess up with a single character change
  * Crash reporter: newline after the word at rather than every occurrence of the letters
  * Rename two things that are supposed to be eventually removed anyway
  * Merge pull request #175 from jsveiga/saveimgmode
  * Layout Image Mode setting was not preserved
  * Merge pull request #171 from jsveiga/newgotouser
  * Manual merge more recent upstream master into gotouser fix
  * Merge branch 'master' into newgotouser
  * Profile.java redundant {}
  * Settings fixes
  * Finished all editor actions
  * Check for valid user name, don't crash if user does not exist.
  * Touch feedback on more stuff
  * Fixe scrunched color chooser
  * Set default color on all color pickers on open
  * Fixed strikethrough text on all content
  * Sanitize all input fields
  * Submitting posts bug fixes and improvements
  * Switched to Universal Image Loader library (instead of Glide and Ion), Redid album view to use RecyclerView instead of ListView, Bug fixes
  * Won't crash if no pins
  * Fixed ugly transparency issues with multitasking
  * Send message screen improvements
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Fix crash if no wiki
  * Altered crash markdown
  * Redid layout settings (again), Sorting pins, Bug fixes
  * Brand new settings menu for cards/list, Bug fixes
  * Brand new settings menu for cards/list, Bug fixes
  * Merge pull request #138 from jsveiga/sortings
  * Show selected sorting options (comments and posts), Save sorting options
  * fixed indentation
  * New sidebar to strings.xml
  * strings.xml progress
  * Strings.xml: card layout, crash screen, submissions, user profiles, comments
  * Added drawer text to strings.xml
  * Added thread and theme views to strings.xml
  * Moved example cards to strings.xml
  * Potential fix to large spacing in headers orientation
  * Orientation changes now handled better (no data reload), Settings now have checkbox on right
  * Post hiding added to all applicable adapter types
  * Comment voting is here, Bug fixes
  * General setting improvements, Open externally additions (settings), Save images now works
  * removed apk from repo, belongs in https://github.com/ccrama/Slide/releases
  * Confirm exit
  * More shadowbox fixes
  * Shadowbox mode gif improvements
  * Filter text in sidebar, Bug fixes
  * Remove authorization token from crash reports
  * Fixed crashing if reddit.com couldn't be reached during auth
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Gitignore improvements
  * Merge https://github.com/l3d00m/Slide
  * updating changelog
  * Update .gitgnore
  * Fix crash for real this time
  * Fixed crash on startup
  * Added settings screen, Theme edit screen, Settings in sidebar now opens settings screen
  * Merge remote-tracking branch 'refs/remotes/ccrama/master'
  * Single Subreddit mode (forgot in last commit)
  * Sidebar fixes, Comment crash fixes
  * Fix image clicking in smallcard and list modes
  * Show score hidden on comments, Comment search improvements
  * 75% reduction in app size, Bug fixes
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Comment crash fixes, Added inbox notifications, Sidebar subreddit search improvements
  * Rename title for multi-column settings to what it is
  * Sidebar improvements, Theme improvements
  * UI touch feedback, Long press info on some views, Bug fixes
  * Many bug fixes and improvements
  * Merge pull request #35 from Alexendoo/master
  * Add Slide icon to README.md
  * Update README.md
  * Initial README rework
  * Fixed checkbox background bug
  * Search comments code added
  * Various bug fixes, Better shadowbox view code, Some better dialog code
  * Start of Shadowbox mode, Fix for single subreddit view, Various bug fixes
  * Cleaned up first entry
  * Merge branch 'master' of github.com:ccrama/Slide
  * deleting blank

v4.0alpha / 2015-10-08
======================

  * Added link to license.txt

v4.0.0alpha4 / 2015-10-08
=========================

  * updating

v4.0alpha5 / 2015-10-08
=======================

  * Removed excess newlines
  * Remove duplicate entries
  * Added a link to History.md
  * Fixed intents not working
  * Fixed crash on landscape
  * Fixed profile colors, Crash on landscape
  * fixing stuff

4.0alpha4 / 2015-10-07
======================

  * Delete gradle.properties
  * Delete cd
  * Delete settings.gradle
  * Delete gradlew.bat
  * Delete gradlew
  * Delete Slide.iml
  * Create license.txt
  * Update README.md
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Fixed many crashes, Open shortlinks with intent, Donations
  * updating changelog and tag

4.1 / 2015-10-06
================

  * GIF won't stop audio
  * GIF won't stop audio
  * GIF won't stop audio
  * New Tablet UI, Send PMs, Won't crash on startup
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Accent system, Bug Fixes, Setting Improvements

4.0alpha1 / 2015-10-03
======================

  * adding the changelog to readme
  * adding changelog and first tag
  * Create README.md

alpha / 2015-10-03
==================

  * New comment sorting code, Comment load more data, and reauthentication
  * Shortcuts and more widget code
  * Start of widget code
  * Begin fix for user deauthentication after 1 hour
  * First Commit
4.3.4 / 2015-11-17
==================
**Cleaner settings & bug fixes**
* Sorted the settings, replaced checkboxes with switches, changed some settings strings
* share image and image link from image view
* fixed some colors to follow material guidelines
* use true black background in amoled mode
* changed some icons
* fixed a crash while viewing gifs
* don't open notification settings when the user isn't logged in
* show gild count on submissions
* fixed crashes in multireddit input fields
* fixed a bug with the vote arrows in comments
* Updated translations from crowdin

4.3.25 / 2015-11-15
==================
**Bug fixes**
* Read post is now instant
* Loading from memory is no longer a blank screen!
* Mail notification crash fix
* fixed issues with live and wiki reddit URls
* don't crash on badly formatted URLs
* fixed flair alignment
* lots of other crash and bug fixes!

4.3.2 / 2015-11-14
==================
**Faster and more stable**
  * Faster start up times
  * Show post as read  
  * Colorize icons when voting
  * Switch vote icon order
  * allow reddit.com/u/me links
  * clear subreddit search after enter button press
  * Gilding now links to the correct URL
  * Added toggleable Floating Action Button to submit posts
  * Removed seperators from subreddit list in drawer
  * Disabled hamburger animation
  * Now clicking on a radio button in the tutorial displays a corresponding screen.
  * Fix a bug where all profiles are invalid
  * Fix bulletin list
  * When trying to get information about user with invalid username, the dialog is shown and then it backs to previous screen.
  * Valid public description is now shown for a subreddit
  * Fix mail sync issue
  * Fixed extra padding on submissions
  * Fixed single view force close on sub change
  * Fixed bug where the toolbar would overlap the content in the overview
  * Updated translations from [crowdin](https://crowdin.com/project/slide-for-reddit)

4.3.1 / 2015-11-07
==================

  * Don't allow comments with 0 childs to collapse
  * Fixes wrong default for the infoBar
  * Subscribing and unsubscribing now work
  * Fixed some layout problems in sub theme picker and multireddit creation
  * Gracefully handle no data or broken authentication
  * User profile improvements
  * Support more links
  * Fixed Album Caption HTML not parsing

4.2.9 / 2015-11-06
==================

  * Default to opening content when selftext picture is tapped
  * Almost done with preliminary modtools
  * Improved light theme
  * Added quick comment navigation
  * Show a spinner when at the bottom of lists
  * Start of Modtools
  * Bug fixes, Overdraw fixes, Settings improvements
  * Fixed KitKat crash on startup
  * Fixed crash on loading more posts
  * Imgur Gallery support, Improved some settings
  * Updated translations

4.2.5 / 2015-10-29
==================

  * Sidebar improvements
  * Account chooser improvements
  * Implemented change in dataset for mult accounts. Also added code to transition the user into the new dataset
  * Fixed some touch target backgrounds
  * Font sizes in settings, Bug fixes

4.2.2 / 2015-10-28
==================

  * Added syncing subs in the background
  * Fixed loading more comments not working
  * Color voting arrows when pressed

4.2.0 / 2015-10-28
==================

  * Shadowbox mode progress
  * Default sort moving to Settings
  * RecyclerView preformance improvements
  * Added Backup and Restore
  * Added Multireddit Editing and Easier Pinning
  * Fixed multi account issues
  * Optimized images
  * Saving images now works
  * Fullscreen mode on Shadowbox, Image, Gif, Video, and Album screens
  * Fixed scroll reset on orientation change
  * Fixed subreddit colors not working
  * Added liked and saved to sidebar
  * Fixed awkward padding on comments in contribution views
  * Fixed blank gifs in Shadowbox
  * Fixed scrunched subs in settings
  * Check for valid user name, don't crash if user does not exist.
  * Touch feedback on more stuff
  * Fixed scrunched color chooser
  * Set default color on all color pickers on open
  * Fixed strikethrough text on all content
  * Sanitize all input fields
  * Switched to Universal Image Loader library (instead of Glide and Ion), Redid album view to use RecyclerView instead of ListView
  * Won't crash if no pins
  * Fixed ugly transparency issues with multitasking
  * Send message screen improvements
  * Fix crash if no wiki
  * Redid layout settings (again), Sorting pins
  * Brand new settings menu for cards/list
  * Show selected sorting options (comments and posts), Save sorting options
  * Potential fix to large spacing in headers orientation
  * Orientation changes now handled better (no data reload), Settings now have checkbox on right
  * Comment voting is here
  * Open externally additions (settings)
  * Added optional confirm dialog on app exit
  * Fixed crashing if reddit.com couldn't be reached during auth

v4.0.0alpha10 / 2015-10-15
==========================

  * Added start of Shadowbox mode
  * Added settings screen, Theme edit screen, Settings in sidebar now opens settings screen
  * Added Single Subreddit mode
  * Show score hidden on comments, Comment search improvements
  * 75% reduction in app size
  * Added inbox notifications
  * Sidebar subreddit search improvements
  * Rename title for multi-column settings from 'tablet layout'
  * Search comments code added


v4.0alpha5 / 2015-10-08
=======================

  * Fixed intents not working
  * Fixed crash on landscape
  * Fixed profile colors

4.0alpha4 / 2015-10-07
======================

  * Fixed many crashes
  * Open shortlinks with intent
  * Donations

4.1 / 2015-10-06
================

  * Fixed GIF won't stop audio
  * New Tablet UI
  * Private messages
  * Won't crash on startup
  * Accent system, Setting Improvements

alpha / 2015-10-03
==================

  * New comment sorting code, Comment load more data, and reauthentication
  * Shortcuts and more widget code
  * Start of widget code
  * Begin fix for user deauthentication after 1 hour
  * First Commit
