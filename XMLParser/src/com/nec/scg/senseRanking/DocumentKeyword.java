/**
 * @Author Xiaofeng
 * @Date 2013-7-30 ����10:09:00
 */
package com.nec.scg.senseRanking;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nec.scg.utility.Utility;

public class DocumentKeyword {

	private static DocumentKeyword instance = null;

	Map<String, Set<String>> caches = new TreeMap<String, Set<String>>();
	
	Set<String> keywords = null;
	Map<String,Integer> keywords_prefix = null;

	private DocumentKeyword() {
		keywords = Utility.getKeywords("D:\\TAC_RESULT\\keywords.txt"); 
		
		keywords_prefix = new TreeMap<String,Integer>();
		
		for (String key : keywords)
		{
			keywords_prefix.put(key,1);
			int index = 0;
			while ((index = key.indexOf(" ", index)) != -1)
			{
				String s = key.substring(0, index).toLowerCase();
				if (keywords.contains(s))
					keywords_prefix.put(s,1);
				else keywords_prefix.put(s, 0);
				index++;
			}
		}
	}

	public static DocumentKeyword getinstance() {
		if (instance == null)
			instance = new DocumentKeyword();
		return instance;
	}

	public Set<String> getKeywords(Query query) {
		String key = query.getId() + "_" + query.getQuery();
		if (caches.containsKey(key))
			return caches.get(key);
		
		Set<String> ret = new TreeSet<String>();
		
		String[] words = getDocContent(query.getDocument()).split("\\s+");
		
		int words_pos = 0;
		
		while (words_pos < words.length)
		{
			StringBuilder  str = new StringBuilder(words[words_pos++]);
			
			int offset = 0;
			String matchString = null;
			while (keywords_prefix.containsKey(str.toString()))
			{
				//System.out.println(str.toString());
				if (keywords_prefix.get(str.toString()) == 1)
					matchString = str.toString();
				if (words_pos + offset  < words.length)
					str.append(" ").append(words[words_pos + offset++]);
				else break;
			}
			
			if (matchString != null)
				ret.add(matchString);
		}
		caches.put(key, ret);
		return ret;
	}
	
	public void saveCaches(){
		Utility.saveCache(caches, "D:\\TAC_RESULT\\documentKeywords.txt");
	}

	private String getDocContent(File file) {

		StringBuilder ret = new StringBuilder();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder;
		try {
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(file);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("/DOC/BODY/TEXT/P");

			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				String content = nodes.item(i).getTextContent().trim()
						.replaceAll("\n", " ");
				ret.append(content).append("\n");
			}
		} catch (ParserConfigurationException | XPathExpressionException
				| SAXException | IOException e) {
			e.printStackTrace();
		}
		return ret.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}