package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.databinding.ActivityTutorialBinding;
import me.ccrama.redditslide.databinding.ChooseaccentBinding;
import me.ccrama.redditslide.databinding.ChoosemainBinding;
import me.ccrama.redditslide.databinding.ChoosethemesmallBinding;
import me.ccrama.redditslide.databinding.FragmentPersonalizeBinding;
import me.ccrama.redditslide.databinding.FragmentWelcomeBinding;


/**
 * Created by ccrama on 3/5/2015.
 */

public class Tutorial extends AppCompatActivity {
    /**
     * The pages (wizard steps) to show in this demo.
     */
    private static final int POS_WELCOME = 0;
    private static final int POS_PERSONALIZE = 1;
    private static final int NUM_PAGES = 2;
    private int back;
    private ActivityTutorialBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources.Theme theme = getTheme();
        theme.applyStyle(new FontPreferences(this).getCommentFontStyle().getResId(), true);
        theme.applyStyle(new FontPreferences(this).getPostFontStyle().getResId(), true);
        theme.applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // The pager adapter, which provides the pages to the view pager widget.
        binding.tutorialViewPager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));

        if (getIntent().hasExtra("page")) {
            binding.tutorialViewPager.setCurrentItem(1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Palette.getDarkerColor(Color.parseColor("#FF5252")));
        }
    }

    @Override
    public void onBackPressed() {
        final int currentItem = binding.tutorialViewPager.getCurrentItem();
        if (currentItem == POS_WELCOME) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
        } else {
            // Otherwise, select the previous step.
            binding.tutorialViewPager.setCurrentItem(currentItem - 1);
        }
    }

    public static class Welcome extends Fragment {
        private FragmentWelcomeBinding welcomeBinding;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            welcomeBinding = FragmentWelcomeBinding.inflate(inflater, container, false);
            welcomeBinding.welcomeGetStarted.setOnClickListener(v1 ->
                    ((Tutorial) getActivity()).binding.tutorialViewPager.setCurrentItem(1));
            return welcomeBinding.getRoot();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            welcomeBinding = null;
        }
    }

    public static class Personalize extends Fragment {
        private FragmentPersonalizeBinding personalizeBinding;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            ((Tutorial) getActivity()).back = new ColorPreferences(getContext()).getFontStyle().getThemeType();

            personalizeBinding = FragmentPersonalizeBinding.inflate(inflater, container, false);

            personalizeBinding.secondaryColorPreview.setColorFilter(
                    getActivity().getResources().getColor(
                            new ColorPreferences(getContext()).getFontStyle().getColor()));
            personalizeBinding.primaryColorPreview.setColorFilter(Palette.getDefaultColor());
            personalizeBinding.header.setBackgroundColor(Palette.getDefaultColor());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = getActivity().getWindow();
                window.setStatusBarColor(Palette.getDarkerColor(Palette.getDefaultColor()));
            }

            personalizeBinding.primaryColor.setOnClickListener(v -> {
                final ChoosemainBinding choosemainBinding =
                        ChoosemainBinding.inflate(getActivity().getLayoutInflater());

                choosemainBinding.title.setBackgroundColor(Palette.getDefaultColor());

                choosemainBinding.picker.setColors(ColorPreferences.getBaseColors(getContext()));
                for (final int i : choosemainBinding.picker.getColors()) {
                    for (final int i2 : ColorPreferences.getColors(getContext(), i)) {
                        if (i2 == Palette.getDefaultColor()) {
                            choosemainBinding.picker.setSelectedColor(i);
                            choosemainBinding.picker2.setColors(ColorPreferences.getColors(getContext(), i));
                            choosemainBinding.picker2.setSelectedColor(i2);
                            break;
                        }
                    }
                }

                choosemainBinding.picker.setOnColorChangedListener(c -> {
                    choosemainBinding.picker2.setColors(ColorPreferences.getColors(getContext(), c));
                    choosemainBinding.picker2.setSelectedColor(c);
                });

                choosemainBinding.picker2.setOnColorChangedListener(i -> {
                    choosemainBinding.title.setBackgroundColor(choosemainBinding.picker2.getColor());
                    personalizeBinding.header.setBackgroundColor(choosemainBinding.picker2.getColor());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final Window window = getActivity().getWindow();
                        window.setStatusBarColor(Palette.getDarkerColor(choosemainBinding.picker2.getColor()));
                    }
                });

                choosemainBinding.ok.setOnClickListener(v13 -> {
                    Reddit.colors.edit().putInt("DEFAULTCOLOR", choosemainBinding.picker2.getColor()).apply();
                    finishDialogLayout();
                });

                new AlertDialog.Builder(getContext())
                        .setView(choosemainBinding.getRoot())
                        .show();
            });

            personalizeBinding.secondaryColor.setOnClickListener(v -> {
                final ChooseaccentBinding accentBinding
                        = ChooseaccentBinding.inflate(getActivity().getLayoutInflater());

                accentBinding.title.setBackgroundColor(Palette.getDefaultColor());

                final int[] arrs = new int[ColorPreferences.getNumColorsFromThemeType(0)];
                int i = 0;
                for (final ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (type.getThemeType()
                            == ColorPreferences.ColorThemeOptions.Dark.getValue()) {
                        arrs[i] = ContextCompat.getColor(getActivity(), type.getColor());

                        i++;
                    }
                }

                accentBinding.picker3.setColors(arrs);
                accentBinding.picker3.setSelectedColor(new ColorPreferences(getActivity()).getColor(""));

                accentBinding.ok.setOnClickListener(v12 -> {
                    final int color = accentBinding.picker3.getColor();
                    ColorPreferences.Theme theme = null;
                    for (final ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                        if (ContextCompat.getColor(getActivity(), type.getColor()) == color
                                && ((Tutorial) getActivity()).back == type.getThemeType()
                        ) {
                            theme = type;
                            break;
                        }
                    }
                    new ColorPreferences(getActivity()).setFontStyle(theme);
                    finishDialogLayout();
                });

                new AlertDialog.Builder(getActivity())
                        .setView(accentBinding.getRoot())
                        .show();
            });

            personalizeBinding.baseColor.setOnClickListener(v -> {
                final ChoosethemesmallBinding themesmallBinding
                        = ChoosethemesmallBinding.inflate(getActivity().getLayoutInflater());
                final View themesmallBindingRoot = themesmallBinding.getRoot();

                themesmallBinding.title.setBackgroundColor(Palette.getDefaultColor());

                for (final Pair<Integer, Integer> pair : ColorPreferences.themePairList) {
                    themesmallBindingRoot.findViewById(pair.first).setOnClickListener(v14 -> {
                        final String[] names =
                                new ColorPreferences(getActivity()).getFontStyle()
                                        .getTitle()
                                        .split("_");
                        final String name = names[names.length - 1];
                        final String newName = name.replace("(", "");

                        for (final ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName)
                                    && theme.getThemeType() == pair.second
                            ) {
                                ((Tutorial) getActivity()).back = theme.getThemeType();
                                new ColorPreferences(getActivity()).setFontStyle(theme);
                                finishDialogLayout();
                                break;
                            }
                        }
                    });
                }

                new AlertDialog.Builder(getActivity())
                        .setView(themesmallBindingRoot)
                        .show();
            });

            personalizeBinding.done.setOnClickListener(v1 -> {
                Reddit.colors.edit().putString("Tutorial", "S").commit();
                Reddit.appRestart.edit().putString("startScreen", "a").apply();
                Reddit.forceRestart(getActivity(), false);
            });

            return personalizeBinding.getRoot();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            personalizeBinding = null;
        }

        private void finishDialogLayout() {
            final Intent intent = new Intent(getActivity(), Tutorial.class);
            intent.putExtra("page", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            getActivity().overridePendingTransition(0, 0);

            getActivity().finish();
            getActivity().overridePendingTransition(0, 0);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private static class TutorialPagerAdapter extends FragmentStatePagerAdapter {

        TutorialPagerAdapter(final FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case POS_WELCOME:
                    return new Welcome();
                case POS_PERSONALIZE:
                    return new Personalize();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
