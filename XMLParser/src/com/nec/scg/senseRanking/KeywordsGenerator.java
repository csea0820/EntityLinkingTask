/**
 * @Author Xiaofeng
 * @Date 2013-6-17 下午3:48:08
 */
package com.nec.scg.senseRanking;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.nec.scg.senseGenerator.STEMLinking;
import com.nec.scg.utility.Utility;

public class KeywordsGenerator extends STEMLinking{

	Map<String,Set<String>> links = new TreeMap<String,Set<String>>();
	
	protected KeywordsGenerator(String outputPath) {
		super(outputPath);
	}
	
	

	@Override
	public void endDocument() throws SAXException {
	}
	
	protected void addToSETM(String source, String target) {
		Set<String> set = links.get(source);
		if (set == null){
			set = new TreeSet<String>();
			links.put(source, set);
		}
		set.add(target);
	}
	
	public void keywordsGenerator()
	{
		StringBuilder sb = new StringBuilder();
		int cnt = 0;
		for (String key : links.keySet())
		{
			if (links.get(key).size() == 1 && validString(key))
			{
				key = key.replaceAll("\"", "");
				key = key.replaceAll("''", "");
				key = key.replaceAll("”", "");
				key = key.replaceAll("“", "");
				sb.append(key.trim()).append("\n");
				cnt++;
			}
		}
		
		Utility.writeToFile("D:\\TAC_RESULT\\keywords.txt", sb.toString());
		System.out.println("key words cnt = " + cnt);
	}
	
	
	private boolean validString(String str)
	{
		if (str.contains(":") || str.contains("{") || str.contains("}")
				|| str.contains("=") || str.contains("<") || str.contains(">")
				|| str.contains("[") || str.contains("]") || str.contains("!") ||
				str.contains("#") || str.contains("$") || str.contains("&") || str.contains("?"))return false;
		return true;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = spf.newSAXParser();
			KeywordsGenerator key = null;
			saxParser.parse(new File(filename),key = new KeywordsGenerator(""));
			key.keywordsGenerator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		Set<String> set = Utility.getKeywords("D:\\TAC_RESULT\\keywords.txt");
//		StringBuilder sb = new StringBuilder();
//		for (String s: set)
//		{
//			if (s.trim().endsWith(")") || s.trim().contains("?") || s.trim().length() < 5
//					|| s.contains("&nbsp;"))continue;
//				sb.append(s).append("\n");
//		}
//		Utility.writeToFile("D:\\TAC_RESULT\\keywords.txt", sb.toString());
		
		
//		System.out.println("''dfjfdj''".replaceAll("''", ""));
	}

}
