//
//  VastPlayer.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package org.nexage.sourcekit.vast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import org.nexage.sourcekit.util.DefaultMediaPicker;
import org.nexage.sourcekit.util.NetworkTools;
import org.nexage.sourcekit.util.Utils;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.activity.VASTActivity;
import org.nexage.sourcekit.vast.activity.VPAIDActivity;
import org.nexage.sourcekit.vast.model.VASTModel;
import org.nexage.sourcekit.vast.processor.VASTMediaPicker;
import org.nexage.sourcekit.vast.processor.VASTProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VASTPlayer {
    private boolean precache = false;
    private static final String TAG = "VASTPlayer";
    private final String cacheDirectory = "/vast_rtb_cache/";
    private final int cacheSize = 5;
    private String xmlUrl;
    protected int maxDuration = 0;
    private boolean disableLongVideo = true;
    protected int closeTime;
    protected boolean useLayoutInCompanion = true;

    public static final String VERSION = "1.3";

    // errors that can be returned in the vastError callback method of the
    // VASTPlayerListener
    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_NETWORK = 1;
    public static final int ERROR_XML_OPEN_OR_READ = 2;
    public static final int ERROR_XML_PARSE = 3;
    public static final int ERROR_SCHEMA_VALIDATION = 4; // not used in SDK, only in sourcekit
    public static final int ERROR_POST_VALIDATION = 5;
    public static final int ERROR_EXCEEDED_WRAPPER_LIMIT = 6;
    public static final int ERROR_VIDEO_PLAYBACK = 7;
    public static final int ERROR_CACHE = 8;
    public static final int ERROR_VIDEO_DURATION = 9;

    protected String segmentId;
    protected String placementId;

    private Context context;
    protected Uri fileUrl;

    public interface VASTPlayerListener {
        void vastReady();

        void vastError(int error);

        void vastShown();

        void vastClick(String url, Activity activity);

        void vastComplete();

        void vastDismiss(boolean finished);
    }

    public static VASTPlayerListener listener;
    protected VASTModel vastModel;

    public VASTPlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    public void loadVideoWithUrl(final String urlString, final VASTPlayerListener listener) {
        VASTLog.d(TAG, "loadVideoWithUrl " + urlString);
        vastModel = null;
        if (NetworkTools.connectedToInternet(context)) {
            final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
            try {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader in = null;
                        StringBuffer sb;
                        try {
                            URL url = new URL(urlString);
                            in = new BufferedReader(new InputStreamReader(url.openStream()));
                            sb = new StringBuffer();
                            String line;
                            while ((line = in.readLine()) != null) {
                                sb.append(line).append(System.getProperty("line.separator"));
                            }
                        } catch (Exception e) {
                            sendError(ERROR_XML_OPEN_OR_READ, listener);
                            VASTLog.e(TAG, e.getMessage(), e);
                            return;
                        } finally {
                            try {
                                if (in != null) {
                                    in.close();
                                }
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                        loadVideoWithData(sb.toString(), listener);
                    }
                });
            } catch (Exception e) {
                sendError(ERROR_CACHE, listener);
            }
        } else {
            sendError(ERROR_NO_NETWORK, listener);
        }
    }

    public void loadVideoWithData(final String xmlData, final VASTPlayerListener vastListener) {
        VASTLog.v(TAG, "loadVideoWithData\n" + xmlData);
        vastModel = null;
        if (NetworkTools.connectedToInternet(context)) {
            final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
            try {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        VASTMediaPicker mediaPicker = new DefaultMediaPicker(context);
                        VASTProcessor processor = new VASTProcessor(mediaPicker);
                        int error = processor.process(xmlData);
                        if (error == ERROR_NONE) {
                            vastModel = processor.getModel();
                            if (precache) {
                                try {
                                    cache(vastModel.getPickedMediaFileURL());
                                    if (fileUrl == null || !new File(fileUrl.getPath()).exists()) {
                                        VASTLog.d(TAG, "fileUrl is null");
                                        sendError(ERROR_CACHE, vastListener);
                                    } else {
                                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(fileUrl.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                                        if (thumb != null) {
                                            Bitmap emptyBitmap = Bitmap.createBitmap(thumb.getWidth(), thumb.getHeight(), thumb.getConfig());
                                            if (!thumb.equals(emptyBitmap)) {
                                                try {
                                                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                                    mediaMetadataRetriever.setDataSource(context, fileUrl);
                                                    String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                    long timeInMills = Long.parseLong(time);
                                                    if (maxDuration == 0 || timeInMills <= maxDuration || (timeInMills > maxDuration && !disableLongVideo)) {
                                                        sendReady(vastListener);
                                                    } else {
                                                        sendError(ERROR_VIDEO_DURATION, vastListener);
                                                    }
                                                } catch (Exception e) {
                                                    VASTLog.e(TAG, e.getMessage());
                                                    sendError(ERROR_VIDEO_PLAYBACK, vastListener);
                                                }
                                            } else {
                                                VASTLog.d(TAG, "empty thumbnail");
                                                sendError(ERROR_VIDEO_PLAYBACK, vastListener);
                                            }
                                        } else {
                                            VASTLog.d(TAG, "video file not supported");
                                            sendError(ERROR_VIDEO_PLAYBACK, vastListener);
                                        }
                                        clearCache();
                                    }
                                } catch (Exception ignore) {
                                    VASTLog.d(TAG, "exception when to cache file");
                                    sendError(ERROR_CACHE, vastListener);
                                }
                            }
                        } else {
                            sendError(error, vastListener);
                        }
                    }
                });
            } catch (Exception e) {
                sendError(ERROR_CACHE, vastListener);
            }
        } else {
            sendError(ERROR_NO_NETWORK, vastListener);
        }
    }

    private String getCacheDirName() {
        File externalStorage = context.getExternalFilesDir(null);
        if (externalStorage != null) {
            return externalStorage.getPath() + cacheDirectory;
        }
        return null;
    }

    private void cache(String pickedMediaFileURL) throws Exception {
        String cacheDir = getCacheDirName();
        if (cacheDir == null) {
            throw new FileNotFoundException("No dir for caching file");
        }
        File dir = new File(cacheDir);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        int fileNameLength = 230 - dir.getPath().length();
        String tempName = "temp" + System.currentTimeMillis();
        String fileName = pickedMediaFileURL.substring(0, Math.min(fileNameLength, pickedMediaFileURL.length())).replace("/", "").replace(":", "");
        File cachedFile = new File(dir, fileName);
        if (cachedFile.exists()) {
            this.fileUrl = Uri.fromFile(cachedFile);
        } else {
            File file = new File(dir, tempName);
            URL url = new URL(pickedMediaFileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            FileOutputStream fileOutput = new FileOutputStream(file);
            long totalSize = connection.getContentLength();
            long downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = is.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            if (totalSize == downloadedSize) {
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(new File(dir, fileName));
            }
            this.fileUrl = Uri.fromFile(new File(dir, fileName));
        }
    }

    class Pair implements Comparable {
        public long mLastModified;
        public File mFile;

        public Pair(File file) {
            mFile = file;
            mLastModified = file.lastModified();
        }

        public int compareTo(@NonNull Object o) {
            Pair file = ((Pair) o);
            return mLastModified > file.mLastModified ? -1 : mLastModified == file.mLastModified ? 0 : 1;
        }
    }

    private void clearCache() {
        try {
            String cacheDir = getCacheDirName();
            if (cacheDir == null) {
                return;
            }
            File dir = new File(cacheDir);
            File[] files = dir.listFiles();
            if (files != null && files.length > cacheSize) {
                Pair[] pairs = new Pair[files.length];
                for (int i = 0; i < files.length; i++) {
                    pairs[i] = new Pair(files[i]);
                }

                Arrays.sort(pairs);

                for (int i = 0; i < files.length; i++) {
                    files[i] = pairs[i].mFile;
                }

                for (int i = cacheSize; i < files.length; i++) {
                    if (!Uri.fromFile(files[i]).equals(this.fileUrl)) {
                        //noinspection ResultOfMethodCallIgnored
                        files[i].delete();
                    }
                }

            }
        } catch (Exception e) {
            VASTLog.e(TAG, e.getMessage());
        }
    }

    public void play(Context context, Video.Type type, boolean autoClose, VASTPlayerListener vastListener) {
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
                    playerIntent.putExtra("com.nexage.android.vast.player.autoClose", autoClose);
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

    private void sendReady(final VASTPlayerListener vastListener) {
        VASTLog.d(TAG, "sendReady");
        if (vastListener != null) {
            Utils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastListener.vastReady();
                }
            });
        }
    }

    private void sendVastModelError(int errorCode) {
        try {
            if (vastModel != null) {
                vastModel.sendError(errorCode);
            }
        } catch (Exception ex) {
            VASTLog.e(TAG, ex.getMessage());
        }
    }

    protected void sendError(final int error, final VASTPlayerListener vastListener) {
        VASTLog.d(TAG, "sendError, code: " + error);

        switch (error) {
            case ERROR_XML_PARSE:
                sendVastModelError(VASTModel.ERROR_CODE_XML_PARSING);
                break;
            case ERROR_POST_VALIDATION:
                sendVastModelError(VASTModel.ERROR_CODE_XML_VALIDATE);
                break;
            case ERROR_CACHE:
                sendVastModelError(VASTModel.ERROR_CODE_BAD_URI);
                break;
            case ERROR_VIDEO_PLAYBACK:
                sendVastModelError(VASTModel.ERROR_CODE_BAD_FILE);
                break;
            case ERROR_VIDEO_DURATION:
                sendVastModelError(VASTModel.ERROR_CODE_DURATION);
                break;
        }

        if (vastListener != null) {
            Utils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vastListener.vastError(error);
                }
            });
        }
    }

    public void setPrecache(boolean precache) {
        this.precache = precache;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }

    public boolean checkFile() {
        try {
            if (this.fileUrl != null && new File(fileUrl.getPath()).exists()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public void setDisableLongVideo(boolean disableLongVideo) {
        this.disableLongVideo = disableLongVideo;
    }

    public void setCloseTime(int closeTime) {
        this.closeTime = closeTime;
    }

    public void setSegmentAndPlacement(String segmentId, String placementId) {
        this.placementId = placementId;
        this.segmentId = segmentId;
    }

    public void setUseLayoutInCompanion(boolean useLayoutInCompanion) {
        this.useLayoutInCompanion = useLayoutInCompanion;
    }
}