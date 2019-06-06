package io.bidmachine.nativead.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Pair;

import org.nexage.sourcekit.util.DefaultMediaPicker;
import org.nexage.sourcekit.vast.model.VASTModel;
import org.nexage.sourcekit.vast.processor.VASTMediaPicker;
import org.nexage.sourcekit.vast.processor.VASTProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.nativead.utils.NoSSLv3SocketFactory;

public class DownloadVastVideoTask implements Runnable {

    private static final String SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(mp4|3gpp|mp2t|webm|matroska)";
    private static final int serverTimeOut = 20000;
    private static final int RADIX = 10 + 26;

    private final Handler handler;
    private final int RESULT_FAIL = 0;
    private final int RESULT_SUCCESS = 1;

    private Context context;
    private OnLoadedListener mListener;
    private String mVideoTag;
    private File cacheDir;

    private boolean initialized;

    public interface OnLoadedListener {
        void onVideoLoaded(DownloadVastVideoTask task, Uri videoFileUri, VASTModel vastModel);

        void onVideoLoadingError(DownloadVastVideoTask task);
    }

    public DownloadVastVideoTask(Context context, OnLoadedListener listener, String tag) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (mListener != null) {
                    switch (msg.what) {
                        case RESULT_SUCCESS:
                            @SuppressWarnings("unchecked")
                            Pair<Uri, VASTModel> pair = (Pair<Uri, VASTModel>) msg.obj;
                            mListener.onVideoLoaded(DownloadVastVideoTask.this, pair.first, pair.second);
                            break;
                        case RESULT_FAIL:
                            mListener.onVideoLoadingError(DownloadVastVideoTask.this);
                            break;
                    }
                }
            }
        };
        if (context == null || tag == null || !Utils.canUseExternalFilesDir(context)) {
            listener.onVideoLoadingError(this);
            return;
        }

        mListener = listener;
        mVideoTag = tag;
        File externalStorage = context.getExternalFilesDir(null);
        if (externalStorage != null) {
            String dir = externalStorage.getPath() + "/native_video/";
            cacheDir = new File(dir);
            if (!cacheDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
            }
        } else {
            listener.onVideoLoadingError(this);
            return;
        }
        initialized = true;
    }

    @Override
    public void run() {
        if (!initialized) {
            handler.sendEmptyMessage(RESULT_FAIL);
            return;
        }
        InputStream inputStream = null;
        try {
            VASTMediaPicker mediaPicker = new DefaultMediaPicker(context);
            VASTProcessor processor = new VASTProcessor(mediaPicker);
            int error = processor.process(mVideoTag);
            if (error == 0) {
                VASTModel vastModel = processor.getModel();
                if (vastModel.getPickedMediaFileType().matches(SUPPORTED_VIDEO_TYPE_REGEX)) {
                    String videoUrl = vastModel.getPickedMediaFileURL();
                    inputStream = setupConnection(videoUrl);
                    String fileName = generateFileName(videoUrl);
                    File file = new File(cacheDir, fileName);
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bufferLength;
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                    }
                    fileOutput.close();

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
                            file.getPath(),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    if (thumb != null) {
                        Message message = handler.obtainMessage(RESULT_SUCCESS, new Pair<>(Uri.fromFile(file), vastModel));
                        handler.sendMessage(message);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            closeInputStream(inputStream);
        }

        handler.sendEmptyMessage(RESULT_FAIL);
    }

    private InputStream setupConnection(String videoUrl) throws IOException {
        try {
            URL url = new URL(videoUrl);
            URLConnection connection = url.openConnection();
            setupNoSSLv3(connection);
            connection.setConnectTimeout(serverTimeOut);
            connection.setReadTimeout(serverTimeOut);
            connection.connect();
            return connection.getInputStream();
        } catch (IOException ex) {
            Logger.log(ex.getMessage());
            Uri.Builder builder = Uri.parse(videoUrl).buildUpon();
            builder.scheme("http");
            URL url = new URL(builder.build().toString());
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(serverTimeOut);
            connection.setReadTimeout(serverTimeOut);
            connection.connect();
            return connection.getInputStream();
        }
    }

    private void setupNoSSLv3(URLConnection connection) {
        try {
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                SSLSocketFactory delegate = httpsURLConnection.getSSLSocketFactory();
                httpsURLConnection.setSSLSocketFactory(new NoSSLv3SocketFactory(delegate));
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private String generateFileName(String imageUri) {
        byte[] md5 = Utils.getMD5(imageUri.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(RADIX);
    }

}