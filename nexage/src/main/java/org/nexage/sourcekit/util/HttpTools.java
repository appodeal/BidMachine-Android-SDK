//
//  HttpTools.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package org.nexage.sourcekit.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.text.TextUtils;

public class HttpTools {

	private static final String TAG = HttpTools.class.getName();

	public static void httpGetURL(final String url) {
		if (!TextUtils.isEmpty(url)) {
			final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
			try {
				singleThreadExecutor.execute(new Runnable() {
					@Override
					public void run() {
						HttpURLConnection conn = null;
						try {
							VASTLog.v(TAG, "connection to URL:" + url);
							URL httpUrl = new URL(url);

							HttpURLConnection.setFollowRedirects(true);
							conn = (HttpURLConnection) httpUrl.openConnection();
							conn.setConnectTimeout(5000);
							conn.setRequestProperty("Connection", "close");
							conn.setRequestMethod("GET");

							int code = conn.getResponseCode();
							VASTLog.v(TAG, "response code:" + code
									+ ", for URL:" + url);
						} catch (Exception e) {
							VASTLog.w(TAG, url + ": " + e.getMessage() + ":"
									+ e.toString());
						} finally {
							if (conn != null) {
								try {
									conn.disconnect();
								} catch (Exception ignore) {
								}
							}
						}
					}
				});
			} catch (Exception e) {
				VASTLog.e(TAG, e.getMessage());
			}
		} else {
			VASTLog.w(TAG, "url is null or empty");
		}
	}
}