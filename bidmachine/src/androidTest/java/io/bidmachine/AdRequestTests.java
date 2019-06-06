package io.bidmachine;

public interface AdRequestTests {

    void testLoadSuccess();

    void testLoadFailBadRequest();

    void testLoadFailAdmNull();

    void testLoadFailNoFill();

    void testLoadFailNoFillConnection();

    void testLoadFailNoFillTimeoutError();

    void testShown();

    void testImpressionTracked();

    void testImpressionNotTrackedByShown();

    void testImpressionNotTrackedByTimeout();

    void testClicked();

    void testExpired();

    void testDestroy();
}
