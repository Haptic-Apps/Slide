package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import me.ccrama.redditslide.Fragments.SubredditListView;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditSearch extends BaseActivityAnim {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.edit: {
                new MaterialDialog.Builder(SubredditSearch.this)
                        .alwaysCallInputCallback()
                        .inputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        .inputRange(3, 100)
                        .input(getString(R.string.discover_search), term, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if (input.length() >= 3) {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                } else {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                }
                            }
                        })
                        .positiveText(R.string.search_all)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent inte = new Intent(SubredditSearch.this, SubredditSearch.class);
                                inte.putExtra("term", dialog.getInputEditText().getText().toString());
                                SubredditSearch.this.startActivity(inte);
                                finish();
                            }
                        })
                        .negativeText(R.string.btn_cancel)
                        .show();
            }
            return true;
            default:
                return false;
        }
    }

    String term;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        term = getIntent().getExtras().getString("term");
        applyColorTheme("");
        setContentView(R.layout.activity_fragmentinner);
        setupAppBar(R.id.toolbar, term, true, true);

        Fragment f = new SubredditListView();
        Bundle args = new Bundle();
        args.putString("id", term);
        f.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentcontent, f);
        fragmentTransaction.commit();
    }


}
