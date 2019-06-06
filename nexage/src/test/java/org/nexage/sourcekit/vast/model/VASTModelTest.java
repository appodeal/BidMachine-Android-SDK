package org.nexage.sourcekit.vast.model;

import android.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexage.sourcekit.util.XmlTools;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE)
public class VASTModelTest {

    @Test
    public void getCompanion_Portrait() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[portrait_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[portrait_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"480\" height=\"320\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">landscape_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">landscape_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[landscape_url]]>\n" +
                "                            </StaticResource>\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[landscape_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1080, 1920));
        assertNotNull(companion);
        assertEquals(companion.getHeight(), 480);
        assertEquals(companion.getWidth(), 320);
        assertEquals(companion.getStaticRecourceUrl(), "portrait_url");
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_close");
        assertEquals(companion.clickThrough, "portrait_click");
        assertFalse(companion.getHtml(1080, 1920, 2.0f).isEmpty());
    }

    @Test
    public void getCompanion_Landscape() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[portrait_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[portrait_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"480\" height=\"320\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">landscape_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">landscape_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[landscape_url]]>\n" +
                "                            </StaticResource>\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[landscape_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";

        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNotNull(companion);
        assertEquals(companion.getWidth(), 480);
        assertEquals(companion.getHeight(), 320);
        assertEquals(companion.getStaticRecourceUrl(), "landscape_url");
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "landscape_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "landscape_close");
        assertEquals(companion.clickThrough, "landscape_click");
        assertFalse(companion.getHtml(1920, 1080, 2.0f).isEmpty());
    }

    @Test
    public void getCompanionHTML_Portrait() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <HTMLResource>\n" +
                "                                <![CDATA[portrait_html]]>\n" +
                "                            </HTMLResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"480\" height=\"320\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">landscape_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">landscape_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <HTMLResource>\n" +
                "                                <![CDATA[landscape_html]]>\n" +
                "                            </HTMLResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1080, 1920));
        assertNotNull(companion);
        assertEquals(companion.getHeight(), 480);
        assertEquals(companion.getWidth(), 320);
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_close");
        assertTrue(companion.getHtml(1080, 1920, 2.0f).contains("portrait_html"));
    }

    @Test
    public void getCompanionHTML_Landscape() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <HTMLResource>\n" +
                "                                <![CDATA[portrait_html]]>\n" +
                "                            </HTMLResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"480\" height=\"320\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">landscape_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">landscape_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <HTMLResource>\n" +
                "                                <![CDATA[landscape_html]]>\n" +
                "                            </HTMLResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";

        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNotNull(companion);
        assertEquals(companion.getWidth(), 480);
        assertEquals(companion.getHeight(), 320);
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "landscape_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "landscape_close");
        assertTrue(companion.getHtml(1920, 1080, 2.0f).contains("landscape_html"));
    }
    @Test
    public void getCompanionIFrame_Portrait() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <IFrameResource>\n" +
                "                                <![CDATA[portrait_iframe]]>\n" +
                "                            </IFrameResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"480\" height=\"320\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">landscape_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">landscape_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <IFrameResource>\n" +
                "                                <![CDATA[landscape_iframe]]>\n" +
                "                            </IFrameResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1080, 1920));
        assertNotNull(companion);
        assertEquals(companion.getHeight(), 480);
        assertEquals(companion.getWidth(), 320);
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_close");
        assertTrue(companion.getHtml(1080, 1920, 2.0f).contains("portrait_iframe"));
    }

    @Test
    public void getCompanionIFrame_Landscape() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <IFrameResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[portrait_iframe]]>\n" +
                "                            </IFrameResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"480\" height=\"320\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">landscape_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">landscape_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <IFrameResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[landscape_iframe]]>\n" +
                "                            </IFrameResource>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";

        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNotNull(companion);
        assertEquals(companion.getWidth(), 480);
        assertEquals(companion.getHeight(), 320);
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "landscape_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "landscape_close");
        assertTrue(companion.getHtml(1920, 1080, 2.0f).contains("landscape_iframe"));
    }

    @Test
    public void getCompanion_OnlyPortrait() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[portrait_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[portrait_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNotNull(companion);
        assertEquals(companion.getHeight(), 480);
        assertEquals(companion.getWidth(), 320);
        assertEquals(companion.getStaticRecourceUrl(), "portrait_url");
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "portrait_close");
        assertEquals(companion.clickThrough, "portrait_click");
        assertFalse(companion.getHtml(1080, 1920, 2.0f).isEmpty());
    }

    @Test
    public void getCompanion_wo_Width() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[portrait_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[portrait_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNull(companion);
    }

    @Test
    public void getCompanion_wo_Height() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        \n" +
                "                        <Companion width=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">portrait_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">portrait_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[portrait_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[portrait_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNull(companion);
    }

    @Test
    public void getCompanion_Empty() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1920, 1080));
        assertNull(companion);
    }

    @Test
    public void getCompanion_ClosestAspectRatio() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        <Companion width=\"540\" height=\"960\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">056_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">056_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[056_url]]>\n" +
                "                            </StaticResource>\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[056_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">067_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">067_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[067_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[067_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1080, 1920));
        assertNotNull(companion);
        assertEquals(companion.getHeight(), 960);
        assertEquals(companion.getWidth(), 540);
        assertEquals(companion.getStaticRecourceUrl(), "056_url");
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings =  companion.getTrackings();
        assertNotNull(trackings);
        List<String> urls = trackings.get(TRACKING_EVENTS_TYPE.creativeView);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "056_creativeView");
        urls = trackings.get(TRACKING_EVENTS_TYPE.close);
        assertNotNull(urls);
        assertEquals(urls.size(), 1);
        assertEquals(urls.get(0), "056_close");
        assertEquals(companion.clickThrough, "056_click");
        assertFalse(companion.getHtml(1080, 1920, 2.0f).isEmpty());
    }

    @Test
    public void getCompanion_inValid() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        <Companion width=\"540\" height=\"960\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">056_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">056_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[056_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1080, 1920));
        assertNull(companion);
    }

    @Test
    public void getCompanion_GoodAndInvalid() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "            <Creatives>\n" +
                "                <Creative AdID=\"-Companion\">\n" +
                "                    <CompanionAds>\n" +
                "                        <Companion width=\"540\" height=\"960\">\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">056_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">056_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[056_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        <Companion width=\"320\" height=\"480\">\n" +
                "\n" +
                "\n" +
                "                           <TrackingEvents>\n" +
                "                           <Tracking event=\"creativeView\">067_creativeView</Tracking>\n" +
                "                           <Tracking event=\"close\">067_close</Tracking>\n" +
                "                           </TrackingEvents>\n" +
                "                            <StaticResource creativeType=\"image/jpeg\">\n" +
                "                                <![CDATA[067_url]]>\n" +
                "                            </StaticResource>\n" +
                "\n" +
                "\n" +
                "                            <CompanionClickThrough>\n" +
                "                                \n" +
                "                                <![CDATA[067_click]]>\n" +
                "                                \n" +
                "                            </CompanionClickThrough>\n" +
                "                        </Companion>\n" +
                "                        \n" +
                "                        \n" +
                "                    </CompanionAds>\n" +
                "                </Creative>\n" +
                "            </Creatives>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        VASTCompanion companion = vastModel.getCompanion(new Pair<Integer, Integer>(1080, 1920));
        assertNotNull(companion);
        assertEquals(companion.getHeight(), 480);
        assertEquals(companion.getWidth(), 320);
    }

    @Test
    public void getExtension() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "           <Extensions>\n" +
                "               <Extension type=\"appodeal\">\n" +
                "               </Extension>\n" +
                "           </Extensions>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        assertNotNull(vastModel.getExtensions());
    }

    @Test
    public void getExtension_unknown() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "           <Extensions>\n" +
                "               <Extension type=\"unknown\">\n" +
                "               </Extension>\n" +
                "           </Extensions>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        assertNull(vastModel.getExtensions());
    }

    @Test
    public void getExtension_withoutType() throws Exception {
        String xmlString = "<VASTS><VAST version=\"2.0\">\n" +
                "    <Ad id=\"50\">\n" +
                "        <InLine>\n" +
                "           <Extensions>\n" +
                "               <Extension>\n" +
                "               </Extension>\n" +
                "           </Extensions>\n" +
                "        </InLine>\n" +
                "    </Ad>\n" +
                "</VAST></VASTS>";
        VASTModel vastModel = new VASTModel(XmlTools.stringToDocument(xmlString));
        assertNotNull(vastModel);
        assertNull(vastModel.getExtensions());
    }
}