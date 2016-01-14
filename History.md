
4.5.1 / 2016-01-14
==================

  * Ready for production beta release
  * Made saving gifs work
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Small tint changes
  * fix filters, fix #776, fix #771
  * Made sidebar buttons untinted
  * Removed some more hard to read colors
  * More color fixes
  * Made inflated dialog titles white, Removed _100 color values from secondary color chooser, Fixed contrast issues with font colors for the most part
  * Last youtube fix
  * Made sharing link text use the util in Reddit.class
  * Fixed youtube issue, Fixed link sharing on internal webview
  * Fixed small youtube link time issue
  * Fixed some card background issues, Made borderless card buttons on v21 and above
  * Made youtube links work from comments again, Added color card background matching mode to theme settings and applicable methods in CreateCardView
  * Cleanup of saving code
  * Redid the multicolumn dialog, Added option for dual portrait columns, Re-organized the edit layout activity, Added save from actionbar button
  * Fixed wrong color text in insert link dialog
  * Added filter system with support for the SubredditPosts dataset, Added settings option for filters, Created a helper class to create the Regex pattern from a comma separated string and matching based on title, body, or domain
  * Added progress spinner to load more comments view
  * Started working on horizontal view for Albums
  * Fixed padding between title and thumbnail
  * Fixed flash when loading/reloading comments
  * Added a no thumbnail preview if there are no associated images or previews
  * Made selftext use the same font size as comments
  * Changed font sizes for the last time
  * Normalized Large and Larger font sizes
  * Added 2 new font sizes, Added 3 new font options for post titles
  * Made font sizes work again for submission titles
  * Merge pull request #774 from trevorhalvorson/master
  * Implement long click on links within comments for additional actions
  * Re-added image previews to fullscreen post view with selftext
  * Made submissions load if cache is set to always use cached data but no data is stored for that subreddit
  * Added titles and description support to imgur galleries
  * Made app totally log out if all accounts are removed
  * Made switching accounts work, Made login open new account, Fixed blank page on crash or switching accounts, Added dialog to remove acconts (log out)
  * Make saving comments actually save, not unsave
  * Updated to JRAW v0.8 (also updated my fork at /ccrama/JRAW), Added comment saving
  * Made failed album requests open in browser, Fixed crash when hiding all posts in a subreddit
  * Made comment screen use full sized image if possible
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Complete redo of card and list modes, Changed options for card layouts and changed the setting activity respectively, New thumbnail for nsfw posts
  * Merge pull request #768 from thatJavaNerd/master
  * Replace "Add friend" and "Remove friend" with XML constants
  * small fixes and optimizations
  * Removed all instances of ActiveTextView from Slide
  * Added setting to hide the header on the navbar, Misc fixes
  * Fixed crash going back to MainActivity from Inbox
  * Added message count bubble in sidebar, Fixed comments sometimes not loading after replying
  * Fixed crash when removing friend
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Added friending from the profile screen, Fixed gradle, Started working on reset button from edit cards layout not working
  * Merge pull request #759 from thatJavaNerd/master
  * Moved startup_* strings to a <string-array>
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Fixed sharing images, Fixed not being able to open sidebar in single sub view
  * Rename some settings in accordance with removal of others
  * Merge pull request #757 from thatJavaNerd/master
  * Added TitleTextView to submission_fullscreen
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Made title font choices thicker
  * Start of comment style revamp, Added a setting page for comment and post title fonts
  * Rewrote me.ccrama.redditslide.util.NetworkUtil
  * Merge pull request #754 from thatJavaNerd/master
  * Reference static variable through a static context
  * Complete wiki overhaul, much faster and more reliable and gives the user more indication of loading progress
  * Recover if SubredditStorage.subredditsForHome is removed from memory
  * Fixed crash when opening search links
  * Merge branch 'master' of https://github.com/ccrama/Slide
  * Changed the view animation to an adapter one (no more glitchy movements), Fixed pull to refresh crash, Fixed double drawer on first open, Fixed changing of ViewDragHelper size when returning to activity (made override public not static)
  * Merge pull request #750 from thatJavaNerd/master
  * More elegant way of setting view visibility
  * new release and updated changelog

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
