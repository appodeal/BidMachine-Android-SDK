package io.bidmachine.models;

public interface IExtraParams<SelfType> {

    SelfType addExtra(String key, String value);

}
