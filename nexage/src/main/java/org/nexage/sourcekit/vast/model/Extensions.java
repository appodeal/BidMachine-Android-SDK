package org.nexage.sourcekit.vast.model;

import android.graphics.Color;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;
import android.widget.RelativeLayout;

import org.nexage.sourcekit.util.Assets;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.XmlTools;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Extensions {
    private final String TAG = "Extensions";

    private final String EXTENSION_NAME_CTA_TEXT = "CtaText";
    private final String EXTENSION_NAME_SHOW_CTA_WIDGET = "ShowCta";
    private final String EXTENSION_NAME_SHOW_MUTE_WIDGET = "ShowMute";
    private final String EXTENSION_NAME_SHOW_COMPANION_AFTER_VIDEO = "ShowCompanion";
    private final String EXTENSION_NAME_COMPANION_CLOSE_TIME = "CompanionCloseTime";
    private final String EXTENSION_NAME_VIDEO_CLICKABLE = "VideoClickable";
    private final String EXTENSION_NAME_CTA_XPOSITION = "CtaXPosition";
    private final String EXTENSION_NAME_CTA_YPOSITION = "CtaYPosition";
    private final String EXTENSION_NAME_CLOSE_XPOSITION = "CloseXPosition";
    private final String EXTENSION_NAME_CLOSE_YPOSITION = "CloseYPosition";
    private final String EXTENSION_NAME_MUTE_XPOSITION = "MuteXPosition";
    private final String EXTENSION_NAME_MUTE_YPOSITION = "MuteYPosition";
    private final String EXTENSION_NAME_ASSETS_COLOR = "AssetsColor";
    private final String EXTENSION_NAME_ASSETS_BACKGROUND_COLOR = "AssetsBackgroundColor";
    private final String EXTENSION_NAME_COMPANION = "Companion";
    private final String EXTENSION_NAME_SHOW_PROGRESSBAR = "ShowProgress";

    private String ctaText;
    private boolean showCta = true;
    private boolean showMute = true;
    private boolean showCompanion = true;
    private boolean showProgress = true;
    private boolean videoClickable;
    private int companionCloseTime;
    private int CtaXPosition = RelativeLayout.ALIGN_PARENT_RIGHT;
    private int CtaYPosition = RelativeLayout.ALIGN_PARENT_BOTTOM;
    private int CloseXPosition = RelativeLayout.ALIGN_PARENT_RIGHT;
    private int CloseYPosition = RelativeLayout.ALIGN_PARENT_TOP;
    private int MuteXPosition = RelativeLayout.ALIGN_PARENT_LEFT;
    private int MuteYPosition = RelativeLayout.ALIGN_PARENT_TOP;
    private int assetsColor = Assets.mainAssetsColor;
    private int assetsBackgroundColor = Color.TRANSPARENT;
    private VASTCompanion vastCompanion;

    Extensions(Node node) {
        VASTLog.d(TAG, String.format("Found extensions: %s", node.toString()));
        NodeList extensions = node.getChildNodes();
        if (extensions != null) {
            for (int i = 0; i < extensions.getLength(); ++i) {
                Node extensionNode = extensions.item(i);
                String name = extensionNode.getNodeName();
                if (name.equals(EXTENSION_NAME_COMPANION)) {
                    VASTCompanion tempVastCompanion = new VASTCompanion(extensionNode);
                    if (tempVastCompanion.isValid(320, 50)) {
                        vastCompanion = tempVastCompanion;
                    }
                } else {
                    String value = XmlTools.getElementValue(extensionNode);
                    switch (name) {
                        case EXTENSION_NAME_CTA_TEXT:
                            ctaText = value;
                            break;
                        case EXTENSION_NAME_SHOW_CTA_WIDGET:
                            showCta = parseBoolean(value);
                            break;
                        case EXTENSION_NAME_SHOW_MUTE_WIDGET:
                            showMute = parseBoolean(value);
                            break;
                        case EXTENSION_NAME_SHOW_COMPANION_AFTER_VIDEO:
                            showCompanion = parseBoolean(value);
                            break;
                        case EXTENSION_NAME_COMPANION_CLOSE_TIME:
                            companionCloseTime = parseTimeToSeconds(value);
                            break;
                        case EXTENSION_NAME_VIDEO_CLICKABLE:
                            videoClickable = parseBoolean(value);
                            break;
                        case EXTENSION_NAME_SHOW_PROGRESSBAR:
                            showProgress = parseBoolean(value);
                            break;
                        case EXTENSION_NAME_ASSETS_COLOR:
                            try {
                                assetsColor = Color.parseColor(value);
                            } catch (Exception e) {
                                VASTLog.e(TAG, e.getLocalizedMessage());
                            }
                            break;
                        case EXTENSION_NAME_ASSETS_BACKGROUND_COLOR:
                            try {
                                assetsBackgroundColor = Color.parseColor(value);
                            } catch (Exception e) {
                                VASTLog.e(TAG, e.getLocalizedMessage());
                            }
                            break;
                        case EXTENSION_NAME_CTA_XPOSITION:
                            switch (value.toLowerCase()) {
                                case "left":
                                    CtaXPosition = RelativeLayout.ALIGN_PARENT_LEFT;
                                    break;
                                case "right":
                                    CtaXPosition = RelativeLayout.ALIGN_PARENT_RIGHT;
                                    break;
                                case "center":
                                    CtaXPosition = RelativeLayout.CENTER_HORIZONTAL;
                                    break;
                            }
                            break;
                        case EXTENSION_NAME_CTA_YPOSITION:
                            switch (value.toLowerCase()) {
                                case "top":
                                    CtaYPosition = RelativeLayout.ALIGN_PARENT_TOP;
                                    break;
                                case "bottom":
                                    CtaYPosition = RelativeLayout.ALIGN_PARENT_BOTTOM;
                                    break;
                                case "center":
                                    CtaYPosition = RelativeLayout.CENTER_VERTICAL;
                                    break;
                            }
                            break;
                        case EXTENSION_NAME_CLOSE_XPOSITION:
                            switch (value.toLowerCase()) {
                                case "left":
                                    CloseXPosition = RelativeLayout.ALIGN_PARENT_LEFT;
                                    break;
                                case "right":
                                    CloseXPosition = RelativeLayout.ALIGN_PARENT_RIGHT;
                                    break;
                                case "center":
                                    CloseXPosition = RelativeLayout.CENTER_HORIZONTAL;
                                    break;
                            }
                            break;
                        case EXTENSION_NAME_CLOSE_YPOSITION:
                            switch (value.toLowerCase()) {
                                case "top":
                                    CloseYPosition = RelativeLayout.ALIGN_PARENT_TOP;
                                    break;
                                case "bottom":
                                    CloseYPosition = RelativeLayout.ALIGN_PARENT_BOTTOM;
                                    break;
                                case "center":
                                    CloseYPosition = RelativeLayout.CENTER_VERTICAL;
                                    break;
                            }
                            break;
                        case EXTENSION_NAME_MUTE_XPOSITION:
                            switch (value.toLowerCase()) {
                                case "left":
                                    MuteXPosition = RelativeLayout.ALIGN_PARENT_LEFT;
                                    break;
                                case "right":
                                    MuteXPosition = RelativeLayout.ALIGN_PARENT_RIGHT;
                                    break;
                                case "center":
                                    MuteXPosition = RelativeLayout.CENTER_HORIZONTAL;
                                    break;
                            }
                            break;
                        case EXTENSION_NAME_MUTE_YPOSITION:
                            switch (value.toLowerCase()) {
                                case "top":
                                    MuteYPosition = RelativeLayout.ALIGN_PARENT_TOP;
                                    break;
                                case "bottom":
                                    MuteYPosition = RelativeLayout.ALIGN_PARENT_BOTTOM;
                                    break;
                                case "center":
                                    MuteYPosition = RelativeLayout.CENTER_VERTICAL;
                                    break;
                            }
                            break;
                        default:
                            VASTLog.d(TAG, String.format("Extension %s is not supported", name));
                    }
                }
            }
        }
    }

    @VisibleForTesting boolean parseBoolean(String value) {
        return value.equals("true") || value.equals("1");
    }

    @VisibleForTesting int parseTimeToSeconds(String time) {
        try {
            String[] units = time.split(":");
            int minutes = Integer.parseInt(units[0]);
            int seconds = Integer.parseInt(units[1]);
            return 60 * minutes + seconds;
        } catch (Exception e) {
            VASTLog.w(TAG, e.getMessage());
        }
        return 0;
    }

    public String getCtaText() {
        return ctaText;
    }

    public boolean canShowCta() {
        return showCta;
    }

    public boolean canShowMute() {
        return showMute;
    }

    public boolean canShowCompanion() {
        return showCompanion;
    }

    public boolean canShowProgress() {
        return showProgress;
    }

    public int getCompanionCloseTime() {
        return companionCloseTime;
    }

    public boolean isVideoClickable() {
        return videoClickable;
    }

    public Pair<Integer, Integer> getCtaPosition() {
        return new Pair<>(CtaXPosition, CtaYPosition);
    }

    public Pair<Integer, Integer> getClosePosition() {
        return new Pair<>(CloseXPosition, CloseYPosition);
    }

    public Pair<Integer, Integer> getMutePosition() {
        return new Pair<>(MuteXPosition, MuteYPosition);
    }

    public int getAssetsColor() {
        return assetsColor;
    }

    public int getAssetsBackgroundColor() {
        return assetsBackgroundColor;
    }

    public VASTCompanion getVastCompanion() {
        return vastCompanion;
    }
}
