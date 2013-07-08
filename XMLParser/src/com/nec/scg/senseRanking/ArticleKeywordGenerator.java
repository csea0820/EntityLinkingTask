/**
 * @Author Xiaofeng
 * @Date 2013-6-24 下午2:02:15
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
import com.nec.scg.senseRanking.Linkability.Entity;
import com.nec.scg.utility.Utility;

public class ArticleKeywordGenerator extends STEMLinking {

	//<String,Set<String>>: hypertext链接到的article集合
	Map<String, Set<String>> keywordInfo = new TreeMap<String, Set<String>>();
	//<String,Set<String>>: 出现hypertext的article集合
	Map<String, Set<String>> links = new TreeMap<String, Set<String>>();

//	Map<String,Integer> linkCnt = new TreeMap<String,Integer>();
	
	// String preTag = null;
	String titleName = null;

	protected ArticleKeywordGenerator(String outputPath) {
		super(outputPath);
	}

	@Override
	public void endDocument() throws SAXException {
		
//		StringBuilder sb = new StringBuilder();
//		sb.append("#word,linkCount\n");
//		for (String key : linkCnt.keySet())
//		{
//			Integer e = linkCnt.get(key);
//			sb.append(key).append(",").append(e).append("\n");
//		}
//		
//		Utility.writeToFile("D:\\TAC_RESULT\\links.txt", sb.toString());
		Map<String,Set<String>> articleTerms = new TreeMap<String,Set<String>>();
		
		for (String term : links.keySet())
		{
			term.replaceAll("\n", " ");
			for (String article : links.get(term))
			{
				Set<String> set = articleTerms.get(article);
				if (set == null){
					set = new TreeSet<String>();
					articleTerms.put(article, set);
				}
				set.add(term);
			}
		}
		links = null;
		keywordInfo = null;
		System.gc();
		int count = 0;
		StringBuilder sb = new StringBuilder();
		for (String article : articleTerms.keySet()){
			sb.append(article);
			if (++count % 10000 == 0)System.out.println(count);
			for (String term : articleTerms.get(article))
				sb.append(",").append(term);
			sb.append("\n");
		}
		Utility.writeToFile("D:\\TAC_RESULT\\articleTerms.txt", sb.toString());
		
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length);
			if (preTag.equals("text")) {
				text_content.append(content);
			} else if ("title".equals(preTag)) {
				titleName = content;
			}
		}
	}

	public void keywordsGenerator() {
		// StringBuilder sb = new StringBuilder();
		// int cnt = 0;
		// for (String key : links.keySet())
		// {
		// if (links.get(key).size() == 1 && validString(key))
		// {
		// key = key.replaceAll("\"", "");
		// key = key.replaceAll("''", "");
		// key = key.replaceAll("”", "");
		// key = key.replaceAll("“", "");
		// sb.append(key.trim()).append("\n");
		// cnt++;
		// }
		// }
		//
		// Utility.writeToFile("D:\\TAC_RESULT\\keywords.txt", sb.toString());
		// System.out.println("key words cnt = " + cnt);
	}
	
	public Map<String, Set<String>> getLinks() {
		return links;
	}

	protected void addToSETM(String source, String target) {

		// 判断source是否为关键词
		Set<String> targetArticles = keywordInfo.get(source);
		if (targetArticles == null) {
			targetArticles = new TreeSet<String>();
			keywordInfo.put(source, targetArticles);
		}
		targetArticles.add(target);
		if (targetArticles.size() > 1) {
//			linkCnt.remove(source);
			links.remove(source);
			return;
		}

		// 更新以source为关键词的article列表
		Set<String> set = links.get(source);
		if (set == null) {
			set = new TreeSet<String>();
			links.put(source, set);

		}
		set.add(titleName);
		
//		Integer  i = linkCnt.get(source);
//		if (i == null)i = 0;
//		linkCnt.put(source, i+1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser
					.parse(new File(filename), new ArticleKeywordGenerator(""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
