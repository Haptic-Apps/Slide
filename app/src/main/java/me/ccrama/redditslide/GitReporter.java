package me.ccrama.redditslide;

import android.os.Bundle;

import me.ccrama.redditslide.Views.GittyReporter;

public class GitReporter extends GittyReporter {

    // Please DO NOT override onCreate. Use init instead.
    @Override
    public void init(Bundle savedInstanceState) {

        // Set where Gitty will send issues.
        // (username, repository name);
        setTargetRepository("ccrama");

        // Set Auth token to open issues if user doesn't have a GitHub account
        // For example, you can register a bot account on GitHub that will open bugs for you.


        // OPTIONAL METHODS

        // Set if User can send bugs with his own GitHub account (default: true)
        // If false, Gitty will always use your Auth token
        enableUserGitHubLogin();


        // Include other relevant info in your bug report (like custom variables)
        setExtraInfo(getIntent().getExtras().getString("stacktrace"));

        // Allow users to edit debug info (default: false)
        canEditDebugInfo();


    }
}