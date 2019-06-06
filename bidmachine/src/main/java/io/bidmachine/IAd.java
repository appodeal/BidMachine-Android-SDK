package io.bidmachine;

import android.support.annotation.Nullable;

import io.bidmachine.models.AuctionResult;

public interface IAd<SelfType extends IAd, AdRequestType extends AdRequest> {

    SelfType load(AdRequestType request);

    @Nullable
    AuctionResult getAuctionResult();

    boolean isLoading();

    boolean isLoaded();

    boolean canShow();

    void destroy();

}
