//
//  VASTModel.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package org.nexage.sourcekit.vast.model;

import android.app.Activity;
import android.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.nexage.sourcekit.util.HttpTools;
import org.nexage.sourcekit.util.Utils;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.XmlTools;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VASTModel implements Serializable {

	private static String TAG = "VASTModel";

	private static final long serialVersionUID = 4318368258447283733L;

	private transient Document vastsDocument;
	private VASTMediaFile pickedMediaFile;

	public static final int ERROR_CODE_XML_PARSING = 100; //Ошибка синтаксического анализа XML.
	public static final int ERROR_CODE_XML_VALIDATE = 101; //Ошибка при проверке схемы VAST.
	public static final int ERROR_CODE_BAD_MODEL = 200; //Ошибка трафика. Тип полученного проигрывателем объявления не соответствует ожидаемому и/или проигрыватель не может показать его.
	public static final int ERROR_CODE_DURATION = 202; //Ошибка связанная с различием заявленной и реальной длительностью файла.
	public static final int ERROR_CODE_BAD_SIZE = 203; //Проигрывателю требуется объявление другого размера.
	public static final int ERROR_CODE_BAD_URI = 301; //Тайм-аут URI VAST, указанного в текущем или одном из последующих элементов Wrapper. Так обозначаются ошибки запросов, например недействительный или недоступный URI, тайм-аут запроса URI, а также ошибки, связанные с безопасностью или запросами URI VAST.
	public static final int ERROR_CODE_EXCEEDED_WRAPPER_LIMIT = 302; //Достигнут предел, указанный в проигрывателе. Получено слишком много ответов с элементами Wrapper, не содержащих ответ InLine.
	public static final int ERROR_CODE_NO_FILE = 401; //Файл не найден. Не удалось обнаружить параметр Linear/MediaFile в URI.
	public static final int ERROR_CODE_BAD_FILE = 403; //Не удалось найти элемент MediaFile с указанными атрибутами, поддерживаемый этим проигрывателем.
	public static final int ERROR_CODE_ERROR_SHOWING = 405; //Не удалось найти элемент MediaFile с указанными атрибутами, поддерживаемый этим проигрывателем.
	public static final int ERROR_CODE_COMPANION_NODE_NOT_FOUND = 603; //Не удалось получить ресурс CompanionAds или Companion.
	public static final int ERROR_CODE_COMPANION_NOT_FOUND = 604; //Не удалось найти ресурс Companion поддерживаемого типа.
	public static final int ERROR_CODE_UNKNOWN = 900; //Не удалось найти ресурс Companion поддерживаемого типа.

	// Tracking xpath expressions
	private static final String inlineLinearTrackingXPATH = "/VASTS/VAST/Ad/InLine/Creatives/Creative/Linear/TrackingEvents/Tracking";
	private static final String inlineNonLinearTrackingXPATH = "/VASTS/VAST/Ad/InLine/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";
	private static final String wrapperLinearTrackingXPATH = "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/Linear/TrackingEvents/Tracking";
	private static final String wrapperNonLinearTrackingXPATH = "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";
	private static final String companionsXPATH = "/VASTS/VAST/Ad/InLine/Creatives/Creative/CompanionAds/Companion";
	private static final String nonLinearXPATH = "/VASTS/VAST/Ad/InLine/Creatives/Creative/NonLinearAds/NonLinear";

	private static final String wrapperExtensionPATH = "/VASTS/VAST/Ad/Wrapper/Extensions/Extension";
	private static final String extensionPATH = "/VASTS/VAST/Ad/InLine/Extensions/Extension";
	private static final String combinedExtensionPATH = wrapperExtensionPATH
			+ "|"
			+ extensionPATH;

	private static final String combinedTrackingXPATH = inlineLinearTrackingXPATH
			+ "|"
			+ inlineNonLinearTrackingXPATH
			+ "|"
			+ wrapperLinearTrackingXPATH + "|" + wrapperNonLinearTrackingXPATH;

	// Mediafile xpath expression
	private static final String mediaFileXPATH = "//MediaFile";

	// Duration xpath expression
	private static final String durationXPATH = "//Duration";

	// Duration xpath expression
	private static final String linearXPATH = "//Linear";

	// Videoclicks xpath expression
	private static final String videoClicksXPATH = "//VideoClicks";

	// Videoclicks xpath expression
	private static final String impressionXPATH = "//Impression";

	// Error url  xpath expression
	private static final String errorUrlXPATH = "//Error";

	// Duration xpath expression
	private static final String adParametersXPATH = "//AdParameters";

	public VASTModel(Document vasts) {

		this.vastsDocument = vasts;

	}


	public Document getVastsDocument() {
		return vastsDocument;
	}

	public HashMap<TRACKING_EVENTS_TYPE, List<String>> getTrackingUrls() {
		VASTLog.d(TAG, "getTrackingUrls");

		List<String> tracking;

		HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings = new HashMap<TRACKING_EVENTS_TYPE, List<String>>();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(combinedTrackingXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;
			String trackingURL;
			String eventName;
			TRACKING_EVENTS_TYPE key;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					NamedNodeMap attributes = node.getAttributes();

					eventName = (attributes.getNamedItem("event"))
							.getNodeValue();
					try {
						key = TRACKING_EVENTS_TYPE.valueOf(eventName);
					} catch (IllegalArgumentException e) {
						VASTLog.w(TAG, "Event:" + eventName
								+ " is not valid. Skipping it.");
						continue;
					}

					trackingURL = XmlTools.getElementValue(node);

					if (trackings.containsKey(key)) {
						tracking = trackings.get(key);
						tracking.add(trackingURL);
					} else {
						tracking = new ArrayList<String>();
						tracking.add(trackingURL);
						trackings.put(key, tracking);

					}

				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return trackings;
	}

	public List<VASTMediaFile> getMediaFiles() {
		VASTLog.d(TAG, "getMediaFiles");

		ArrayList<VASTMediaFile> mediaFiles = new ArrayList<VASTMediaFile>();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(mediaFileXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;
			VASTMediaFile mediaFile;
			String mediaURL;
			Node attributeNode;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					mediaFile = new VASTMediaFile();
					node = nodes.item(i);
					NamedNodeMap attributes = node.getAttributes();

					attributeNode = attributes.getNamedItem("apiFramework");
					mediaFile.setApiFramework((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes.getNamedItem("bitrate");
					mediaFile.setBitrate((attributeNode == null) ? null
							: new BigInteger(attributeNode.getNodeValue()));

					attributeNode = attributes.getNamedItem("delivery");
					mediaFile.setDelivery((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes.getNamedItem("height");
					mediaFile.setHeight((attributeNode == null) ? null
							: new BigInteger(attributeNode.getNodeValue()));

					attributeNode = attributes.getNamedItem("id");
					mediaFile.setId((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes
							.getNamedItem("maintainAspectRatio");
					mediaFile
							.setMaintainAspectRatio((attributeNode == null) ? null
									: Boolean.valueOf(attributeNode
									.getNodeValue()));

					attributeNode = attributes.getNamedItem("scalable");
					mediaFile.setScalable((attributeNode == null) ? null
							: Boolean.valueOf(attributeNode.getNodeValue()));

					attributeNode = attributes.getNamedItem("type");
					mediaFile.setType((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes.getNamedItem("width");
					mediaFile.setWidth((attributeNode == null) ? null
							: new BigInteger(attributeNode.getNodeValue()));

					mediaURL = XmlTools.getElementValue(node);
					mediaFile.setValue(mediaURL);

					mediaFiles.add(mediaFile);
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return mediaFiles;
	}

	public String getDuration() {
		VASTLog.d(TAG, "getDuration");

		String duration = null;

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(durationXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					duration = XmlTools.getElementValue(node);
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return duration;
	}

	public VASTCompanion getCompanion(Pair<Integer, Integer> screenSize) {
		VASTLog.d(TAG, "checkCompanion");

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(companionsXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;
			if (nodes != null) {
				if (nodes.getLength() == 0) {
					try {
						sendError(VASTModel.ERROR_CODE_COMPANION_NODE_NOT_FOUND);
					} catch (Exception ex) {
						VASTLog.e(TAG, ex.getMessage());
					}
				} else {
					Map<Float, VASTCompanion> map = new HashMap<Float, VASTCompanion>();
					for (int i = 0; i < nodes.getLength(); i++) {
						node = nodes.item(i);
						NamedNodeMap attributes = node.getAttributes();
						Node heightNode = attributes.getNamedItem("height");
						Node widthNode = attributes.getNamedItem("width");
						if (heightNode != null && widthNode != null) {
							int height = Integer.valueOf(heightNode.getNodeValue());
							int width = Integer.valueOf(widthNode.getNodeValue());
							float companionAspectRatio = (float) (Math.max(width, height)) / Math.min(width, height);
							if (Math.min(width, height) >= 250 && companionAspectRatio <= 2.5) {
								VASTCompanion vastCompanion = new VASTCompanion(node);
								if (vastCompanion.isValid(screenSize.first, screenSize.second)) {
									map.put((float) width / height, vastCompanion);
								}
							}
						}
					}
					if (!map.isEmpty()) {
						float currentAspectRatio = (float) screenSize.first/screenSize.second;
						Set<Float> keys = map.keySet();
						float closest = keys.iterator().next();
						for (float i: keys) {
							if (Math.abs(closest - currentAspectRatio) > Math.abs(i - currentAspectRatio)) {
								closest = i;
							}
						}
						return map.get(closest);
					}
					try {
						sendError(VASTModel.ERROR_CODE_COMPANION_NOT_FOUND);
					} catch (Exception ex) {
						VASTLog.e(TAG, ex.getMessage());
					}
				}
			} else {
				try {
					sendError(ERROR_CODE_COMPANION_NODE_NOT_FOUND);
				} catch (Exception ex) {
					VASTLog.e(TAG, ex.getMessage());
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			try {
				sendError(VASTModel.ERROR_CODE_COMPANION_NODE_NOT_FOUND);
			} catch (Exception ex) {
				VASTLog.e(TAG, ex.getMessage());
			}
			return null;
		}
		return null;
	}

	public VASTCompanion getBanner(Activity activity) {
		VASTLog.d(TAG, "checkCompanion");
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(companionsXPATH, vastsDocument, XPathConstants.NODESET);
			Node node;
			if (nodes != null) {
				if (nodes.getLength() > 0) {
					for (int i = 0; i < nodes.getLength(); i++) {
						node = nodes.item(i);
						NamedNodeMap attributes = node.getAttributes();
						Node heightNode = attributes.getNamedItem("height");
						Node widthNode = attributes.getNamedItem("width");
						if (heightNode != null && widthNode != null) {
							int height = Integer.valueOf(heightNode.getNodeValue());
							int width = Integer.valueOf(widthNode.getNodeValue());
							if (Utils.isTablet(activity) && width == 728 && height == 90) {
								return new VASTCompanion(node);
							} else if (width == 320 && height == 50) {
								return new VASTCompanion(node);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
		}
		return null;
	}

	public int getSkipoffset() {
		VASTLog.d(TAG, "getSkipoffset");

		int skipTime = 0;

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(linearXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					Node skipOffsetNode = node.getAttributes().getNamedItem("skipoffset");
					if (skipOffsetNode != null) {
						String skipOffset = skipOffsetNode.getNodeValue();
						if (skipOffset != null) {
							String[] units = skipOffset.split(":");
							int hours = Integer.parseInt(units[0]);
							int minutes = Integer.parseInt(units[1]);
							int seconds = Integer.parseInt(units[2]);
							skipTime = hours * 60 * 60 + minutes * 60 + seconds;
						}
					}
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return 0;
		}

		return skipTime;
	}

	public VideoClicks getVideoClicks() {
		VASTLog.d(TAG, "getVideoClicks");

		VideoClicks videoClicks = new VideoClicks();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(videoClicksXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);

					NodeList childNodes = node.getChildNodes();

					Node child;
					String value;

					for (int childIndex = 0; childIndex < childNodes
							.getLength(); childIndex++) {

						child = childNodes.item(childIndex);
						String nodeName = child.getNodeName();

						if (nodeName.equalsIgnoreCase("ClickTracking")) {
							value = XmlTools.getElementValue(child);
							videoClicks.getClickTracking().add(value);

						} else if (nodeName.equalsIgnoreCase("ClickThrough")) {
							value = XmlTools.getElementValue(child);
							videoClicks.setClickThrough(value);

						} else if (nodeName.equalsIgnoreCase("CustomClick")) {
							value = XmlTools.getElementValue(child);
							videoClicks.getCustomClick().add(value);
						}
					}
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return videoClicks;
	}

	public List<String> getImpressions() {
		VASTLog.d(TAG, "getImpressions");

		return getListFromXPath(impressionXPATH);

	}

	public List<String>  getErrorUrl() {

		VASTLog.d(TAG, "getErrorUrl");

		return getListFromXPath(errorUrlXPATH);

	}



	private List<String>  getListFromXPath(String xPath) {

		VASTLog.d(TAG, "getListFromXPath");

		ArrayList<String> list = new ArrayList<String>();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(xPath,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					list.add(XmlTools.getElementValue(node));
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return list;

	}

	public String getAdParameterms() {
		VASTLog.d(TAG, "getAdParameterms");

		String adParameters = null;

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(adParametersXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					adParameters = XmlTools.getElementValue(node);
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return adParameters;
	}

	public Extensions getExtensions() {
		VASTLog.d(TAG, "getExtensions");
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(combinedExtensionPATH,
					vastsDocument, XPathConstants.NODESET);
			if (nodes != null && nodes.getLength() > 0) {
				for (int i = 0; i < nodes.getLength(); ++i) {
					Node child = nodes.item(i);
					if (child != null) {
						NamedNodeMap attributes = child.getAttributes();
						if (attributes != null) {
							Node typeNode = attributes.getNamedItem("type");
							if (typeNode != null && typeNode.getNodeValue().equals("appodeal")) {
								return new Extensions(child);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return null;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		VASTLog.d(TAG, "writeObject: about to write");
		oos.defaultWriteObject();

		String data = XmlTools.xmlDocumentToString(vastsDocument);
		// oos.writeChars();
		oos.writeObject(data);
		VASTLog.d(TAG, "done writing");

	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		VASTLog.d(TAG, "readObject: about to read");
		ois.defaultReadObject();

		String vastString = (String) ois.readObject();
		VASTLog.d(TAG, "vastString data is:\n" + vastString + "\n");

		vastsDocument = XmlTools.stringToDocument(vastString);

		VASTLog.d(TAG, "done reading");
	}

	public String getPickedMediaFileURL() {
		return pickedMediaFile.getValue();
	}
	public String getPickedMediaFileType() {
		return pickedMediaFile.getType();
	}

	public void setPickedMediaFile(VASTMediaFile pickedMediaFile) {
		this.pickedMediaFile = pickedMediaFile;
	}

	public VASTMediaFile getPickedMediaFile() {
		return pickedMediaFile;
	}

	public void sendError(int errorCode) {
		List<String> errorUrls = getErrorUrl();
		if (errorUrls != null) {

			for (String url : errorUrls) {
				url = replaceMacros(url, errorCode);
				VASTLog.v(TAG, "Fire error url:" + url);
				HttpTools.httpGetURL(url);
			}

		} else {
			VASTLog.d(TAG, "Error url list is null");
		}

	}

	private String replaceMacros(String url, int errorCode) {
		if (url != null) {
			if (url.contains("[ERRORCODE]")) {
				url = url.replace("[ERRORCODE]", String.valueOf(errorCode));
			}
			if (url.contains("%5BERRORCODE%5D")) {
				url = url.replace("%5BERRORCODE%5D", String.valueOf(errorCode));
			}
		}
		return url;
	}

}
