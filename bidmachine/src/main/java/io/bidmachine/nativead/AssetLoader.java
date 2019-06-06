package io.bidmachine.nativead;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.nexage.sourcekit.vast.model.VASTModel;

import java.io.File;
import java.util.ArrayList;

import io.bidmachine.AdProcessCallback;
import io.bidmachine.MediaAssetType;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.nativead.tasks.DownloadImageTask;
import io.bidmachine.nativead.tasks.DownloadVastVideoTask;
import io.bidmachine.nativead.tasks.DownloadVideoTask;
import io.bidmachine.nativead.utils.NativeMediaPrivateData;
import io.bidmachine.nativead.utils.NativeNetworkExecutor;
import io.bidmachine.nativead.utils.NativePrivateData;
import io.bidmachine.utils.BMError;

class AssetLoader {

    private static final String DIR_NAME = "native_cache_image";

    private final ArrayList<Runnable> pendingTasks = new ArrayList<>();

    private Context context;
    private NativeRequest adRequest;
    private AdProcessCallback callback;
    private NativePrivateData nativeData;
    private NativeMediaPrivateData nativeMediaData;

    AssetLoader(@NonNull Context context,
                @NonNull NativeRequest request,
                @NonNull AdProcessCallback callback,
                @NonNull NativePrivateData nativeData,
                @NonNull NativeMediaPrivateData nativeMediaData) {
        this.context = context;
        this.adRequest = request;
        this.callback = callback;
        this.nativeData = nativeData;
        this.nativeMediaData = nativeMediaData;
    }

    void downloadNativeAdsImages() {
        startDownloadTask();
        checkTasksCount();
    }

    private void startDownloadTask() {
        final String iconUrl = nativeData.getIconUrl();
        final String imageUrl = nativeData.getImageUrl();
        final String videoUrl = nativeData.getVideoUrl();
        final String videoAdm = nativeData.getVideoAdm();
        if (adRequest.containsAssetType(MediaAssetType.Icon)) {
            createIconTask(context, iconUrl);
        }
        if (adRequest.containsAssetType(MediaAssetType.Image)
                || adRequest.containsAssetType(MediaAssetType.Video)) {
            createImageTask(context, imageUrl);
            if (!TextUtils.isEmpty(videoUrl)) {
                createVideoTask(context, videoUrl);
            } else if (!TextUtils.isEmpty(videoAdm)) {
                createVastVideoTask(context, videoAdm);
            }
        }
        if (pendingTasks.isEmpty()) {
            checkTasksCount();
        } else {
            for (Runnable task : pendingTasks) {
                NativeNetworkExecutor.getInstance().execute(task);
            }
        }
    }

    private void createIconTask(final Context context, String url) {
        if (url != null && !url.isEmpty()) {
            pendingTasks.add(DownloadImageTask.newBuilder(context, url)
                    .setOnCacheImageListener(new DownloadImageTask.OnCacheImageListener() {
                        @Override
                        public void onPathSuccess(DownloadImageTask task, Uri imagePath) {
                            nativeMediaData.setIconUri(imagePath);
                            removePendingTask(task);

                        }

                        @Override
                        public void onImageSuccess(DownloadImageTask task, Bitmap imageBitmap) {
                            nativeMediaData.setIconBitmap(imageBitmap);
                            removePendingTask(task);
                        }

                        @Override
                        public void onFail(DownloadImageTask task) {
                            removePendingTask(task);
                        }
                    })
                    .build());
        }
    }

