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
import org.xml.sax.helpers.DefaultHandler;

public class EntityLinking extends DefaultHandler {

	int index = 0;
	String preTag = null;

	long startMili;
	long endMili;

	Map<String, Article> articles = new TreeMap<String, Article>();

	String referencedArticle = null;

	private final String regular_all_SETM = "\\s+\\[\\[[^\\[\\[:]+\\]\\]"; // 匹配所有以[[开始，中间内容不包含:，且以]]结尾的字符串
	private final String regular_partial_SETM = "\\s+\\[\\[[^\\[+:]+\\|[^\\]+]+\\]\\]"; // 匹配所有以[[开始，中间内容不包含:，同时以|分隔，最后以]]结尾的字符串
	private final String regular_all_DP = "\\*+\\s+\\[\\[[^\\[+]+\\]\\]"; // 匹配以*开始，中间内容包含anchor的字符串。(用于消歧页)
	private final String dpIndicator = "{{disambiguation}}";

	Pattern pattern_SETM = null;
	Pattern pattern_DP = null;

	StringBuilder text_content = null;
	
//	int count_key = 0;
//	int count_value = 0;
	
	EntityLinking() {
	}

	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数

		pattern_SETM = Pattern.compile(regular_partial_SETM);
		pattern_DP = Pattern.compile(regular_all_DP);
	}

	@Override
	public void endDocument() throws SAXException {

		
		endMili = System.currentTimeMillis();
		System.out.println("生成候选者耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();
		
		queryTask("linkresult");
		
		endMili = System.currentTimeMillis();
		System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;

		if (preTag.equals("text"))
			text_content = new StringBuilder(1000);
		else if (qName.equals("redirect")) {
			
			Article art = getArticle(attributes.getValue(0));
			art.addRedirectName(referencedArticle);
		}
	}

	private Set<String> query(String query)
	{
		Set<String> candidates = new TreeSet<String>();
		for (String art : articles.keySet())
		{
			Article article = articles.get(art);
			//article.setArticleName(art);
			if (article.match(query))
				candidates.add(art);
		}
		
		
		return candidates;
	}
	
	public void queryTask(String filePath)
	{
		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult(filePath);
		
		EvaluationMetric em = new EvaluationMetric();
		
		int querySize = elr.getQueries().size();
		em.allRelevantPagesPlus(querySize);
		Set<String> candidates = null;
		for (int i = 0; i < querySize; i++)
		{
			System.out.println("Query " + (i+1) + " Done!");

			if (elr.getExpectedResult().get(i).equals("NIL"))
			{
				em.returnedRelevantPagesPlus();
				continue;
			}
				
			
			candidates = query(elr.getQueries().get(i));
			em.allReturnedPagesPlus(candidates.size());
			if (candidates.contains(elr.getExpectedResult().get(i)))
				em.returnedRelevantPagesPlus();
			
			candidates = null;
		}
		
		System.out.println("Recall="+em.recall()+",Precision="+em.precision());
	}
	
	private Article getArticle(String articleName)
	{
		Article art = articles.get(articleName);
		if (art == null) {
			art = new Article(); 
			articles.put(articleName, art);
			//art.setNormalizedName(normalizedName(articleName));
		}
		
		return art;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if ("text".equals(preTag)) {
			parseText();
			index++;
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
		if (content.endsWith(dpIndicator)) {

			String titleName = null;
			int dpFlagIndex = referencedArticle.indexOf("(");
			if (dpFlagIndex != -1)
				titleName = referencedArticle.substring(0, dpFlagIndex).trim();
			else titleName = referencedArticle;
//			String titleName = referencedArticle.replace(
//					"(disambiguation)", "").trim();

//			Article titleArt = getArticle(referencedArticle);
			
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

				art = getArticle(new String(dp_contents[0].toCharArray()));
//				if (!titleName.equals(dp_contents[0]))
				art.addEAB(titleName);
//				titleArt.addDP(dp_contents[0]);
				
//				if (dp_contents.length == 2)
//					addToSETM(dp_contents[1], dp_contents[0]);
//				dp_content_tmp = null;
//				dp_content = null;
//				dp_contents = null;
			}
		} else
		{
			matcher = pattern_SETM.matcher(content);

			while (matcher.find()) {
				resolveHyperLinks(matcher.group().trim());
			}
		}

		
		matcher = null;
	
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
	
	
	private void timeStamp() {
		if (index % 10000 == 0) {
			endMili = System.currentTimeMillis();
			System.out.println(index+","+(endMili - startMili) / 1000);
			System.gc();
		}
	}

	private void resolveHyperLinks(String anchor) {
		String[] anchor_contents = anchor.substring(2,
				anchor.length() - 2).split("\\|"); // 去除首尾的[[和]]字符，并将|左右字符分隔出来

		if (anchor_contents.length < 2)
			return;
		addToSETM(new String(anchor_contents[1].toCharArray()), new String(anchor_contents[0].toCharArray()));
	}

	private void addToSETM(String source, String target) {
		Article art = getArticle(target);
		art.addSETM(source);
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
			saxParser.parse(new File(filename), new EntityLinking());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Pattern pattern = Pattern.compile("\\*+\\s+\\[\\[[^\\[+]+\\]\\]");
		// Matcher matcher =
		// pattern.matcher("* [[Apache Group (disambiguation)]]\n* [[Fort Apache (disambiguation)]]");
		// while (matcher.find())
		// {
		// System.out.println(matcher.group());
		// }
		//
		//		
//		 String str = "Apache (novel)";
//		 for (String s : str.split("\\|"))
//		 System.out.println(s+"h");
		//
		// for (String s:split("dfdf|fdfdfa"))
		// System.out.println(s);

		// Map<Article,Set<String>> SETM = new TreeMap<Article,Set<String>>();
		//
		// Article a = new Article();
		// a.setArticleName("A");
		// a.setId(1);
		//
		// SETM.put(a, new TreeSet<String>());
		//
		// Article b = new Article();
		// b.setArticleName("A");
		//
		//
		// if (SETM.containsKey(b))
		// System.out.println("YES");
		// else System.out.println("NO");
		//
		//
		// Set<Article> set = new TreeSet<Article>();
		// set.add(a);
		//
		// if (set.contains(b))
		// System.out.println("YES_SET");
		// else System.out.println("NO_SET");

		// Matcher m = Pattern.compile("\\w+"
		// )
		// .matcher("Evening is full of the linnet's wings"
		// );
		// while
		// (m.find())
		// System.out.println(m.group());
	}

}
