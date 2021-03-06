package com.nec.scg.senseGenerator;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.nec.scg.senseRanking.ArticleAttributes;
import com.nec.scg.utility.Utility;
/*
 * table 1,TreeMap<normalizedTitleName, TreeSet<redirectName>>()
 */
public class NARLinking extends AbstractLinking {

	private String titleName;
	private String normalizedTitleName = null;
	private String redirectName = null;
	private Map<String, Set<String>> NAR;

//	boolean ignoreTitle = true;

	NARLinking(String outputPath) {
		super(outputPath);
	}

	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数
		NAR = new TreeMap<String, Set<String>>();

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("redirect") && ignoreTitle(normalizedTitleName) == false) {
			redirectName = attributes.getValue(0);

			Set<String> set = NAR.get(normalizedTitleName);
			if (set == null) {
				set = new TreeSet<String>();
				NAR.put(normalizedTitleName, set);
			}
			set.add(redirectName);
		} else if (qName.equals("text")) { 
//			ignoreTitle = true;
			redirectName = null;
		}
		preTag = qName;

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("text") && ignoreTitle(normalizedTitleName) == false) {
			if (redirectName == null) {
				Set<String> set = NAR.get(normalizedTitleName);
				if (set == null) {
					set = new TreeSet<String>();
					NAR.put(normalizedTitleName, set);
				}
				set.add(titleName);
				//System.out.println("adding(key="+normalizedTitleName+",value="+titleName);
			}
		}
		preTag = null;

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if ("title".equals(preTag)) {

			String content = new String(ch, start, length);

			titleName = content;
//			if (titleName.trim().equals("Abbott Laboratories")) {
//				System.err.println("found");
//			}
//			ignoreTitle = ignoreTitle(titleName);
			timeStamp();
//			if (!titleName.equals("Abbott Laboratories")) {
//				return;
//			}
			/*
			 * normalizedName 通过调研发现title的（）目前只出现在句尾。所以去（）标签同时转化为小写
			 */
			if (content.contains("(")) {
				normalizedTitleName = content
						.substring(0, content.indexOf("(")).toLowerCase()
						.trim();
			} else {
				normalizedTitleName = content.toLowerCase().trim();
			}

		}
	}

	public String getShortName(String name) {
		String result = "";
		for (String s : name.split("\\s")) {
			if (!"of".equals(s) && s.length() > 0)
				result += s.charAt(0);
		}
		return result.toLowerCase();
	}

	private static boolean ignoreTitle(String str) {
		 if (str.contains(":"))
			 return true;
		return false;

	}

	// 字符串subString是否是mainString的子串
	private  boolean isSubString(String mainString, String subString) {
		int index = mainString.indexOf(subString);
		if (index == -1 || (index != 0 && mainString.charAt(index - 1) != ' '))
			return false;
		int lastIndex = index + subString.length();
		if (lastIndex < mainString.length()
				&& mainString.charAt(lastIndex) != ' ')
			return false;
		return true;
	}

	@Override
	protected Set<ArticleAttributes> senseGenerator(String query) {
		Set<ArticleAttributes> candidates = new TreeSet<ArticleAttributes>();
			for (String nor : NAR.keySet()) {
				if (!nor.equals("")) {
					boolean substr_test = false;
					boolean editDist_test = false;
					if (nor.equals(query) || (substr_test = isSubString(nor, query))
							|| (substr_test =isSubString(query, nor)) ||(nor.length() == query.length()&&(editDist_test = (Utility.getEditDistance(nor, query)*1.0/query.length() <= 0.2)))) {
							
						for (String ar : NAR.get(nor)){
							ArticleAttributes art = new ArticleAttributes(ar);
							art.setEditDistance_test(editDist_test);
							art.setSubstr_test(substr_test);
							candidates.add(art);
						}
					}
//					 else {
//					 String sn = getShortName(nor);
//					 if (sn.equals(query))
//						 candidates.addAll(NAR.get(nor));
//					 }
				}
			}
		return candidates;
	}
	
	public void info() {
		for (String key : NAR.keySet()) {
			System.out.println("Key=" + key + "," + NAR.get(key));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String filename
		// ="C:\\chenxiulongdata\\enwiki-latest-pages-articles.xml";
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		NARLinking firsttable = new NARLinking("D:\\TAC_RESULT\\NAR");
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), firsttable);
			//firsttable.info();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		// System.out.println(isSubString("DJDJF DJFJ KDFK",
		// "DJDJF DJFJ KDFK"));

		/*
		 * 遍历输出
		 */

	}

	@Override
	protected void setOutputPrefixName() {
		this.outputPrefixName = "NAR";
	}

}
