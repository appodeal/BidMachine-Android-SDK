package io.bidmachine.models;

public interface IPriceFloorParams<SelfType> {

    SelfType addPriceFloor(double value);

    SelfType addPriceFloor(String id, double value);

}
