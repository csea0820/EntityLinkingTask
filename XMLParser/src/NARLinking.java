
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class NARLinking extends AbstractLinking {

	

	
	private String titleName;
	private String normalizedTitleName = null;
	private String redirectName = null;
	private Map<String,Set<String>> NAR;

	boolean ignoreTitle = false;
	
	NARLinking(String outputPath) {
		super(outputPath);
	}
	
	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数
		NAR = new TreeMap<String,Set<String>>();
		
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("redirect") && ignoreTitle == false) {
			redirectName = attributes.getValue(0);
			
			Set<String> set = NAR.get(normalizedTitleName);
			if (set == null)
			{
				set = new TreeSet<String>();
				NAR.put(normalizedTitleName, set);
			}
			set.add(redirectName);
		}
		else if (qName.equals("text"))
		{
			ignoreTitle = true;
			redirectName = null;
		}
		preTag = qName;

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("text") && ignoreTitle == false)
		{
			if (redirectName == null)
			{
				Set<String> set = NAR.get(normalizedTitleName);
				if (set == null)
				{
					set = new TreeSet<String>();
					NAR.put(normalizedTitleName, set);
				}
				set.add(titleName);
			}
		}
		preTag = null;

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if ("title".equals(preTag))
		{
			{
				String content = new String(ch, start, length);

				titleName = content;
				ignoreTitle = ignoreTitle(titleName);
				/*
				 * normalizedName 通过调研发现title的（）目前只出现在句尾。所以去（）标签同时转化为小写
				 */
				if (content.contains("(")) {
					normalizedTitleName = content.substring(0,content.indexOf("(")).toLowerCase();
				} 
				else {
					normalizedTitleName = content.toLowerCase();
				}
				
				if (++index % 10000 == 0) {
					endMili = System.currentTimeMillis();
					System.out.println(index+","+(endMili - startMili) / 1000);
					System.gc();
				}
			}
		}
	}

	
	
	private String getShortName(String name)
	{
		String result = "";
		for (String s : name.split("\\s"))
		{
			if (!"of".equals(s) && s.length() > 0)
				result += s.charAt(0);
		}
		return result;
	}
	
	private boolean ignoreTitle(String str)
	{
		if (str.contains("[") || str.contains("]") || str.contains("{") || str.contains("}") 
				|| str.contains("=") || str.contains(":") || str.contains("'") || str.contains("\"")
				|| str.trim().equals(""))
			return true;
		return false;

	}
	
	@Override
	protected Set<String> senseGenerator(String query) {
		Set<String> candidates = new TreeSet<String>();
		String q = query.toLowerCase();
		for (String nor : NAR.keySet())
		{
			if (nor.equals(q) || nor.contains(q) || q.contains(nor))
			{
				for (String ar: NAR.get(nor))
					candidates.add(ar);
			}
			else if (!nor.trim().equals(""))
			{
				String sn = getShortName(nor);
				if (sn.equals(q) || sn.contains(q) || q.contains(sn))
					candidates.addAll(NAR.get(nor));
			}
		}	
		return candidates;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String filename ="C:\\chenxiulongdata\\enwiki-latest-pages-articles.xml";
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		NARLinking firsttable = new NARLinking("D:\\TAC_RESULT\\NAR");
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), firsttable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * 遍历输出
		 */
		
	}

	

}
