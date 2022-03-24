package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.source.dash.manifest.AdaptationSet;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.Representation;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.ClippedTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by carlo_000 on 5/5/2016.
 */
public class GifCache {

    public static long discCacheSize = 100000000L; //100mb
    public static DiskCache discCache;

    public static void init(Context c) {
        File dir = ImageLoaderUtils.getCacheDirectoryGif(c);
        try {
            dir.mkdir();
            discCache = new LruDiskCache(dir, new Md5FileNameGenerator(), discCacheSize);
            ((LruDiskCache) discCache).setBufferSize(5 * 1024);

        } catch (IOException e) {
            e.printStackTrace();
            discCache = new UnlimitedDiskCache(dir);
        }
    }

    public static File getGif(URL url) {
        return discCache.get(url.toString());
    }

    public static void writeGif(String url, InputStream stream, IoUtils.CopyListener listener) {
        try {
            LogUtil.v(discCache.save(url, stream, listener) + "DONE ");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(stream);
        }
    }

    public static boolean fileExists(URL url) {
        return discCache.get(url.toString()) != null;
    }

    private static void showErrorDialog(final Activity a) {
        DialogUtil.showErrorDialog((MediaView) a);
    }

    private static void showFirstDialog(final Activity a) {
        DialogUtil.showFirstDialog((MediaView) a);
    }

