package me.ccrama.redditslide;

import me.ccrama.redditslide.PostLoader;

public class PostLoaderManager {
    private static PostLoader instance;

    private PostLoaderManager() {}

    public static PostLoader getInstance() {
        return instance;
    }

    public static void setInstance(PostLoader loader)
    {
        instance = loader;
    }
}
