package io.bidmachine;

class TrackEventInfo {

    final long startTimeMs;
    long finishTimeMs;

    TrackEventInfo() {
        startTimeMs = System.currentTimeMillis();
    }
}