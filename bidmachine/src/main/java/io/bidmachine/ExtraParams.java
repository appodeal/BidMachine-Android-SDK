package io.bidmachine;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.IExtraParams;
import io.bidmachine.models.RequestParams;

public class ExtraParams extends RequestParams implements IExtraParams<ExtraParams> {

    private Map<String, String> extrasMap;

    Map<String, String> getExtrasMap() {
        return extrasMap;
    }

    @Override
    public ExtraParams addExtra(String key, String value) {
        if (extrasMap == null) {
            extrasMap = new HashMap<>();
        }
        extrasMap.put(key, value);
        return this;
    }

}
