package org.nexage.sourcekit.vast.model;

import android.graphics.Color;
import android.util.Pair;
import android.widget.RelativeLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexage.sourcekit.util.Assets;
import org.nexage.sourcekit.util.XmlTools;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE)
public class ExtensionsTest {

    private Node getExtensionsFromString(String xmlString) throws Exception {
        Document document = XmlTools.stringToDocument(xmlString);
        XPath xpath = XPathFactory.newInstance().newXPath();
        return  ((NodeList) xpath.evaluate("/TestVAST/Extensions/Extension", document, XPathConstants.NODESET)).item(0);
    }

    @Test
    public void ParsingExtensionsTest_DefaultValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";


        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertNull(extensions.getCtaText());
        assertTrue(extensions.canShowCta());
        assertTrue(extensions.canShowMute());
        assertTrue(extensions.canShowCompanion());
        assertEquals(extensions.getCompanionCloseTime(), 0);
        assertFalse(extensions.isVideoClickable());
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getAssetsColor(), Assets.mainAssetsColor);
        assertEquals(extensions.getAssetsBackgroundColor(), Color.TRANSPARENT);
        assertNull(extensions.getVastCompanion());
        assertTrue(extensions.canShowProgress());
    }

    @Test
    public void ParsingExtensionsTest_UnknownValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<ShowCtaButton>false</ShowCtaButton>\n" +
                "<CompanionCloseTime_Unknown>00:10</CompanionCloseTime_Unknown>>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";


        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertNull(extensions.getCtaText());
        assertTrue(extensions.canShowCta());
        assertTrue(extensions.canShowMute());
        assertTrue(extensions.canShowCompanion());
        assertEquals(extensions.getCompanionCloseTime(), 0);
        assertFalse(extensions.isVideoClickable());
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getAssetsColor(), Assets.mainAssetsColor);
        assertEquals(extensions.getAssetsBackgroundColor(), Color.TRANSPARENT);
        assertNull(extensions.getVastCompanion());
        assertTrue(extensions.canShowProgress());
    }

    @Test
    public void ParsingExtensionsTest() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaText>Download</CtaText>\n" +
                "<ShowCta>false</ShowCta>\n" +
                "<ShowMute>false</ShowMute>\n" +
                "<ShowCompanion>false</ShowCompanion>\n" +
                "<CompanionCloseTime>00:10</CompanionCloseTime>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";


        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaText(), "Download");
        assertFalse(extensions.canShowCta());
        assertFalse(extensions.canShowMute());
        assertFalse(extensions.canShowCompanion());
        assertEquals(extensions.getCompanionCloseTime(), 10);
    }

    @Test
    public void ParsingExtensionsTest_boolInNumbers() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaText>Download</CtaText>\n" +
                "<ShowCta>1</ShowCta>\n" +
                "<ShowMute>0</ShowMute>\n" +
                "<ShowCompanion>1</ShowCompanion>\n" +
                "<CompanionCloseTime>00:10</CompanionCloseTime>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";


        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaText(), "Download");
        assertTrue(extensions.canShowCta());
        assertFalse(extensions.canShowMute());
        assertTrue(extensions.canShowCompanion());
        assertEquals(extensions.getCompanionCloseTime(), 10);
    }

    @Test
    public void ParsingExtensionsTest_videoClickable() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<VideoClickable>true</VideoClickable>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertTrue(extensions.isVideoClickable());
    }

    @Test
    public void ParsingExtensionsTest_videoClickable_false() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<VideoClickable>false</VideoClickable>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertFalse(extensions.isVideoClickable());
    }

    @Test
    public void ParsingExtensionsTest_invalidPosition() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>false</CtaXPosition>\n" +
                "<CtaYPosition>123</CtaYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
    }

    @Test
    public void ParsingExtensionsTest_left_top() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>left</CtaXPosition>\n" +
                "<CtaYPosition>top</CtaYPosition>\n" +
                "<MuteXPosition>left</MuteXPosition>\n" +
                "<MuteYPosition>top</MuteYPosition>\n" +
                "<CloseXPosition>left</CloseXPosition>\n" +
                "<CloseYPosition>top</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
    }

    @Test
    public void ParsingExtensionsTest_left_center() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>left</CtaXPosition>\n" +
                "<CtaYPosition>center</CtaYPosition>\n" +
                "<MuteXPosition>left</MuteXPosition>\n" +
                "<MuteYPosition>center</MuteYPosition>\n" +
                "<CloseXPosition>left</CloseXPosition>\n" +
                "<CloseYPosition>center</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.CENTER_VERTICAL));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.CENTER_VERTICAL));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.CENTER_VERTICAL));
    }

    @Test
    public void ParsingExtensionsTest_left_bottom() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>left</CtaXPosition>\n" +
                "<CtaYPosition>bottom</CtaYPosition>\n" +
                "<MuteXPosition>left</MuteXPosition>\n" +
                "<MuteYPosition>bottom</MuteYPosition>\n" +
                "<CloseXPosition>left</CloseXPosition>\n" +
                "<CloseYPosition>bottom</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_BOTTOM));
    }

    @Test
    public void ParsingExtensionsTest_right_top() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>right</CtaXPosition>\n" +
                "<CtaYPosition>top</CtaYPosition>\n" +
                "<MuteXPosition>right</MuteXPosition>\n" +
                "<MuteYPosition>top</MuteYPosition>\n" +
                "<CloseXPosition>right</CloseXPosition>\n" +
                "<CloseYPosition>top</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
    }

    @Test
    public void ParsingExtensionsTest_right_center() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>right</CtaXPosition>\n" +
                "<CtaYPosition>center</CtaYPosition>\n" +
                "<MuteXPosition>right</MuteXPosition>\n" +
                "<MuteYPosition>center</MuteYPosition>\n" +
                "<CloseXPosition>right</CloseXPosition>\n" +
                "<CloseYPosition>center</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.CENTER_VERTICAL));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.CENTER_VERTICAL));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.CENTER_VERTICAL));
    }

    @Test
    public void ParsingExtensionsTest_right_bottom() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>right</CtaXPosition>\n" +
                "<CtaYPosition>bottom</CtaYPosition>\n" +
                "<MuteXPosition>right</MuteXPosition>\n" +
                "<MuteYPosition>bottom</MuteYPosition>\n" +
                "<CloseXPosition>right</CloseXPosition>\n" +
                "<CloseYPosition>bottom</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
    }

    @Test
    public void ParsingExtensionsTest_center_top() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>center</CtaXPosition>\n" +
                "<CtaYPosition>top</CtaYPosition>\n" +
                "<MuteXPosition>center</MuteXPosition>\n" +
                "<MuteYPosition>top</MuteYPosition>\n" +
                "<CloseXPosition>center</CloseXPosition>\n" +
                "<CloseYPosition>top</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_TOP));
    }

    @Test
    public void ParsingExtensionsTest_center_center() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>center</CtaXPosition>\n" +
                "<CtaYPosition>center</CtaYPosition>\n" +
                "<MuteXPosition>center</MuteXPosition>\n" +
                "<MuteYPosition>center</MuteYPosition>\n" +
                "<CloseXPosition>center</CloseXPosition>\n" +
                "<CloseYPosition>center</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.CENTER_VERTICAL));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.CENTER_VERTICAL));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.CENTER_VERTICAL));
    }

    @Test
    public void ParsingExtensionsTest_center_bottom() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>center</CtaXPosition>\n" +
                "<CtaYPosition>bottom</CtaYPosition>\n" +
                "<MuteXPosition>center</MuteXPosition>\n" +
                "<MuteYPosition>bottom</MuteYPosition>\n" +
                "<CloseXPosition>center</CloseXPosition>\n" +
                "<CloseYPosition>bottom</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_BOTTOM));
    }

    @Test
    public void ParsingExtensionsTest_center_bottom_UpperCase() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>Center</CtaXPosition>\n" +
                "<CtaYPosition>BOTTOM</CtaYPosition>\n" +
                "<MuteXPosition>center</MuteXPosition>\n" +
                "<MuteYPosition>BOTTOM</MuteYPosition>\n" +
                "<CloseXPosition>center</CloseXPosition>\n" +
                "<CloseYPosition>BOTTOM</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_BOTTOM));
    }

    @Test
    public void ParsingExtensionsTest_colors() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<AssetsColor>#C8FFFFFF</AssetsColor>\n" +
                "<AssetsBackgroundColor>#5c000000</AssetsBackgroundColor>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getAssetsColor(), Color.parseColor("#C8FFFFFF"));
        assertEquals(extensions.getAssetsBackgroundColor(), Color.parseColor("#5c000000"));

        xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<AssetsColor>#FFFFFF</AssetsColor>\n" +
                "<AssetsBackgroundColor>#000000</AssetsBackgroundColor>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getAssetsColor(), Color.parseColor("#FFFFFF"));
        assertEquals(extensions.getAssetsBackgroundColor(), Color.parseColor("#000000"));
    }

    @Test
    public void ParsingExtensionsTest_colors_badValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<AssetsColor>true</AssetsColor>\n" +
                "<AssetsBackgroundColor>false</AssetsBackgroundColor>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getAssetsColor(), Assets.mainAssetsColor);
        assertEquals(extensions.getAssetsBackgroundColor(), Color.TRANSPARENT);

        xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<AssetsColor>123</AssetsColor>\n" +
                "<AssetsBackgroundColor>123</AssetsBackgroundColor>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getAssetsColor(), Assets.mainAssetsColor);
        assertEquals(extensions.getAssetsBackgroundColor(), Color.TRANSPARENT);

        xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<AssetsColor>null</AssetsColor>\n" +
                "<AssetsBackgroundColor>null</AssetsBackgroundColor>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertEquals(extensions.getAssetsColor(), Assets.mainAssetsColor);
        assertEquals(extensions.getAssetsBackgroundColor(), Color.TRANSPARENT);
    }
    
    @Test
    public void ParsingExtensionsTest_companion() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<Companion width=\"50\" height=\"50\">\n" +
                "   <TrackingEvents>\n" +
                "  <Tracking event=\"start\">\n" +
                "  <![CDATA[http://my.adserver.com/trackPoint=trackstart;]]>\n" +
                "  </Tracking>\n" +
                "   </TrackingEvents>\n" +
                "   <StaticResource creativeType=\"image/png\"><![CDATA[http://1.bp.blogspot.com/_p0ZTd1mmzMU/TIQXL67HhZI/AAAAAAAAAHY/6Q9jQ12Ifhk/s320/your_ad_here.png]]></StaticResource>\n" +
                "   <CompanionClickThrough>\n" +
                "<![CDATA[http://my.adserver.com/?clickThrough=banner468x60_VAST_expandingBanner]]>\n" +
                "   </CompanionClickThrough>\n" +
                "</Companion>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertNotNull(extensions.getVastCompanion());
    }

    @Test
    public void ParsingExtensionsTest_showProgress() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<ShowProgress>false</ShowProgress>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertFalse(extensions.canShowProgress());
    }

    @Test
    public void parseBoolean_InvalidValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);

        assertTrue(extensions.parseBoolean("true"));
        assertTrue(extensions.parseBoolean("1"));
        assertFalse(extensions.parseBoolean("false"));
        assertFalse(extensions.parseBoolean("0"));
        assertFalse(extensions.parseBoolean("aaaa"));
        assertFalse(extensions.parseBoolean("21321312"));
        assertFalse(extensions.parseBoolean("null"));
    }

    @Test
    public void parseTimeToSeconds_InvalidValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);

        assertEquals(extensions.parseTimeToSeconds("00:10"), 10);
        assertEquals(extensions.parseTimeToSeconds("01:10"), 70);
        assertEquals(extensions.parseTimeToSeconds("02:01:10"), 121);
        assertEquals(extensions.parseTimeToSeconds("01:00.001"), 0);
        assertEquals(extensions.parseTimeToSeconds("0"), 0);
        assertEquals(extensions.parseTimeToSeconds("a"), 0);
        assertEquals(extensions.parseTimeToSeconds("true"), 0);
        assertEquals(extensions.parseTimeToSeconds("null"), 0);
    }

    @Test
    public void positions_InvalidValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>null</CtaXPosition>\n" +
                "<CtaYPosition>null</CtaYPosition>\n" +
                "<MuteXPosition>null</MuteXPosition>\n" +
                "<MuteYPosition>null</MuteYPosition>\n" +
                "<CloseXPosition>null</CloseXPosition>\n" +
                "<CloseYPosition>null</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);

        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));

        xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>text</CtaXPosition>\n" +
                "<CtaYPosition>text</CtaYPosition>\n" +
                "<MuteXPosition>text</MuteXPosition>\n" +
                "<MuteYPosition>text</MuteYPosition>\n" +
                "<CloseXPosition>text</CloseXPosition>\n" +
                "<CloseYPosition>text</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);

        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));

        xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>1</CtaXPosition>\n" +
                "<CtaYPosition>2</CtaYPosition>\n" +
                "<MuteXPosition>3</MuteXPosition>\n" +
                "<MuteYPosition>4</MuteYPosition>\n" +
                "<CloseXPosition>5</CloseXPosition>\n" +
                "<CloseYPosition>6</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);

        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));

        xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<CtaXPosition>true</CtaXPosition>\n" +
                "<CtaYPosition>true</CtaYPosition>\n" +
                "<MuteXPosition>true</MuteXPosition>\n" +
                "<MuteYPosition>true</MuteYPosition>\n" +
                "<CloseXPosition>true</CloseXPosition>\n" +
                "<CloseYPosition>true</CloseYPosition>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);

        assertEquals(extensions.getCtaPosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM));
        assertEquals(extensions.getMutePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP));
        assertEquals(extensions.getClosePosition(), new Pair<>(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP));
    }

    @Test
    public void companion_InvalidValues() throws Exception {
        String xmlString = "<TestVAST><Extensions>\n" +
                "<Extension type=\"appodeal\">\n" +
                "<Companion width=\"540\" height=\"960\">\n" +
                "   <TrackingEvents>\n" +
                "       <Tracking event=\"creativeView\">056_creativeView</Tracking>\n" +
                "       <Tracking event=\"close\">056_close</Tracking>\n" +
                "   </TrackingEvents>\n" +
                "   <CompanionClickThrough>\n" +
                "       <![CDATA[056_click]]>\n" +
                "   </CompanionClickThrough>\n" +
                "</Companion>\n" +
                "</Extension>\n" +
                "</Extensions></TestVAST>";

        Extensions extensions = new Extensions(getExtensionsFromString(xmlString));
        assertNotNull(extensions);
        assertNull(extensions.getVastCompanion());
    }
}