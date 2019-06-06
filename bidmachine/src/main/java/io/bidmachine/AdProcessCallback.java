package io.bidmachine;

import io.bidmachine.utils.BMError;

public interface AdProcessCallback {

    void processLoadSuccess();

    void processLoadFail(final BMError error);

    void processShown();

    void processShowFail(final BMError error);

    void processClicked();

    void processImpression();

    void processFinished();

    void processClosed(final boolean finished);

    void processExpired();

    void processDestroy();

}
