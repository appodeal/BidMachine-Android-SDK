package io.bidmachine;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;

public class RequestTestActivity extends Activity {

    public FrameLayout parentFrame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFrame = new FrameLayout(this);
        setContentView(parentFrame);
    }

}
