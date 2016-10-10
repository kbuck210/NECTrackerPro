package com.nectp.webtools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private final ArrayList<Element> elements = new ArrayList<Element>();
    
    private static final Logger log = Logger.getLogger(DOMParser.class.getName());

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
        	log.severe("File null/not found!");
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
    
    /** Creates an instance of the DOM parser for the given InputStream (i.e. for reading web-uploaded Files) - NOTE: need to set qualified name
     * 
     * @param iStream the InputStream to parse
     * @return a DOMParser object containing the normalized DOM from the parsed XML, null if no XML file defined
     */
    public static DOMParser newInstance(final InputStream iStream) {
    	 DOMParser parser = new DOMParser();
    	 if (iStream == null) {
    		 log.severe("InputStream not defined! can not parse XML");
    		 return null;
    	 }
    	 
    	 //	Create the document builder factory
         parser.dbFactory = DocumentBuilderFactory.newInstance();
         try {
             //	Create the document builder & parse the XML file into the document
             parser.dBuilder = parser.dbFactory.newDocumentBuilder();
             parser.doc = parser.dBuilder.parse(iStream);
             
             //	Normalize the document to fix newlines etc.
             parser.doc.getDocumentElement().normalize();
         } catch (ParserConfigurationException e) {
        	 log.severe("ParserConfigurationException: " + e.getMessage());
             e.printStackTrace();
         } catch (SAXException e) {
        	 log.severe("SAXException: " + e.getMessage());
             e.printStackTrace();
         } catch (IOException e) {
        	 log.severe("IOException: " + e.getMessage());
             e.printStackTrace();
         }

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
    public void setQualifiedNodeName(final String qName) {
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
     * @return a ArrayList of Elements found from nodes in the DOM, empty list if either the qualified name or document are undefined
     */
    public List<Element> generateElementList() {
        ArrayList<Element> eList = new ArrayList<Element>();
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

    /** Returns a List containing the child element nodes corresponding to the specified tag name
     * 
     * @param parentElement
     * @param tagName
     * @return
     */
    public List<Element> getSubElementsByTagName(final Element parentElement, final String tagName) {
        NodeList nodes = parentElement.getElementsByTagName(tagName);
        ArrayList<Element> subElements = new ArrayList<Element>();
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
    public String getTextSubElementByTagName(final Element parentElement, final String tagName) {
    	List<String> nodes = getTextSubElementsByTagName(parentElement, tagName);
    	if (nodes.size() == 1) {
    		return nodes.get(0);
    	}
    	else return null;
    }
    
    /** Gets the text nodes with the specified tag name as a list of Strings
     * 
     * @param parentElement the DOM element that is a parent of the specified tag
     * @param tagName the qualified node name to search for within the parent
     * @return a List of strings containing the text from the text Nodes
     */
    public List<String> getTextSubElementsByTagName(final Element parentElement, final String tagName) {
    	List<String> nodes = new ArrayList<String>();
    	NodeList list = parentElement.getElementsByTagName(tagName);
    	String text = null;
    	if (list != null && list.getLength() > 0) {
    		for (int i = 0; i < list.getLength(); ++i) {
    			text = list.item(i).getTextContent();
    			if (text != null) {
    				text.trim();
    				nodes.add(text);
    			}
    		} 
    	}
        return nodes;
    }

    /** Returns the list of DOM elements that were parsed for the given qualified node name
     * 
     * @return a ArrayList of Elements found from nodes in the DOM
     */
    public List<Element> getDOMElements() {
        return elements;
    }
}

