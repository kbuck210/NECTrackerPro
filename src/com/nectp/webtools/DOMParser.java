package com.nectp.webtools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DOMParser {

    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder dBuilder;
    private Document doc;
    private String qName;
    private final LinkedList<Element> elements = new LinkedList<Element>();

    //	Default constructor is private to avoid instantiation
    private DOMParser() { }

    /** Create an instance of the DOM Parser for the given XML file
     * 
     * @param xmlFile the XML file for which to generate the Document Object Model
     * @param qName the qualified node name to parse, if null, must be set after instantiation
     * @return a DOMParser object containing the normalized DOM from the parsed XML, null if no XML file defined
     */
    public static DOMParser newInstance(final File xmlFile, final String qName) {
        DOMParser parser = new DOMParser();

        //	Return null if the xmlFile is undefined
        if (xmlFile == null || !xmlFile.exists()) {
        	System.out.println("ERROR: File not found.");
            return null;
        }

        //	Create the document builder factory
        parser.dbFactory = DocumentBuilderFactory.newInstance();
        try {
            //	Create the document builder & parse the XML file into the document
            parser.dBuilder = parser.dbFactory.newDocumentBuilder();
            parser.doc = parser.dBuilder.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //	Normalize the document to fix newlines etc.
        parser.doc.getDocumentElement().normalize();

        parser.setQualifiedNodeName(qName);

        return parser;
    }
    
    /** Create an instance of a DOMParser for the specified URL and qualified node name
    * 
    * @param urlStr the URL string representing the XML to parse
    * @param qName the qualified node name to parse, if null, must be set after instantiation
    * @return a DOMParser object containing the normalized DOM from the parsed XML, null if no XML file defined
    */
    public static DOMParser newInstance(final String urlStr, final String qName) {
        URL url;
        InputStream istream;
        try {
            url = new URL(urlStr);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(false);
            huc.setConnectTimeout(15 * 1000);
            huc.setRequestMethod("GET");
            huc.connect();
            istream = huc.getInputStream();
        } catch (MalformedURLException e) {
            Logger.getLogger(DOMParser.class.getName()).log(Level.SEVERE, "Malformed URL Exception: " + e.getMessage(), e);
            return null;
        } catch (IOException e) {
            Logger.getLogger(DOMParser.class.getName()).log(Level.SEVERE, "I/O Exception with URL input stream: " + e.getMessage(), e);
            return null;
        }

        DOMParser parser = new DOMParser();
        //  Create the document builder factory
        parser.dbFactory = DocumentBuilderFactory.newInstance();
        try {
            //	Create the document builder & parse the input stream from the url into the document
            parser.dBuilder = parser.dbFactory.newDocumentBuilder();
            parser.doc = parser.dBuilder.parse(istream);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //	Normalize the document to fix newlines etc.
        parser.doc.getDocumentElement().normalize();

        parser.setQualifiedNodeName(qName);

        return parser;
    }

    /** Create an instance of the DOM Parser for the given XML file, skipping setting the qualified node name
     * 
     * @param xmlFile the XML file for which to generate the Document Object Model
     * @return a DOMParser object containing the normalized DOM from the parsed XML, null if no XML file defined
     */
    public static DOMParser newInstance(final File xmlFile) {
        return newInstance(xmlFile, null);
    }

    /** Set the qualified node name of the elements to list
     * 
     * @param qName the String representation of the element node names to list 
     */
    protected void setQualifiedNodeName(final String qName) {
        if (qName != null) {
            this.qName = qName.trim().toLowerCase();
        }
    }
    
    /** Gets the root XML element
     * 
     * @return the Element that represents the root element for the document
     */
    public Element getRootElement() {
    	Element root = null;
    	if (doc != null) {
    		root = doc.getDocumentElement();
    	}
    	return root;
    }

    /** Generates a list of element nodes that were found in the DOM for the specified qualified name
     * 
     * @return a LinkedList of Elements found from nodes in the DOM, empty list if either the qualified name or document are undefined
     */
    public LinkedList<Element> generateElementList() {
        LinkedList<Element> eList = new LinkedList<Element>();
        if (qName !=  null && doc != null) {
            NodeList nodes = doc.getElementsByTagName(qName);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        eList.add(element);
                    }
                }
            }
        }

        return eList;
    }

    /** Returns a LinkedList containing the child element nodes corresponding to the specified tag name
     * 
     * @param parentElement
     * @param tagName
     * @return
     */
    public static LinkedList<Element> getSubElementsByTagName(final Element parentElement, final String tagName) {
        NodeList nodes = parentElement.getElementsByTagName(tagName);
        LinkedList<Element> subElements = new LinkedList<Element>();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                subElements.add(el);
            }
        }
        return subElements;
    }

    /** Gets the text value of a sub-element of the specfied parent element based on the specified tag name
     * 
     * @param parentElement
     * @param tagName
     * @return
     */
    public static String getTextSubElementByTagName(final Element parentElement, final String tagName) {
    	NodeList list = parentElement.getElementsByTagName(tagName);
    	String text = null;
    	if (list != null && list.getLength() > 0) {
    		text = list.item(0).getTextContent();
    		 if (text != null) text.trim();
    	}
        return text;
    }

    /** Returns the list of DOM elements that were parsed for the given qualified node name
     * 
     * @return a LinkedList of Elements found from nodes in the DOM
     */
    public LinkedList<Element> getDOMElements() {
        return elements;
    }
}
