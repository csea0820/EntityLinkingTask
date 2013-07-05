/**
 * @Author Xiaofeng
 * @Date 2013-6-25 下午5:11:14
 */
package com.nec.scg.senseRanking;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.nec.scg.senseGenerator.AbstractLinking;
import com.nec.scg.utility.Utility;

public class WordOccurrence extends AbstractLinking{

	protected StringBuilder text_content = null;
	Set<String> keywords = null;
	Map<String,Integer> keywords_prefix = null;
	Map<String,Integer> occurrenceCount = null;
	
	
	WordOccurrence(String outputPath) {
		super(outputPath);
	}

	
	@Override
	protected Set<String> senseGenerator(String query) {
		return null;
	}

	@Override
	protected void setOutputPrefixName() {		
	}
	
	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();
		occurrenceCount = new TreeMap<String,Integer>();
		keywords = Utility.getKeywords("D:\\TAC_RESULT\\keywords.txt"); 
		
		keywords_prefix = new TreeMap<String,Integer>();
		
		for (String key : keywords)
		{
			occurrenceCount.put(key, 0);
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
	
	@Override
	public void endDocument() throws SAXException {
		
		StringBuilder sb = new StringBuilder();
		for (String key : occurrenceCount.keySet())
		{
			sb.append(key).append(",").append(occurrenceCount.get(key)).append("\n");
		}
		
		Utility.writeToFile("D:\\TAC_RESULT\\occurrence.txt", sb.toString());
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;

		if (preTag.equals("text"))
			text_content = new StringBuilder(1000);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if ("text".equals(preTag)) {
			
			String content = text_content.toString();
			text_content = null;
			String[] words = content.split("\\s+");
			
//			for (int i = 0; i < words.length; i++)
//			try{	if (!Character.isLetterOrDigit(words[i].charAt(words[i].length()-1)))
//						words[i] = words[i].substring(0, words[i].length()-1);
//			}catch(StringIndexOutOfBoundsException e)
//			{
//				System.out.println(words[i]);
//			}
			int words_pos = 0;
			
			while (words_pos < words.length)
			{
				StringBuilder  str = new StringBuilder(words[words_pos++]);
				
				int offset = 0;
				while (keywords_prefix.containsKey(str.toString()))
				{
					//System.out.println(str.toString());
					if (keywords_prefix.get(str.toString()) == 1)
						incrementOccurrence(str.toString());
					if (words_pos + offset  < words.length)
						str.append(" ").append(words[words_pos + offset++]);
					else break;
				}
				
			}
			timeStamp();
		}

		preTag = null;
	}
	
	private void incrementOccurrence(String word)
	{
		Integer count = occurrenceCount.get(word);
		occurrenceCount.put(word, count+1);
//		System.out.println(word+","+occurrenceCount.get(word));
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length).toLowerCase();
			if (preTag.equals("text")) {
				text_content.append(content);
			} 
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), new WordOccurrence("D:\\TAC_RESULT\\STEM"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		String str = "AD KD ET GE";
//		int index = 0;
//		while ((index = str.indexOf(" ", index)) != -1)
//		{
//			System.out.println(str.substring(0, index));
//			index++;
//		}
		
//		Map<String,Integer> map = new TreeMap<String,Integer>();
//		map.put("A", 1);
//		Integer i = map.get("B");
//		map.put("B", i+1);
//		System.out.println(map.get("B"));
	}

	

}
