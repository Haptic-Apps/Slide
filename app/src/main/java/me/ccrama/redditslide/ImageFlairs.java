package me.ccrama.redditslide;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import net.dean.jraw.http.HttpRequest;
import net.dean.jraw.http.MediaTypes;
import net.dean.jraw.http.RestResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.Activities.SendMessage;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OkHttpImageDownloader;

/**
 * Created by Carlos on 4/15/2017.
 */

public class ImageFlairs {
    public static void syncFlairs(final Context context, final String subreddit) {
        new StylesheetFetchTask(subreddit, context) {
            @Override
            protected void onPostExecute(FlairStylesheet flairStylesheet) {
                super.onPostExecute(flairStylesheet);
                d.dismiss();
                if (flairStylesheet != null) {
                    flairs.edit().putBoolean(subreddit.toLowerCase(Locale.ENGLISH), true).commit();
                    d = new AlertDialogWrapper.Builder(context).setTitle("Subreddit flairs synced")
                            .setMessage("Slide found and synced "
                                    + flairStylesheet.count
                                    + " image flairs")
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(context).setTitle(
                            "Error syncing subreddit flairs")
                            .setMessage("Slide could not find any subreddit flairs to sync from /r/"
                                    + subreddit
                                    + "'s stylesheet.")
                            .setPositiveButton(R.string.btn_ok, null);
                    if(Authentication.isLoggedIn){
                        b.setNeutralButton("Report no flairs", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Not all subreddits can be parsed, but send a message to SlideBot and hopefully we can add support for this subreddit :)\n\nPlease, only send one report.", Toast.LENGTH_LONG);
                                Intent i = new Intent(context, SendMessage.class);
                                i.putExtra(SendMessage.EXTRA_NAME, "slidebot");
                                i.putExtra(SendMessage.EXTRA_MESSAGE, "/r/" + subreddit);
                                i.putExtra(SendMessage.EXTRA_REPLY, "Subreddit flair");
                                context.startActivity(i);
                            }
                        });
                    }

                    d = b.show();
                }
            }


            @Override
            protected void onPreExecute() {
                d = new MaterialDialog.Builder(context).progress(true, 100)
                        .content(R.string.misc_please_wait)
                        .title("Syncing flairs...")
                        .cancelable(false)
                        .show();
            }
        }.execute();
    }

    static class StylesheetFetchTask extends AsyncTask<Void, Void, FlairStylesheet> {
        String  subreddit;
        Context context;
        Dialog  d;

        StylesheetFetchTask(String subreddit, Context context) {
            super();
            this.context = context;
            this.subreddit = subreddit;
        }

        @Override
        protected FlairStylesheet doInBackground(Void... params) {
            try {
                HttpRequest r = new HttpRequest.Builder().host("reddit.com")
                        .path("/r/" + subreddit + "/stylesheet")
                        .expected(MediaTypes.CSS.type())
                        .build();
                RestResponse response = Authentication.reddit.execute(r);
                String stylesheet = response.getRaw();

                ArrayList<String> allImages = new ArrayList<>();
                FlairStylesheet flairStylesheet = new FlairStylesheet(stylesheet);
                int count = 0;
                for (String s : flairStylesheet.getListOfFlairIds()) {
                    String classDef = flairStylesheet.getClass(flairStylesheet.stylesheetString,
                            "flair-" + s);
                    try {
                        String backgroundURL = flairStylesheet.getBackgroundURL(classDef);
                        if (backgroundURL == null) backgroundURL = flairStylesheet.defaultURL;
                        if (!allImages.contains(backgroundURL)) allImages.add(backgroundURL);
                    } catch (Exception e) {
                        //  e.printStackTrace();
                    }
                }
                if (flairStylesheet.defaultURL != null) {
                    LogUtil.v("Default url is " + flairStylesheet.defaultURL);
                    allImages.add(flairStylesheet.defaultURL);
                }
                for (String backgroundURL : allImages) {
                    flairStylesheet.cacheFlairsByFile(subreddit, backgroundURL, context);
                }
                return flairStylesheet;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static SharedPreferences flairs;

    public static boolean isSynced(String subreddit) {
        return flairs.contains(subreddit.toLowerCase(Locale.ENGLISH));
    }

    public static class CropTransformation {
        private int width, height, x, y;
        private String id;

        public CropTransformation(Context context, String id, int width, int height, int x, int y) {
            super();
            this.id = id;
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }

        public Bitmap transform(Bitmap bitmap, boolean isPercentage) throws Exception {
            int nX, nY;

            if (isPercentage) {
                nX = Math.max(0, Math.min(bitmap.getWidth() - 1, bitmap.getWidth() * x / 100));
                nY = Math.max(0, Math.min(bitmap.getHeight() - 1, bitmap.getHeight() * y / 100));
            } else {
                nX = Math.max(0, Math.min(bitmap.getWidth() - 1, x));
                nY = Math.max(0, Math.min(bitmap.getHeight() - 1, y));
            }

            int nWidth = Math.max(1, Math.min(bitmap.getWidth() - nX - 1, width)), nHeight =
                    Math.max(1, Math.min(bitmap.getHeight() - nY - 1, height));

            LogUtil.v("Flair loaded: "
                    + id
                    + " size: "
                    + nWidth
                    + "x"
                    + nHeight
                    + " location: "
                    + nX
                    + ":"
                    + nY + " and bit is " + bitmap.getWidth() + ":" + bitmap.getHeight());

            return Bitmap.createBitmap(bitmap, nX, nY, nWidth, nHeight);
        }

    }


    static class FlairStylesheet {
        String stylesheetString;
        Dimensions defaultDimension = new Dimensions();
        Location   defaultLocation  = new Location();
        String     defaultURL       = "";
        int count;

        Dimensions prevDimension = null;

        static class Dimensions {
            int width, height;
            Boolean scale   = false;
            Boolean missing = true;

            Dimensions(int width, int height) {
                this.width = width;
                this.height = height;
                if (height == -1) {
                    scale = true;
                }
                missing = false;
            }

            Dimensions() {
            }
        }

        static class Location {
            int x, y;
            Boolean isPercentage = false;
            Boolean missing      = true;

            Location(int x, int y) {
                this.x = x;
                this.y = y;
                missing = false;
            }

            Location(int x, int y, boolean isPercentage) {
                this.x = x;
                this.y = y;
                this.isPercentage = isPercentage;
                missing = false;
            }

            Location() {
            }
        }

        FlairStylesheet(String stylesheetString) {
            stylesheetString =
                    stylesheetString.replaceAll("@media[^{]+\\{([\\s\\S]+?\\})\\s*\\}", "");
            stylesheetString = stylesheetString.replaceAll("~.", " .");
            this.stylesheetString = stylesheetString;

            String baseFlairDef = getClass(stylesheetString, "flair");
            if (baseFlairDef == null) return;

            LogUtil.v("Base is " + baseFlairDef);
            // Attempts to find default dimension, offset and image URL
            defaultDimension = getBackgroundSize(baseFlairDef);
            LogUtil.v("Default dimens are " + defaultDimension.width + ":" + defaultDimension.height);
            defaultLocation = getBackgroundPosition(baseFlairDef);
            defaultURL = getBackgroundURL(baseFlairDef);
            count = 0;
        }

        /**
         * Get class definition string by class name.
         *
         * @param cssDefinitionString
         * @param className
         * @return
         */
        String getClass(String cssDefinitionString, String className) {
            Pattern propertyDefinition = Pattern.compile(
                    "(?<! )\\." + className + "(?!-|\\[|[A-Za-z0-9_.])([^\\{]*)*\\{(.+?)\\}");
            Matcher matches = propertyDefinition.matcher(cssDefinitionString);

            StringBuilder properties = null;

            while (matches.find()) {
                if (properties == null) properties = new StringBuilder();
                properties.insert(0, matches.group(2)
                        + ";");   // append properties to simulate property overriding
            }

            return properties == null ? null : properties.toString();
        }

        /**
         * Get property value inside a class definition by property name.
         *
         * @param classDefinitionsString
         * @param property
         * @return
         */
        String getProperty(String classDefinitionsString, String property) {
            Pattern propertyDefinition = Pattern.compile("(?<!-)" + property + "\\s*:\\s*(.+?)(;|$)");
            Matcher matches = propertyDefinition.matcher(classDefinitionsString);

            if (matches.find()) {
                return matches.group(1);
            } else {
                return null;
            }
        }

        //Attempts to get a real integer value instead of "auto", if possible
        String getPropertyTryNoAuto(String classDefinitionsString, String property) {
            Pattern propertyDefinition = Pattern.compile("(?<!-)" + property + "\\s*:\\s*(.+?)(;|$)");
            Matcher matches = propertyDefinition.matcher(classDefinitionsString);

            String defaultString;
            if (matches.find()) {
                defaultString = matches.group(1);
            } else {
                return null;
            }
            LogUtil.v("Has auto");
            while((defaultString.contains("auto")||(!defaultString.contains("%") || !defaultString.contains("px"))) && matches.find()){
                defaultString = matches.group(1);
            }
            LogUtil.v("Returning " + defaultString);
            return defaultString;
        }


        String getPropertyBackgroundUrl(String classDefinitionsString) {
            Pattern propertyDefinition = Pattern.compile("background:url\\([\"'](.+?)[\"']\\)");
            Matcher matches = propertyDefinition.matcher(classDefinitionsString);

            if (matches.find()) {
                return matches.group(1);
            } else {
                return null;
            }
        }

        /**
         * Get flair background url in class definition.
         *
         * @param classDefinitionString
         * @return
         */
        String getBackgroundURL(String classDefinitionString) {
            Pattern urlDefinition = Pattern.compile("url\\([\"\'](.+?)[\"\']\\)");
            String backgroundProperty = getPropertyBackgroundUrl(classDefinitionString);
            if (backgroundProperty != null) {
                // check "background"
                    String url = backgroundProperty;
                    if (url.startsWith("//")) url = "https:" + url;
                    return url;
            }
            // either backgroundProperty is null or url cannot be found
            String backgroundImageProperty = getProperty(classDefinitionString, "background-image");
            if (backgroundImageProperty != null) {
                // check "background-image"
                Matcher matches = urlDefinition.matcher(backgroundImageProperty);
                if (matches.find()) {
                    String url = matches.group(1);
                    if (url.startsWith("//")) url = "https:" + url;
                    return url;
                }
            }
            // could not find any background url
            return null;
        }

        /**
         * Get background dimension in class definition.
         *
         * @param classDefinitionString
         * @return
         */
        Dimensions getBackgroundSize(String classDefinitionString) {
            Pattern numberDefinition = Pattern.compile("(\\d+)\\s*px");

            boolean autoWidth = false, autoHeight = false;
            // check common properties used to define width
            String widthProperty = getPropertyTryNoAuto(classDefinitionString, "width");
            if (widthProperty == null) {
                widthProperty = getPropertyTryNoAuto(classDefinitionString, "min-width");
            } else if (widthProperty.equals("auto")) {
                autoWidth = true;
            }
            if (widthProperty == null) {
                widthProperty = getProperty(classDefinitionString, "text-indent");
            }
            if (widthProperty == null) return new Dimensions();

            // check common properties used to define height
            String heightProperty = getPropertyTryNoAuto(classDefinitionString, "height");
            if (heightProperty == null) {
                heightProperty = getPropertyTryNoAuto(classDefinitionString, "min-height");
            } else if (heightProperty.equals("auto")) {
                autoHeight = true;
            }
            if (heightProperty == null) return new Dimensions();

            int width = 0, height = 0;
            Matcher matches;

            if (!autoWidth) {
                matches = numberDefinition.matcher(widthProperty);
                if (matches.find()) {
                    width = Integer.parseInt(matches.group(1));
                } else {
                    return new Dimensions();
                }
            }

            if (!autoHeight) {
                matches = numberDefinition.matcher(heightProperty);
                if (matches.find()) {
                    height = Integer.parseInt(matches.group(1));
                } else {
                    return new Dimensions();
                }
            }

            if (autoWidth) {
                width = height;
            }
            if (autoHeight) {
                height = width;
            }
            return new Dimensions(width, height);
        }

        /**
         * Get background scaling in class definition.
         *
         * @param classDefinitionString
         * @return
         */
        Dimensions getBackgroundScaling(String classDefinitionString) {
            Pattern positionDefinitionPx =
                    Pattern.compile("([+-]?\\d+|0)(px\\s|\\s)+(|([+-]?\\d+|0)(px|))");
            String backgroundPositionProperty =
                    getProperty(classDefinitionString, "background-size");
            String backgroundPositionPropertySecondary =
                    getProperty(classDefinitionString, "background-size");

            if (backgroundPositionProperty == null && backgroundPositionPropertySecondary == null
                    || backgroundPositionProperty == null
                    && !backgroundPositionPropertySecondary.contains("px ")
                    && !backgroundPositionPropertySecondary.contains("px;")) {
                return new Dimensions();
            }

            Matcher matches = positionDefinitionPx.matcher(backgroundPositionProperty);
            if (matches.find()) {
                return new Dimensions(Integer.parseInt(matches.group(1)),
                        matches.groupCount() < 2 ? Integer.parseInt(matches.group(3)) : -1);
            } else {
                return new Dimensions();
            }

        }

        /**
         * Get background offset in class definition.
         *
         * @param classDefinitionString
         * @return
         */
        Location getBackgroundPosition(String classDefinitionString) {
            Pattern positionDefinitionPx =
                    Pattern.compile("([+-]?\\d+|0)(px\\s|\\s)+([+-]?\\d+|0)(px|)"),
                    positionDefinitionPercentage =
                            Pattern.compile("([+-]?\\d+|0)(%\\s|\\s)+([+-]?\\d+|0)(%|)");

            String backgroundPositionProperty =
                    getProperty(classDefinitionString, "background-position");
            if (backgroundPositionProperty == null) {
                backgroundPositionProperty = getProperty(classDefinitionString, "background");
                if (backgroundPositionProperty == null) {
                    return new Location();
                }
            }

            Matcher matches = positionDefinitionPx.matcher(backgroundPositionProperty);
            try {
                if (matches.find()) {
                    return new Location(-Integer.parseInt(matches.group(1)),
                            -Integer.parseInt(matches.group(3)));
                } else {
                    matches = positionDefinitionPercentage.matcher(backgroundPositionProperty);
                    if (matches.find()) {
                        return new Location(
                                Integer.parseInt(matches.group(1)),
                                Integer.parseInt(matches.group(3)), true);
                    }
                }
            } catch (NumberFormatException ignored) {

            }
            return new Location();
        }

        Dimensions getBackgroundOffset(String classDefinitionString) {
            Pattern positionDefinitionPx =
                    Pattern.compile("([+-]?\\d+|0)\\/+([+-]?\\d+|0)(px|)");
           String backgroundPositionProperty = getProperty(classDefinitionString, "background");
            if (backgroundPositionProperty == null) {
                return new Dimensions();
            }


            Matcher matches = positionDefinitionPx.matcher(backgroundPositionProperty);
            try {
                if (matches.find()) {
                    return new Dimensions(Integer.parseInt(matches.group(2)),
                            Integer.parseInt(matches.group(2)));
                }
            } catch (NumberFormatException ignored) {

            }
            return new Dimensions();
        }


        /**
         * Request a flair by flair id. `.into` can be chained onto this method call.
         *
         * @param id
         * @param context
         * @return
         */
        void cacheFlairsByFile(final String sub, final String filename, final Context context) {
            final ArrayList<String> flairsToGet = new ArrayList<>();
            LogUtil.v("Doing sheet " + filename);
            for (String s : getListOfFlairIds()) {
                String classDef = getClass(stylesheetString, "flair-" + s);
                if (classDef != null && !classDef.isEmpty()) {
                    String backgroundURL = getBackgroundURL(classDef);
                    if (backgroundURL == null) backgroundURL = defaultURL;
                    if (backgroundURL != null && backgroundURL.equalsIgnoreCase(filename)) {
                        flairsToGet.add(s);
                    }
                }
            }

            String scaling = getClass(stylesheetString, "flair");
            final Dimensions backScaling;
            final Dimensions offset;
            if (scaling != null) {
                backScaling = getBackgroundScaling(scaling);
                offset = getBackgroundOffset(scaling);
                LogUtil.v("Offset is " + offset.width);
            } else {
                backScaling = new Dimensions();
                offset = new Dimensions();
            }
            if ((!backScaling.missing && !backScaling.scale) || (!offset.missing && !offset.scale)) {
                Bitmap loaded = getFlairImageLoader(context).loadImageSync(filename,
                        new ImageSize(backScaling.width, backScaling.height));
                if (loaded != null) {
                    Bitmap b;
                    if(backScaling.missing || backScaling.width < offset.width) {
                        b = Bitmap.createScaledBitmap(loaded, offset.width, offset.height,
                                false);
                    } else {
                        b = Bitmap.createScaledBitmap(loaded, backScaling.width, backScaling.height,
                                false);
                    }
                    loadingComplete(b, sub, context, filename, flairsToGet);
                    loaded.recycle();
                }
            } else {
                Bitmap loadedB = getFlairImageLoader(context).loadImageSync(filename);
                if (loadedB != null) {
                    if (backScaling.scale) {
                        int width = backScaling.width;
                        int height = loadedB.getHeight();
                        int scaledHeight = (height * width) / loadedB.getWidth();
                        loadingComplete(
                                Bitmap.createScaledBitmap(loadedB, width, scaledHeight, false), sub,
                                context, filename, flairsToGet);
                        loadedB.recycle();
                    } else {
                        loadingComplete(loadedB, sub, context, filename, flairsToGet);
                    }
                }
            }
        }

        private void loadingComplete(Bitmap loadedImage, String sub, Context context,
                String filename, ArrayList<String> flairsToGet) {
            if (loadedImage != null) {
                for (String id : flairsToGet) {
                    Bitmap newBit = null;
                    String classDef =
                            FlairStylesheet.this.getClass(stylesheetString, "flair-" + id);
                    if (classDef == null) break;

                    Dimensions flairDimensions = getBackgroundSize(classDef);
                    if (flairDimensions.missing) {
                        flairDimensions = defaultDimension;
                    }

                    prevDimension = flairDimensions;

                    Location flairLocation = getBackgroundPosition(classDef);
                    if (flairLocation.missing) flairLocation = defaultLocation;

                    LogUtil.v("Flair: "
                            + id
                            + " size: "
                            + flairDimensions.width
                            + "x"
                            + flairDimensions.height
                            + " location: "
                            + flairLocation.x
                            + ":"
                            + flairLocation.y);
                    try {
                        newBit = new CropTransformation(context, id, flairDimensions.width,
                                flairDimensions.height, flairLocation.x, flairLocation.y).transform(
                                loadedImage, flairLocation.isPercentage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        getFlairImageLoader(context).getDiskCache()
                                .save(sub.toLowerCase(Locale.ENGLISH) + ":" + id.toLowerCase(Locale.ENGLISH), newBit);
                        count += 1;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                loadedImage.recycle();
            } else {
                LogUtil.v("Loaded image is null for " + filename);
            }
        }

        /**
         * Util function
         *
         * @return
         */
        List<String> getListOfFlairIds() {
            Pattern flairId = Pattern.compile("\\.flair-(\\w+)\\s*(\\{|\\,|\\:|)");
            Matcher matches = flairId.matcher(stylesheetString);

            List<String> flairIds = new ArrayList<>();
            while (matches.find()) {
                if (!flairIds.contains(matches.group(1))) flairIds.add(matches.group(1));
            }

            Collections.sort(flairIds);
            return flairIds;
        }
    }

    public static class FlairImageLoader extends ImageLoader {

        private volatile static FlairImageLoader instance;

        /** Returns singletone class instance */
        public static FlairImageLoader getInstance() {
            if (instance == null) {
                synchronized (ImageLoader.class) {
                    if (instance == null) {
                        instance = new FlairImageLoader();
                    }
                }
            }
            return instance;
        }
    }

    public static FlairImageLoader getFlairImageLoader(Context context) {
        if (imageLoader == null) {
            return initFlairImageLoader(context);
        } else {
            return imageLoader;
        }
    }

    public static FlairImageLoader imageLoader;


    public static File getCacheDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && context.getExternalCacheDir() != null) {
            return new File(context.getExternalCacheDir(), "flairs");
        }
        return new File(context.getCacheDir(), "flairs");
    }

    public static FlairImageLoader initFlairImageLoader(Context context) {
        long discCacheSize = 1024 * 1024 * 100; //100 MB limit
        DiskCache discCache;
        File dir = getCacheDirectory(context);
        int threadPoolSize;
        discCacheSize *= 100;
        threadPoolSize = 7;
        if (discCacheSize > 0) {
            try {
                dir.mkdir();
                discCache = new LruDiskCache(dir, new Md5FileNameGenerator(), discCacheSize);
            } catch (IOException e) {
                discCache = new UnlimitedDiskCache(dir);
            }
        } else {
            discCache = new UnlimitedDiskCache(dir);
        }

        options = new DisplayImageOptions.Builder().cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE)
                .cacheInMemory(false)
                .resetViewBeforeLoading(false)
                .build();
        ImageLoaderConfiguration config =
                new ImageLoaderConfiguration.Builder(context).threadPoolSize(threadPoolSize)
                        .denyCacheImageMultipleSizesInMemory()
                        .diskCache(discCache)
                        .threadPoolSize(4)
                        .imageDownloader(new OkHttpImageDownloader(context))
                        .defaultDisplayImageOptions(options)
                        .build();

        if (FlairImageLoader.getInstance().isInited()) {
            FlairImageLoader.getInstance().destroy();
        }

        imageLoader = FlairImageLoader.getInstance();
        imageLoader.init(config);
        return imageLoader;

    }

    public static DisplayImageOptions options;
}
