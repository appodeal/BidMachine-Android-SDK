package org.nexage.sourcekit.vast.model;

import android.support.annotation.VisibleForTesting;
import android.util.Pair;

import org.nexage.sourcekit.mraid.internal.MRAIDHtmlProcessor;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.XmlTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class VASTCompanion {
    private final String TAG = "VASTCompanion";
    private final String SUPPORTED_STATIC_TYPE_REGEX = "image/.*(?i)(gif|jpeg|jpg|bmp|png)";
    private VASTMediaFile staticResource;
    private VASTMediaFile iFrameResource;
    private VASTMediaFile htmlResource;
    private HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings = new HashMap<TRACKING_EVENTS_TYPE, List<String>>();
    @VisibleForTesting String clickThrough;
    public int width;
    public int height;

    VASTCompanion(Node node) {
        NamedNodeMap companionAttributes = node.getAttributes();
        height = Integer.valueOf(companionAttributes.getNamedItem("height").getNodeValue());
        width = Integer.valueOf(companionAttributes.getNamedItem("width").getNodeValue());
        VASTLog.d(TAG, "VASTCompanion");
        NodeList companions = node.getChildNodes();
        Node child;
        if (companions != null) {
            for (int j = 0; j < companions.getLength(); j++) {
                child = companions.item(j);
                String nodeName = child.getNodeName();
                String value;

                if (nodeName.equalsIgnoreCase("StaticResource") && this.staticResource == null) {
                    VASTMediaFile mf = new VASTMediaFile();
                    mf.setType(child.getAttributes().getNamedItem("creativeType").getNodeValue());
                    mf.setValue(XmlTools.getElementValue(child));
                    if (isMediaFileCompatible(mf)) {
                        this.staticResource = mf;
                    }
                } else if (nodeName.equalsIgnoreCase("IFrameResource") && this.iFrameResource == null) {
                    VASTMediaFile mf = new VASTMediaFile();
                    mf.setValue(XmlTools.getElementValue(child));
                    this.iFrameResource = mf;
                } else if (nodeName.equalsIgnoreCase("HTMLResource") && this.htmlResource == null) {
                    VASTMediaFile mf = new VASTMediaFile();
                    mf.setValue(XmlTools.getElementValue(child));
                    this.htmlResource = mf;
                } else if (nodeName.equalsIgnoreCase("CompanionClickThrough")) {
                    value = XmlTools.getElementValue(child);
                    this.clickThrough = value;
                } else if (nodeName.equalsIgnoreCase("NonLinearClickThrough")){
                    value = XmlTools.getElementValue(child);
                    this.clickThrough = value;
                } else if (nodeName.equalsIgnoreCase("TrackingEvents")) {
                    List<String> tracking;
                    trackings = new HashMap<>();

                    NodeList nodes = child.getChildNodes();
                    Node trackingNode;
                    String trackingURL;
                    String eventName;
                    TRACKING_EVENTS_TYPE key;

                    if (nodes != null) {
                        for (int i = 0; i < nodes.getLength(); i++) {
                            trackingNode = nodes.item(i);
                            if(trackingNode.getNodeName().equalsIgnoreCase("Tracking")) {
                                NamedNodeMap attributes = trackingNode.getAttributes();

                                eventName = (attributes.getNamedItem("event"))
                                        .getNodeValue();
                                try {
                                    key = TRACKING_EVENTS_TYPE.valueOf(eventName);
                                } catch (IllegalArgumentException e) {
                                    VASTLog.w(TAG, "Event:" + eventName
                                            + " is not valid. Skipping it.");
                                    continue;
                                }

                                trackingURL = XmlTools.getElementValue(trackingNode);

                                if (trackings.containsKey(key)) {
                                    tracking = trackings.get(key);
                                    tracking.add(trackingURL);
                                } else {
                                    tracking = new ArrayList<>();
                                    tracking.add(trackingURL);
                                    trackings.put(key, tracking);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isMediaFileCompatible(VASTMediaFile media) {
        return media.getType().matches(SUPPORTED_STATIC_TYPE_REGEX);
    }

    String getStaticRecourceUrl() {
        if (staticResource != null) {
            return staticResource.getValue();
        }
        return null;
    }

    public Pair<String, Pair<Integer, Integer>> getHtmlForMraid(int screenWidth, int screenHeight, float density) {
        if (htmlResource != null) {
            return new Pair<>(htmlResource.getValue(), new Pair<>(getWidth(), getHeight()));
        } else if (staticResource != null) {
            float screenAspectRatio = ((float) screenWidth) / screenHeight;
            float imageAspectRatio = ((float) width) / height;

            int newHeight, newWidth;
            if(!Float.isNaN(imageAspectRatio)) {
                if (imageAspectRatio <= screenAspectRatio) {
                    newHeight = screenHeight;
                    newWidth = Math.round(newHeight * imageAspectRatio);
                } else {
                    newWidth = screenWidth;
                    newHeight = Math.round(newWidth / imageAspectRatio);
                }
            } else {
                newHeight = screenHeight;
                newWidth = screenWidth;
            }
            if (density != 0) {
                newWidth = Math.round(newWidth / density);
                newHeight = Math.round(newHeight / density);
            }
            Pair<Integer, Integer> size = new Pair<>(newWidth, newHeight);
            return new Pair<>(String.format("<a href='%s'><img width='%s' height='%s' src='%s'/></a>", clickThrough, newWidth, newHeight, getStaticRecourceUrl()), size);
        } else if (iFrameResource != null) {
            Pair<Integer, Integer> size = new Pair<>(0, 0);
            return new Pair<>(String.format("<html style=\"overflow: hidden\"><body style=\"overflow: hidden\"><iframe style=\"overflow: hidden\" scrolling=\"no\" frameborder=\"no\" width=\"%d\" height=\"%d\" src=\"%s\"></iframe></body></html>", screenWidth, screenHeight, iFrameResource.getValue()), size);
        }
        return null;
    }

    public String getHtml(int webViewWidth, int webViewHeight, float density) {
        if (htmlResource != null) {
            String html = MRAIDHtmlProcessor.processRawHtml(htmlResource.getValue());
            float screenAspectRatio = ((float) webViewWidth) / webViewHeight;

            float imageAspectRatio = ((float) width) / height;

            int newHeight, newWidth;
            if(!Float.isNaN(imageAspectRatio)) {
                if (imageAspectRatio <= screenAspectRatio) {
                    newHeight = webViewHeight;
                    newWidth = Math.round(newHeight * imageAspectRatio);
                } else {
                    newWidth = webViewWidth;
                    newHeight = Math.round(newWidth / imageAspectRatio);
                }
            } else {
                newHeight = webViewHeight;
                newWidth = webViewWidth;
            }

            int newWebViewWidth = Math.round(newWidth / density);
            int newWebViewHeight = Math.round(newHeight / density);

            String style = String.format("body, p {margin:0; padding:0} img {max-width:%dpx; max-height:%dpx} #appnext-interstitial {min-width:%dpx; min-height:%dpx;}"
                            + "img[width='%d'][height='%d'] {width: %dpx; height: %dpx} "
                            + ".appodeal-outer {display: table; position: absolute; height: 100%%; width: 100%%;}"
                            + ".appodeal-middle {display: table-cell; vertical-align: middle;}"
                            + ".appodeal-inner {margin-left: auto; margin-right: auto; width: %dpx; height: %dpx;}"
                            + ".ad_slug_table {margin-left: auto !important; margin-right: auto !important;} #ad[align='center'] {height: %dpx;} "
                            + "#voxelPlayer {position: relative !important;} "
                            + "#lsm_mobile_ad #wrapper, #lsm_overlay {position: relative !important;}",
                    newWebViewWidth, newWebViewHeight, newWebViewWidth, newWebViewHeight, width, height, newWebViewWidth, newWebViewHeight, newWebViewWidth, newWebViewHeight, newWebViewHeight);
            return String.format("<style type='text/css'>%s</style><div class='appodeal-outer'><div class='appodeal-middle'><div class='appodeal-inner'>%s</div></div></div>", style, html);
        } else if (staticResource != null) {
            String html = MRAIDHtmlProcessor.processRawHtml(String.format("<a href='%s'><img width='%s' height='%s' src='%s'/></a>", clickThrough, width, height, getStaticRecourceUrl()));
            float screenAspectRatio = ((float) webViewWidth) / webViewHeight;

            float imageAspectRatio = ((float) width) / height;

            int newHeight, newWidth;
            if(!Float.isNaN(imageAspectRatio)) {
                if (imageAspectRatio <= screenAspectRatio) {
                    newHeight = webViewHeight;
                    newWidth = Math.round(newHeight * imageAspectRatio);
                } else {
                    newWidth = webViewWidth;
                    newHeight = Math.round(newWidth / imageAspectRatio);
                }
            } else {
                newHeight = webViewHeight;
                newWidth = webViewWidth;
            }
            int newWebViewWidth = Math.round(newWidth / density);
            int newWebViewHeight = Math.round(newHeight / density);

            String style = String.format("body, p {margin:0; padding:0} img {max-width:%dpx; max-height:%dpx} #appnext-interstitial {min-width:%dpx; min-height:%dpx;}"
                            + "img[width='%d'][height='%d'] {width: %dpx; height: %dpx} "
                            + ".appodeal-outer {display: table; position: absolute; height: 100%%; width: 100%%;}"
                            + ".appodeal-middle {display: table-cell; vertical-align: middle;}"
                            + ".appodeal-inner {margin-left: auto; margin-right: auto; width: %dpx; height: %dpx;}"
                            + ".ad_slug_table {margin-left: auto !important; margin-right: auto !important;} #ad[align='center'] {height: %dpx;} "
                            + "#voxelPlayer {position: relative !important;} "
                            + "#lsm_mobile_ad #wrapper, #lsm_overlay {position: relative !important;}",
                    newWebViewWidth, newWebViewHeight, newWebViewWidth, newWebViewHeight, width, height, newWebViewWidth, newWebViewHeight, newWebViewWidth, newWebViewHeight, newWebViewHeight);
            return String.format("<style type='text/css'>%s</style><div class='appodeal-outer'><div class='appodeal-middle'><div class='appodeal-inner'>%s</div></div></div>", style, html);
        } else if (iFrameResource != null) {
            return String.format("<html style=\"overflow: hidden\"><body style=\"overflow: hidden\"><iframe style=\"overflow: hidden\" scrolling=\"no\" frameborder=\"no\" width=\"%s\" height=\"%s\" src=\"%s\"></iframe></body></html>", webViewWidth, webViewHeight, iFrameResource.getValue());
        }
        return null;
    }

    public HashMap<TRACKING_EVENTS_TYPE, List<String>> getTrackings() {
        return trackings;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    boolean isValid(int width, int height) {
        if (getHtmlForMraid(width, height, 1) == null || getHtmlForMraid(width, height, 1).first == null || getHtmlForMraid(width, height, 1).second == null || getHtml(width, height, 1) == null) {
            return false;
        }
        return true;
    }
}
