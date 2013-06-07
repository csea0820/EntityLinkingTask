import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DPLinking extends AbstractLinking {

	DPLinking(String outputPath) {
		super(outputPath);
	}

	Map<String, Article> articles = new TreeMap<String, Article>();

	String referencedArticle = null;

	private final String regular_all_DP = "\\*+\\s*\\[\\[[^\\[+]+\\]\\]"; // 匹配以*开始，中间内容包含anchor的字符串。(用于消歧页)
	private final String dpIndicator = "\\{\\{[Dd]isambig(uation){0,1}\\|{0,1}.*\\}\\}";
		
	Pattern pattern_DP = null;
	Pattern pattern_DP_PAGE = null;

	StringBuilder text_content = null;

	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数

		pattern_DP = Pattern.compile(regular_all_DP);
		pattern_DP_PAGE = Pattern.compile(dpIndicator);
		
	}


	@Override
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
			parseText();
			timeStamp();
			referencedArticle = null;
		}
		
		preTag = null;
	}
	
	private void parseText()
	{
		Matcher matcher = null;
		String content = text_content.toString().trim();
		text_content.delete(0, text_content.length());
		text_content = null;
		matcher = pattern_DP_PAGE.matcher(content);
		if (matcher.find()){

			String titleName = null;
			int dpFlagIndex = referencedArticle.indexOf("(");  //referencedArticle为page name(如JC）
			if (dpFlagIndex != -1)
				titleName = referencedArticle.substring(0, dpFlagIndex).trim();
			else titleName = referencedArticle;
			
			matcher = pattern_DP.matcher(content);
			String dp_content = null;
			String dp_content_tmp = null;
			String[] dp_contents = null;
			Article art = null;
			while (matcher.find()) {
				dp_content_tmp = matcher.group().trim();
				dp_content = dp_content_tmp
						.substring(dp_content_tmp.indexOf("[[") + 2,dp_content_tmp.indexOf("]]"));
				dp_contents = dp_content.split("\\|");

				art = getArticle(dp_contents[0]);
				art.addEAB(titleName);
			}
		} 
	}
	
	private Article getArticle(String articleName)
	{
		Article art = articles.get(articleName);
		if (art == null) {
			art = new Article(); 
			articles.put(articleName, art);
		}
		
		return art;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length);				
			if (preTag.equals("text")) {	
				text_content.append(content);
			} else if (preTag.equals("title")) {
				referencedArticle = content;
			}
		}
	}
	
	@Override
	protected Set<String> senseGenerator(String query) {
		Set<String> candidates = new TreeSet<String>();
		String q = query.toLowerCase();
		for (String art : articles.keySet())
		{
			Article article = articles.get(art);
			if (article.match(q))
				candidates.add(art);
		}
		return candidates;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// String filename = "D:\\KBP数据集\\test3.xml";
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), new DPLinking("D:\\TAC_RESULT\\DP"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void setOutputPrefixName() {
		this.outputPrefixName = "DP";
	}

	

}
