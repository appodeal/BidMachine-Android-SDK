package org.nexage.sourcekit.vast.view;

import android.content.Context;
import android.content.Intent;

import org.nexage.sourcekit.util.NetworkTools;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.VASTPlayer;
import org.nexage.sourcekit.vast.activity.VASTActivity;
import org.nexage.sourcekit.vast.activity.VPAIDActivity;

public class AppodealVASTPlayer extends VASTPlayer {

    private static final String TAG = "AppodealVASTPlayer";

    public AppodealVASTPlayer(Context context) {
        super(context);
    }

    public void play(Context context, Video.Type type, VASTPlayerListener vastListener) {
        VASTLog.d(TAG, "play");
        VASTPlayer.listener = vastListener;
        if (vastModel != null) {
            if (NetworkTools.connectedToInternet(context)) {
                Intent playerIntent;
                if (vastModel.getPickedMediaFileType().equals("application/javascript")) {
                    playerIntent = new Intent(context, VPAIDActivity.class);
                    String url = getXmlUrl();
                    if (url == null || url.isEmpty() || url.equals(" ")) {
                        sendError(ERROR_POST_VALIDATION, vastListener);
                        return;
                    }
                    playerIntent.putExtra("android.net.url", url);
                    playerIntent.putExtra("com.nexage.android.vast.player.vastModel", vastModel);
                    playerIntent.putExtra("com.nexage.android.vast.player.type", type);
                } else {
                    playerIntent = new Intent(context, VASTActivity.class);
                    playerIntent.putExtra("com.nexage.android.vast.player.vastModel", vastModel);
                    playerIntent.putExtra("com.nexage.android.vast.player.type", type);
                    if (this.fileUrl != null) {
                        playerIntent.putExtra("android.net.url", fileUrl);
                    }
                    playerIntent.putExtra("com.nexage.android.vast.player.maxDuration", maxDuration);
                    playerIntent.putExtra("com.nexage.android.vast.player.closeTime", closeTime);
                    if (segmentId != null) {
                        playerIntent.putExtra("com.nexage.android.vast.player.segmentId", segmentId);
                    }
                    if (placementId != null) {
                        playerIntent.putExtra("com.nexage.android.vast.player.placementId", placementId);
                    }
                    playerIntent.putExtra("com.nexage.android.vast.player.useLayoutInCompanion", useLayoutInCompanion);
                }
                context.startActivity(playerIntent);
            } else {
                sendError(ERROR_NO_NETWORK, vastListener);
            }
        } else {
            VASTLog.w(TAG, "vastModel is null; nothing to play");
        }
    }
}