    private void createImageTask(final Context context, String url) {
        if (url != null && !url.isEmpty()) {
            pendingTasks.add(DownloadImageTask.newBuilder(context, url)
                    .setCheckAspectRatio(true)
                    .setOnCacheImageListener(new DownloadImageTask.OnCacheImageListener() {
                        @Override
                        public void onPathSuccess(DownloadImageTask task, Uri imagePath) {
                            nativeMediaData.setImageUri(imagePath);
                            removePendingTask(task);
                        }

                        @Override
                        public void onImageSuccess(DownloadImageTask task, Bitmap imageBitmap) {
                            nativeMediaData.setImageBitmap(imageBitmap);
                            removePendingTask(task);
                        }

                        @Override
                        public void onFail(DownloadImageTask task) {
                            removePendingTask(task);
                        }
                    })
                    .build());
        }
    }

    private void createVideoTask(final Context context, String url) {
        pendingTasks.add(new DownloadVideoTask(context,
                new DownloadVideoTask.OnLoadedListener() {
                    @Override
                    public void onVideoLoaded(DownloadVideoTask task, Uri videoFileUri) {
                        nativeMediaData.setVideoUri(videoFileUri);
                        if (TextUtils.isEmpty(nativeData.getImageUrl())
                                && videoFileUri != null
                                && videoFileUri.getPath() != null
                                && new File(videoFileUri.getPath()).exists()) {
                            nativeMediaData.setImageUri(
                                    Uri.parse(Utils.retrieveAndSaveFrame(context, videoFileUri, DIR_NAME)));
                        }
                        removePendingTask(task);
                    }

                    @Override
                    public void onVideoLoadingError(DownloadVideoTask task) {
                        removePendingTask(task);
                    }
                }, url));
    }

    private void createVastVideoTask(final Context context, String vastVideoAdm) {
        pendingTasks.add(new DownloadVastVideoTask(context,
                new DownloadVastVideoTask.OnLoadedListener() {
                    @Override
                    public void onVideoLoaded(DownloadVastVideoTask task, Uri videoFileUri, VASTModel vastModel) {
                        nativeMediaData.setVideoUri(videoFileUri);
                        nativeMediaData.setVideoVastModel(vastModel);

                        if (TextUtils.isEmpty(nativeData.getImageUrl())
                                && videoFileUri != null
                                && videoFileUri.getPath() != null
                                && new File(videoFileUri.getPath()).exists()) {
                            nativeMediaData.setImageUri(
                                    Uri.parse(Utils.retrieveAndSaveFrame(context, videoFileUri, DIR_NAME)));
                        }
                        removePendingTask(task);
                    }

                    @Override
                    public void onVideoLoadingError(DownloadVastVideoTask task) {
                        removePendingTask(task);
                    }
                }, vastVideoAdm));
    }

    private void removePendingTask(Runnable task) {
        pendingTasks.remove(task);
        checkTasksCount();
    }

    private void checkTasksCount() {
        if (pendingTasks.isEmpty()) {
            notifyNativeCallback();
        }
    }

    private synchronized void notifyNativeCallback() {
        if (!isAssetsValid()) {
            callback.processLoadFail(BMError.IncorrectAdUnit);
            callback.processDestroy();
        } else {
            callback.processLoadSuccess();
        }
    }

    private boolean isAssetsValid() {
        try {
            return isIconValid() && isImageValid() && isVideoValid();
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }

    private boolean isIconValid() {
        if (adRequest.containsAssetType(MediaAssetType.Icon)) {
            return !TextUtils.isEmpty(nativeData.getIconUrl())
                    || nativeMediaData.getIconBitmap() != null;
        }
        return true;
    }

    private boolean isImageValid() {
        if (adRequest.containsAssetType(MediaAssetType.Image)
                && !adRequest.containsAssetType(MediaAssetType.Video)) {
            return !TextUtils.isEmpty(nativeData.getImageUrl())
                    || nativeMediaData.getImageBitmap() != null;
        }
        return true;
    }

    private boolean isVideoValid() {
        if (adRequest.containsAssetType(MediaAssetType.Video)
                && !adRequest.containsAssetType(MediaAssetType.Image)) {
            return nativeMediaData.hasVideo() && nativeMediaData.getVideoUri() != null;
        }
        return true;
    }

}
