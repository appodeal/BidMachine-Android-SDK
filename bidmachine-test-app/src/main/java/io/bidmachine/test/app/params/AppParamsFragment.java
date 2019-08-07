package io.bidmachine.test.app.params;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import io.bidmachine.test.app.ParamsHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class AppParamsFragment extends BaseParamsFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ScrollView scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        scrollView.setBackgroundColor(Color.WHITE);

        final View origin = super.onCreateView(inflater, container, savedInstanceState);
        origin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout parent = new LinearLayout(getContext());
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(origin);

        Button btnFetchInfo = new Button(getContext());
        btnFetchInfo.setText("Fetch info");
        btnFetchInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchInfo(v, false);
            }
        });

        Button btnFetchInfoForce = new Button(getContext());
        btnFetchInfoForce.setText("Fetch info (force)");
        btnFetchInfoForce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchInfo(v, true);
            }
        });

        parent.addView(btnFetchInfo);
        parent.addView(btnFetchInfoForce);

        scrollView.addView(parent);
        return scrollView;
    }

    @Override
    protected void prepareView(Context context, ViewGroup parent, final ParamsHelper paramsHelper) {
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Init url", paramsHelper.getInitUrl(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setInitUrl(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Seller Id", paramsHelper.getSellerId(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setSellerId(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("App Bundle", paramsHelper.getAppBundle(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setAppBundle(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("App Name", paramsHelper.getAppName(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setAppName(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("App Version", paramsHelper.getAppVersion(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setAppVersion(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Store Url", paramsHelper.getStoreUrl(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setStoreUrl(param);
                            }
                        }));
    }

    private void fetchInfo(View view, final boolean force) {
        final ParamsHelper helper = ParamsHelper.getInstance(getContext(), obtainAdsType());
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        final WeakReference<View> referenceView = new WeakReference<>(view);
        final String bundle = helper.getAppBundle();
        if (TextUtils.isEmpty(bundle)) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String name = null;
                    String version = null;
                    String storeUrl = null;
                    if (!force && preferences.contains(bundle)) {
                        String saved = preferences.getString(bundle, null);
                        if (!TextUtils.isEmpty(saved)) {
                            try {
                                JSONObject jsonObject = new JSONObject(saved);
                                name = jsonObject.getString("name");
                                version = jsonObject.getString("version");
                                storeUrl = jsonObject.getString("storeUrl");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(version)) {
                        storeUrl = "https://play.google.com/store/apps/details?id=" + helper.getAppBundle();

                        final Document document = Jsoup.connect(storeUrl + "&hl=en")
                                .timeout(30000)
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com")
                                .get();

                        name = document
                                .select("[itemprop=name]")
                                .first()
                                .text();

                        version = document
                                .select("[class=hAyfc]")
                                .get(3)
                                .select("[class=IQ1z0d]")
                                .text();

                    }

                    if (!TextUtils.isEmpty(name)
                            && !TextUtils.isEmpty(version)
                            && !TextUtils.isEmpty(storeUrl)) {
                        helper.setAppName(name);
                        helper.setAppVersion(version);
                        helper.setStoreUrl(storeUrl);
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("name", name);
                            jsonObject.put("version", version);
                            jsonObject.put("storeUrl", storeUrl);
                            preferences.edit().putString(helper.getAppBundle(), jsonObject.toString()).apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    View view = referenceView.get();
                    if (view != null) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                recreateView();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