    /**
     * Temporarily cache or permanently save a GIF
     *
     * @param uri       URL of the GIF
     * @param a
     * @param subreddit Subreddit for saving in sub-specific folders
     * @param save      Whether to permanently save the GIF of just temporarily cache it
     */
    public static void cacheSaveGif(Uri uri, Activity a, String subreddit, String submissionTitle, boolean save) {
        if (save) {
            try {
                Toast.makeText(a, a.getString(R.string.mediaview_notif_title), Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
            }
        }

        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog(a);
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog(a);
        } else {
            new AsyncTask<Void, Integer, Boolean>() {
                File outFile;
                NotificationManager notifMgr = ContextCompat.getSystemService(a, NotificationManager.class);

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (save) {
                        Notification notif = new NotificationCompat.Builder(a, Reddit.CHANNEL_IMG)
                                .setContentTitle(a.getString(R.string.mediaview_saving,
                                        uri.toString().replace("/DASHPlaylist.mpd", "")))
                                .setSmallIcon(R.drawable.ic_download)
                                .setProgress(0, 0, true)
                                .setOngoing(true)
                                .build();
                        notifMgr.notify(1, notif);
                    }
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    String folderPath = Reddit.appRestart.getString("imagelocation", "");

                    String subFolderPath = "";
                    if (SettingValues.imageSubfolders && !subreddit.isEmpty()) {
                        subFolderPath = File.separator + subreddit;
                    }

                    String extension = ".mp4";

                    outFile = FileUtil.getValidFile(folderPath, subFolderPath, submissionTitle, "", extension);

                    OutputStream out = null;
                    InputStream in = null;

                    try {
                        DataSource.Factory downloader =
                                new OkHttpDataSource.Factory(Reddit.client)
                                        .setUserAgent(a.getString(R.string.app_name));
                        DataSource.Factory cacheDataSourceFactory =
                                new CacheDataSource.Factory()
                                        .setCache(Reddit.videoCache)
                                        .setUpstreamDataSourceFactory(downloader);
                        if (uri.getLastPathSegment().endsWith("DASHPlaylist.mpd")) {
                            InputStream dashManifestStream = new DataSourceInputStream(cacheDataSourceFactory.createDataSource(),
                                    new DataSpec(uri));
                            DashManifest dashManifest = new DashManifestParser().parse(uri, dashManifestStream);
                            dashManifestStream.close();

                            Uri audioUri = null;
                            Uri videoUri = null;

                            for (int i = 0; i < dashManifest.getPeriodCount(); i++) {
                                for (AdaptationSet as : dashManifest.getPeriod(i).adaptationSets) {
                                    boolean isAudio = false;
                                    int bitrate = 0;
                                    String hqUri = null;
                                    for (Representation r : as.representations) {
                                        if (r.format.bitrate > bitrate) {
                                            bitrate = r.format.bitrate;
                                            hqUri = r.baseUrl;
                                        }
                                        if (MimeTypes.isAudio(r.format.sampleMimeType)) {
                                            isAudio = true;
                                        }
                                    }
                                    if (isAudio) {
                                        audioUri = Uri.parse(hqUri);
                                    } else {
                                        videoUri = Uri.parse(hqUri);
                                    }
                                }
                            }

                            if (audioUri != null) {
                                LogUtil.v("Downloading DASH audio from: " + audioUri);
                                DataSourceInputStream audioInputStream = new DataSourceInputStream(
                                        cacheDataSourceFactory.createDataSource(), new DataSpec(audioUri));
                                if (save) {
                                    FileUtils.copyInputStreamToFile(audioInputStream,
                                            new File(a.getCacheDir().getAbsolutePath(), "audio.mp4"));
                                } else {
                                    IOUtils.copy(audioInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
                                }
                                audioInputStream.close();
                            }
                            if (videoUri != null) {
                                LogUtil.v("Downloading DASH video from: " + videoUri);
                                DataSourceInputStream videoInputStream = new DataSourceInputStream(
                                        cacheDataSourceFactory.createDataSource(), new DataSpec(videoUri));
                                if (save) {
                                    FileUtils.copyInputStreamToFile(videoInputStream,
                                            new File(a.getCacheDir().getAbsolutePath(), "video.mp4"));
                                } else {
                                    IOUtils.copy(videoInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
                                }
                                videoInputStream.close();
                            }

                            if (!save) {
                                return true;
                            } else if (audioUri != null && videoUri != null) {
                                if (mux(new File(a.getCacheDir().getAbsolutePath(), "video.mp4").getAbsolutePath(),
                                        new File(a.getCacheDir().getAbsolutePath(), "audio.mp4").getAbsolutePath(),
                                        new File(a.getCacheDir().getAbsolutePath(), "muxed.mp4").getAbsolutePath())) {
                                    in = new FileInputStream(new File(a.getCacheDir().getAbsolutePath(), "muxed.mp4"));
                                } else {
                                    throw new IOException("Muxing failed!");
                                }
                            } else {
                                in = new FileInputStream(new File(a.getCacheDir().getAbsolutePath(), "video.mp4"));
                            }
                        } else {
                            in = new DataSourceInputStream(cacheDataSourceFactory.createDataSource(), new DataSpec(uri));
                        }

                        out = save ? new FileOutputStream(outFile) : NullOutputStream.NULL_OUTPUT_STREAM;
                        IOUtils.copy(in, out);
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e("Error saving GIF called with: "
                                + "from = ["
                                + uri
                                + "], in = ["
                                + in
                                + "]");
                        return false;
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            LogUtil.e("Error closing GIF called with: "
                                    + "from = ["
                                    + uri
                                    + "], out = ["
                                    + out
                                    + "]");
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    super.onPostExecute(success);
                    if (save) {
                        notifMgr.cancel(1);
                        if (success) {
                            doNotifGif(outFile, a);
                        } else {
                            showErrorDialog(a);
                        }
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Mux a video and audio file (e.g. from DASH) together into a single video
     *
     * @param videoFile  Video file
     * @param audioFile  Audio file
     * @param outputFile File to output muxed video to
     * @return Whether the muxing completed successfully
     */
    private static boolean mux(String videoFile, String audioFile, String outputFile) {
        Movie rawVideo;
        try {
            rawVideo = MovieCreator.build(videoFile);
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }

        Movie audio;
        try {
            audio = MovieCreator.build(audioFile);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        Track audioTrack = audio.getTracks().get(0);
        Track videoTrack = rawVideo.getTracks().get(0);
        Movie video = new Movie();

        ClippedTrack croppedTrackAudio = new ClippedTrack(audioTrack, 0, audioTrack.getSamples().size());
        video.addTrack(croppedTrackAudio);
        ClippedTrack croppedTrackVideo = new ClippedTrack(videoTrack, 0, videoTrack.getSamples().size());
        video.addTrack(croppedTrackVideo);

        Container out = new DefaultMp4Builder().build(video);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        GifUtils.BufferedWritableFileByteChannel byteBufferByteChannel =
                new GifUtils.BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Create a notification that opens a newly-saved GIF
     *
     * @param f File referencing the GIF
     * @param c
     */
    public static void doNotifGif(File f, Activity c) {
        MediaScannerConnection.scanFile(c,
                new String[]{f.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        final Intent shareIntent = FileUtil.getFileIntent(f, new Intent(Intent.ACTION_VIEW), c);
                        PendingIntent contentIntent =
                                PendingIntent.getActivity(c, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                        Notification notif =
                                new NotificationCompat.Builder(c, Reddit.CHANNEL_IMG).setContentTitle(c.getString(R.string.gif_saved))
                                        .setSmallIcon(R.drawable.ic_save)
                                        .setContentIntent(contentIntent)
                                        .build();

                        NotificationManager mNotificationManager =
                                ContextCompat.getSystemService(c, NotificationManager.class);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify((int) System.currentTimeMillis(), notif);
                        }
                    }
                }
        );
    }
}
