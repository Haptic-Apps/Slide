package me.ccrama.redditslide.Views;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
//Code from https://github.com/PaoloRotolo/GittyReporter. Used to override the view used by GittyReporter to change the background to black!

public abstract class GittyReporter extends AppCompatActivity {

    private EditText bugTitleEditText;
    private EditText bugDescriptionEditText;
    private EditText deviceInfoEditText;
    private String deviceInfo;
    private String targetUser;
    private String targetRepository;
    private String gitUser;
    private String gitPassword;
    private String extraInfo;
    private String gitToken;
    private Boolean enableGitHubLogin = true;
    private Boolean enableGuestGitHubLogin = true;

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set my custom theme here - ccrama
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.gitreporter);

        // Get Device info and print them in EditText
        deviceInfoEditText = (EditText) findViewById(R.id.gittyreporter_device_info);
        getDeviceInfo();
        deviceInfoEditText.setText(deviceInfo);

        init(savedInstanceState);

        final View nextFab = findViewById(R.id.gittyreporter_fab_next);
        final View sendFab = findViewById(R.id.gittyreporter_fab_send);

        if (!enableGitHubLogin){
            nextFab.setVisibility(View.INVISIBLE);
            sendFab.setVisibility(View.VISIBLE);
        }

        AppCompatCheckBox githubCheckbox = (AppCompatCheckBox) findViewById(R.id.gittyreporter_github_checkbox);
        AppCompatButton registerButton = (AppCompatButton) findViewById(R.id.gittyreporter_github_register);

        final EditText userName = (EditText) findViewById(R.id.gittyreporter_login_username);
        final EditText userPassword = (EditText) findViewById(R.id.gittyreporter_login_password);

        userPassword.setTypeface(Typeface.DEFAULT);
        userPassword.setTransformationMethod(new PasswordTransformationMethod());

        if (!enableGuestGitHubLogin){
            githubCheckbox.setChecked(false);
            githubCheckbox.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }

        githubCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            userName.setEnabled(false);
                            userName.setText("");
                            userPassword.setEnabled(false);
                            userPassword.setText("");
                        } else {
                            userName.setEnabled(true);
                            userPassword.setEnabled(true);
                        }
                    }
                }
        );
    }

    public void reportIssue (View v) {
        if (enableGitHubLogin) {
            final AppCompatCheckBox githubCheckbox = (AppCompatCheckBox) findViewById(R.id.gittyreporter_github_checkbox);
            EditText userName = (EditText) findViewById(R.id.gittyreporter_login_username);
            EditText userPassword = (EditText) findViewById(R.id.gittyreporter_login_password);

            if (!githubCheckbox.isChecked()){
                if (validateGitHubLogin()){
                    this.gitUser = userName.getText().toString();
                    this.gitPassword = userPassword.getText().toString();
                    sendBugReport();
                }
            } else {
                this.gitUser = "";
                this.gitPassword = "";
                sendBugReport();
            }
        } else {
            if (validateBugReport()) {
                this.gitUser = "";
                this.gitPassword = "";
                sendBugReport();
            }
        }
    }

    private boolean validateGitHubLogin(){
        EditText userName = (EditText) findViewById(R.id.gittyreporter_login_username);
        EditText userPassword = (EditText) findViewById(R.id.gittyreporter_login_password);

        boolean hasErrors = false;

        if (TextUtils.isEmpty(userName.getText())){
            setError(userName, "Please enter a vaild username");

            hasErrors = true;
        } else {
            removeError(userName);
        }

        if (TextUtils.isEmpty(userPassword.getText())) {
            setError(userPassword, "Please enter a vaild password");

            hasErrors = true;
        } else {
            removeError(userPassword);
        }

        return !hasErrors;
    }

    private boolean validateBugReport(){
        bugTitleEditText = (EditText) findViewById(R.id.gittyreporter_bug_title);
        bugDescriptionEditText = (EditText) findViewById(R.id.gittyreporter_bug_description);

        boolean hasErrors = false;

        if (TextUtils.isEmpty(bugTitleEditText.getText())) {
            setError(bugTitleEditText, "Please enter a valid title");

            hasErrors = true;
        } else {
            removeError(bugTitleEditText);
        }

        if (TextUtils.isEmpty(bugDescriptionEditText.getText())) {
            setError(bugDescriptionEditText, "Please describe your issue");

            hasErrors = true;
        } else {
            removeError(bugDescriptionEditText);
        }

        return !hasErrors;
    }

    private void setError(TextView view, String text) {
        TextInputLayout parent = (TextInputLayout) view.getParent();

        // there is a small flashing when the error is set again
        // the only way to fix that is to track if the error is
        // currently shown, because for some reason TextInputLayout
        // doesn't provide any getError methods.
        parent.setError(text);
    }

    private void removeError(TextView view) {
        TextInputLayout parent = (TextInputLayout) view.getParent();

        parent.setError(null);
    }

    private void sendBugReport(){
        bugTitleEditText = (EditText) findViewById(R.id.gittyreporter_bug_title);
        bugDescriptionEditText = (EditText) findViewById(R.id.gittyreporter_bug_description);
        final String bugTitle = bugTitleEditText.getText().toString();
        final String bugDescription = bugDescriptionEditText.getText().toString();

        if (extraInfo == null) {
            this.extraInfo = "Nothing to show.";
        } else if (!enableGitHubLogin){
            this.gitUser = "";
            this.gitPassword = "";
        }

        new ReportIssue(GittyReporter.this, this).execute(gitUser, gitPassword, bugTitle, bugDescription, deviceInfo, targetUser, targetRepository, extraInfo, gitToken, enableGitHubLogin.toString());
    }

    public void showLoginPage (View v) {
        if (validateBugReport()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateLoginPage();
            } else {
                View loginView = findViewById(R.id.gittyreporter_loginFrame);
                View nextFab = findViewById(R.id.gittyreporter_fab_next);
                View sendFab = findViewById(R.id.gittyreporter_fab_send);

                loginView.setVisibility(View.VISIBLE);
                nextFab.setVisibility(View.INVISIBLE);
                sendFab.setVisibility(View.VISIBLE);
            }
        }
    }

    private void animateLoginPage(){
        final View colorView = findViewById(R.id.gittyreporter_material_ripple);
        final View loginView = findViewById(R.id.gittyreporter_loginFrame);
        final View nextFab = findViewById(R.id.gittyreporter_fab_next);
        final View sendFab = findViewById(R.id.gittyreporter_fab_send);

        final AlphaAnimation fadeOutColorAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeOutColorAnim.setDuration(400);
        fadeOutColorAnim.setInterpolator(new AccelerateInterpolator());
        final AlphaAnimation fadeOutFabAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeOutFabAnim.setDuration(400);
        fadeOutFabAnim.setInterpolator(new AccelerateInterpolator());
        final AlphaAnimation fadeInAnim = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnim.setDuration(400);
        fadeInAnim.setInterpolator(new AccelerateInterpolator());

        fadeOutColorAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loginView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                colorView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutFabAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sendFab.setVisibility(View.VISIBLE);
                sendFab.startAnimation(fadeInAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        int cx = (colorView.getRight());
        int cy = (colorView.getBottom());
        int finalRadius = Math.max(colorView.getWidth(), colorView.getHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Animator rippleAnim =
                    ViewAnimationUtils.createCircularReveal(colorView, cx, cy, 0, finalRadius);

            rippleAnim.setInterpolator(new AccelerateInterpolator());
            rippleAnim.addListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    colorView.startAnimation(fadeOutColorAnim);
                    nextFab.startAnimation(fadeOutFabAnim);
                    nextFab.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                }
            });
            colorView.setVisibility(View.VISIBLE);
            rippleAnim.start();
        } else {
            colorView.setVisibility(View.VISIBLE);

        }

    }

    public void showDoneAnimation(){
        final View doneView = findViewById(R.id.gittyreporter_doneFrame);
        final View doneImage = findViewById(R.id.gittyreporter_done_image);
        final View sendFab = findViewById(R.id.gittyreporter_fab_send);

        final AlphaAnimation fadeOutColorAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeOutColorAnim.setDuration(1000);
        fadeOutColorAnim.setInterpolator(new AccelerateInterpolator());
        final AlphaAnimation fadeOutFabAnim = new AlphaAnimation(1.0f, 0.0f);
        fadeOutFabAnim.setDuration(400);
        fadeOutFabAnim.setInterpolator(new AccelerateInterpolator());

        fadeOutColorAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doneImage.setVisibility(View.INVISIBLE);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutFabAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sendFab.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        int cx = (doneView.getRight());
        int cy = (doneView.getBottom());
        int finalRadius = Math.max(doneView.getWidth(), doneView.getHeight());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Animator rippleAnim =
                    ViewAnimationUtils.createCircularReveal(doneView, cx, cy, 0, finalRadius);

            rippleAnim.setInterpolator(new AccelerateInterpolator());
            rippleAnim.addListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                    sendFab.startAnimation(fadeOutFabAnim);
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    doneImage.startAnimation(fadeOutColorAnim);

                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                }
            });

            doneView.setVisibility(View.VISIBLE);
            rippleAnim.start();
        } else {
            doneView.setVisibility(View.VISIBLE);

        }
    }

    public void setTargetRepository(String user, String repository){
        this.targetUser = user;
        this.targetRepository = repository;
    }

    public void setGuestOAuth2Token(String token){
        this.gitToken = token;
    }

    public void setExtraInfo(String info){
        this.extraInfo = info;
    }

    public void enableUserGitHubLogin(boolean enableLogin){
        this.enableGitHubLogin = enableLogin;
    }

    public void enableGuestGitHubLogin(boolean enableGuest){
        this.enableGuestGitHubLogin = enableGuest;
    }

    public void canEditDebugInfo(boolean canEdit){
        deviceInfoEditText.setEnabled(canEdit);
    }

    public void setFabColor1(int colorNormal, int colorPressed, int colorRipple){
        final com.melnykov.fab.FloatingActionButton nextFab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.gittyreporter_fab_next);
        nextFab.setColorNormal(colorNormal);
        nextFab.setColorPressed(colorPressed);
        nextFab.setColorRipple(colorRipple);
    }

    public void setFabColor2(int colorNormal, int colorPressed, int colorRipple){
        final com.melnykov.fab.FloatingActionButton sendFab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.gittyreporter_fab_send);
        sendFab.setColorNormal(colorNormal);
        sendFab.setColorPressed(colorPressed);
        sendFab.setColorRipple(colorRipple);
    }

    public void setBackgroundColor1(int color){
        FrameLayout view = (FrameLayout) findViewById(R.id.gittyreporter_reportFrame);
        view.setBackgroundColor(color);
    }

    public void setBackgroundColor2(int color){
        FrameLayout view = (FrameLayout) findViewById(R.id.gittyreporter_loginFrame);
        view.setBackgroundColor(color);
    }

    public void setRippleColor(int color){
        FrameLayout ripple = (FrameLayout) findViewById(R.id.gittyreporter_material_ripple);
        ripple.setBackgroundColor(color);
    }



    @Override
    public void onBackPressed() {
        View loginView = findViewById(R.id.gittyreporter_loginFrame);
        if (loginView.getVisibility() == View.VISIBLE){
            View nextFab = findViewById(R.id.gittyreporter_fab_next);
            View sendFab = findViewById(R.id.gittyreporter_fab_send);

            loginView.setVisibility(View.INVISIBLE);
            nextFab.setVisibility(View.VISIBLE);
            sendFab.setVisibility(View.INVISIBLE);
        } else {
            finish();
        }
    }

    public void openGitHubRegisterPage(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/join"));
        startActivity(browserIntent);
    }

    private void getDeviceInfo() {
        try {
            String s = "Debug info:";
            s += "\n OS Version: "      + System.getProperty("os.version")      + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            s += "\n OS API Level: "    + android.os.Build.VERSION.SDK_INT;
            s += "\n Device: "          + android.os.Build.DEVICE;
            s += "\n Model (and Product): " + android.os.Build.MODEL            + " ("+ android.os.Build.PRODUCT + ")";

            s += "\n RELEASE: "         + android.os.Build.VERSION.RELEASE;
            s += "\n BRAND: "           + android.os.Build.BRAND;
            s += "\n DISPLAY: "         + android.os.Build.DISPLAY;
            s += "\n CPU_ABI: "         + android.os.Build.CPU_ABI;
            s += "\n CPU_ABI2: "        + android.os.Build.CPU_ABI2;
            s += "\n HARDWARE: "        + android.os.Build.HARDWARE;
            s += "\n MANUFACTURER: "    + android.os.Build.MANUFACTURER;

            deviceInfo = s;
        } catch (Exception e) {
            Log.e("gitty-reporter", "Error getting Device INFO");
        }
    }

    public abstract void init(@Nullable Bundle savedInstanceState);
}