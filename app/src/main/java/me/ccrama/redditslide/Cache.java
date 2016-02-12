package me.ccrama.redditslide;

/**
 * Created by carlo_000 on 11/18/2015.
 */
public final class Cache {
    private Cache() {
    }

   /* public static void writeSubreddit(List<Submission> objects, String subreddit)  {
        StringBuilder s = new StringBuilder();
        s.append(System.currentTimeMillis()).append("<SEPARATOR>");
        for (Submission sub : objects) {
            s.append(sub.getDataNode().toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
        Reddit.appRestart.edit().putString(subreddit.toLowerCase() , finals).commit();
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "saved.txt");
        try {
            f.createNewFile();

            Files.write(finals, f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void writeSubredditJson(ArrayList<JsonNode> objects, String subreddit)  {
        StringBuilder s = new StringBuilder();
        s.append(System.currentTimeMillis()).append("<SEPARATOR>");
        for (JsonNode sub : objects) {
            s.append(sub.toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
        Reddit.appRestart.edit().putString(subreddit.toLowerCase() , finals).commit();
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "saved.txt");
        try {
            f.createNewFile();

            Files.write(finals, f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static OfflineSubreddit getSubreddit(String s) {
        Log.v(LogUtil.getTag(), "GETTING SUBREDDIT");
        if (Reddit.appRestart.contains(s.toLowerCase())) {
            return new OfflineSubreddit(Reddit.appRestart.getString(s.toLowerCase(), ""));
        } else {
            return null;
        }
    }


    public static boolean hasSub(String subredditPaginator) {
        return Reddit.appRestart.contains(subredditPaginator.toLowerCase());
    }*/
}