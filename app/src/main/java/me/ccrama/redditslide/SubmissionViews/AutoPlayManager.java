package me.ccrama.redditslide.SubmissionViews;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.HashMap;

import me.ccrama.redditslide.Views.MediaVideoView;

/**
 * Created by Carlos on 10/15/2017.
 */

public class AutoPlayManager {
    public HashMap<String, AutoPlayer> players = new HashMap<>();
    public AutoPlayer currentPlayer;

    public AutoPlayManager(RecyclerView r) {
        r.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    if (currentPlayer != null) {
                        currentPlayer.pause();
                    }
                } else {
                    AutoPlayer player = chooseCurrentPlayer();
                    if (player != currentPlayer && currentPlayer != null) {
                        currentPlayer.kill();
                    }
                    currentPlayer = player;
                    currentPlayer.beginLoad();
                }
            }

            private AutoPlayer chooseCurrentPlayer() {
                AutoPlayer bigger = (AutoPlayer) players.values().toArray()[0];
                Float size = visibleAreaOffset(bigger);
                for(AutoPlayer p : players.values()){
                    Float pSize = visibleAreaOffset(p);
                    if(pSize > size ){
                        size = pSize;
                        bigger = p;
                    }
                }
                return bigger;
            }
        });
    }

    public void registerPlayer(String tag, AutoPlayer player) {
        players.put(tag, player);
    }

    //Code by Nam Nguyen https://github.com/eneim/toro/toro-core/src/main/java/im/ene/toro/ToroUtil.java
    public static float visibleAreaOffset(AutoPlayer player) {

        View playerView = player.getView();
        Rect drawRect = new Rect();
        playerView.getDrawingRect(drawRect);
        int drawArea = drawRect.width() * drawRect.height();

        Rect playerRect = new Rect();
        boolean visible = playerView.getGlobalVisibleRect(playerRect, new Point());

        float offset = 0.f;
        if (visible && drawArea > 0) {
            int visibleArea = playerRect.height() * playerRect.width();
            offset = visibleArea / (float) drawArea;
        }
        return offset;
    }

    public interface AutoPlayer {
        void resume();
        void pause();
        void beginLoad();
        MediaVideoView getView();
        void kill();
    }
}
