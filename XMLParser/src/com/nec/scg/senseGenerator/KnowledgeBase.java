/**
 * @Author Xiaofeng
 * @Date 2013-8-2 下午5:24:42
 */
package com.nec.scg.senseGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

public class KnowledgeBase {

	Set<String> articleNames = null;
	private final String KB_DIR = "D:\\KBP数据集\\TAC2009\\TAC_2009_KBP_Evaluation_Reference_Knowledge_Base\\data";
	private final String CACHE_FILE = "D:\\TAC_RESULT\\AllKbArticleNames";
	
	public KnowledgeBase(){
		articleNames = new TreeSet<String>();
	}
	
	
	
	private Set<String> getArticleNames(File file){
		Set<String> ret = new TreeSet<String>();
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder;
		try {
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(file);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("/knowledge_base/entity");

			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				ret.add(nodes.item(i).getAttributes().getNamedItem("name").getNodeValue());
			}
		} catch (ParserConfigurationException | XPathExpressionException
				| SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public Set<String> getKbArticleNames(){
		
		File cache = new File(CACHE_FILE);
		if (cache.exists()){
			return getCaches(cache);
		}
		
		File dir = new File(KB_DIR);
		if (dir.isDirectory()){
			File[] files = dir.listFiles();
			for (File file : files)
				articleNames.addAll(getArticleNames(file));
			
		}
		saveAllKbArticleNames();
		System.out.println("total article = " + articleNames.size());
		return articleNames;
	}
	
	private Set<String> getCaches(File file){
		Set<String> ret = new TreeSet<String>();
		
			BufferedReader br = null;
			FileReader fr = null;
			String str;

			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				str = br.readLine();

				while (str != null) {
					ret.add(str.trim());
					str = br.readLine();

				}
			}catch (IOException e) {
				e.printStackTrace();
			}finally{
				
				try {
					fr.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		return ret;
	}
	
	private void saveAllKbArticleNames(){
		StringBuilder sb = new StringBuilder();
		for (String name : articleNames)
			sb.append(name).append("\n");
		
		Utility.writeToFile(CACHE_FILE, sb.toString());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new KnowledgeBase().getKbArticleNames();
	}

}
