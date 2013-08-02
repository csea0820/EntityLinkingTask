/**
 * @Author Xiaofeng
 * @Date 2013-8-2 下午5:24:42
 */
package com.nec.scg.senseGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Set;
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
	private final String KB_DIR = "D:\\KBP数据集\\TAC2009\\LDC2009E58A(knowledge base)\\TAC_2009_KBP_Evaluation_Reference_Knowledge_Base\\data";
	
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
				ret.add(nodes.item(i).getAttributes().getNamedItem("wiki_title").getNodeValue());
			}
		} catch (ParserConfigurationException | XPathExpressionException
				| SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public Set<String> getKbArticleNames(){
		File dir = new File(KB_DIR);
		if (dir.isDirectory()){
			File[] files = dir.listFiles();
			for (File file : files)
				articleNames.addAll(getArticleNames(file));
			
		}
		return articleNames;
	}
	
	private void saveAllKbArticleNames(){
		StringBuilder sb = new StringBuilder();
		for (String name : articleNames)
			sb.append(name).append("\n");
		Utility.writeToFile("D:\\TAC_RESULT\\AllKbArticleNames", sb.toString());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new KnowledgeBase().getKbArticleNames();
	}

}
