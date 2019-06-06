package io.bidmachine.test.app.params;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.bidmachine.test.app.ParamsHelper;

public class ExtraParamsFragment extends BaseParamsFragment {

    @Override
    protected void prepareView(Context context, ViewGroup parent, final ParamsHelper paramsHelper) {
        final Map<String, String> currentParams = paramsHelper.getExtraParamsMap();
        final ArrayList<ListItemContainer> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : currentParams.entrySet()) {
            items.add(new ListItemContainer(entry.getKey(), entry.getValue()));
        }
        bindParamWidget(context, parent, "Extra",
                new ListParamsWidget("Extra", "Add Extra", ListParamsWidget.Mode.KeyValue,
                        items.toArray(new ListItemContainer[items.size()]),
                        new ParamWidget.ChangeTracker<ListItemContainer[]>() {
                            @Override
                            public void onChanged(ParamWidget widget, ListItemContainer[] param) {
                                Log.e("TEST", "param size: " + param.length);
                                final HashMap<String, String> outMap = new HashMap<>();
                                for (ListItemContainer container : param) {
                                    outMap.put(container.getKey(), container.getValue());
                                }
                                paramsHelper.setExtraParams(outMap);
                            }
                        }));
    }

}
