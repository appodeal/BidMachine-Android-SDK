package io.bidmachine.banner;

public enum BannerSize {

    Size_320x50(320, 50),
    Size_300x250(300, 250),
    Size_728x90(728, 90);

    public final int width;
    public final int height;

    BannerSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

}