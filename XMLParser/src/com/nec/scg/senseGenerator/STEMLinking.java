package com.nec.scg.senseGenerator;

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

public class STEMLinking extends AbstractLinking {

	protected Map<String, Article> articles = new TreeMap<String, Article>(); // <Silver
																				// Bauhinia
																				// Star,set<SBS，...>>
	// private final String regular_all_SETM = "\\s+\\[\\[[^\\[\\[:]+\\]\\]"; //
	// 匹配所有以[[开始，中间内容不包含:，且以]]结尾的字符串
	private final String regular_partial_SETM = "\\s+\\[\\[[^\\[+:=\\{\\}\\]]+\\|[^\\]+]+\\]\\]"; // 匹配所有以[[开始，中间内容不包含:，同时以|分隔，最后以]]结尾的字符串
	Pattern pattern_STEM = null;

	protected StringBuilder text_content = null;

	class LinkCount {
		String article;
		int linkCnt;

		public LinkCount(String article, int linkCnt) {
			this.article = article;
			this.linkCnt = linkCnt;
		}
	}

	protected STEMLinking(String outputPath) {
		super(outputPath);
	}

	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数

		pattern_STEM = Pattern.compile(regular_partial_SETM);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;

		if (preTag.equals("text"))
			text_content = new StringBuilder(1000);
	}

	protected Article getArticle(String articleName) {
		Article art = articles.get(articleName);
		if (art == null) {
			art = new Article();
			articles.put(articleName, art);
		}

		return art;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if ("text".equals(preTag)) {
			parseText();
			timeStamp();
		}

		preTag = null;
	}

	private void parseText() {
		Matcher matcher = null;
		String content = text_content.toString().trim();
		text_content.delete(0, text_content.length());
		text_content = null;

		matcher = pattern_STEM.matcher(content);

		while (matcher.find()) {
			resolveHyperLinks(matcher.group().trim());
		}

	}

	@Override
	public void endDocument() throws SAXException {

		endMili = System.currentTimeMillis();
		System.out.println("生成候选者耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();

		Map<String, Set<LinkCount>> map = new TreeMap<String, Set<LinkCount>>();

		for (String article : articles.keySet()) {
			Article art = articles.get(article);

			Map<String, Integer> sourceStatistic = art.getSourceStatistic();
			for (String source : sourceStatistic.keySet()) {
				Set<LinkCount> set = map.get(source);
				if (set == null) {
					set = new TreeSet<LinkCount>();
				}
				set.add(new LinkCount(art.articleName, sourceStatistic
						.get(source)));
			}
		}

		for (String source : map.keySet()) {
			int totalCnt = 0;
			for (LinkCount lc : map.get(source)) {
					totalCnt += lc.linkCnt;
			}
			
			for (LinkCount lc : map.get(source))
			{
				articles.get(lc.article).setLink_prob(lc.linkCnt*1.0/totalCnt);
			}
		}

		queryTask("linkresult");

		endMili = System.currentTimeMillis();
		System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length);
			if (preTag.equals("text")) {
				text_content.append(content);
			}
		}
	}

	private void resolveHyperLinks(String anchor) {
		String[] anchor_contents = anchor.substring(2, anchor.length() - 2)
				.split("\\|"); // 去除首尾的[[和]]字符，并将|左右字符分隔出来

		if (anchor_contents.length < 2)
			return;
		addToSETM(anchor_contents[1], anchor_contents[0]);
	}

	protected void addToSETM(String source, String target) {
		Article art = getArticle(target);
		art.addSETM(source);
		art.addSource(source);

	}

	@Override
	protected Set<String> senseGenerator(String query) {
		Set<String> candidates = new TreeSet<String>();
		String q = query.toLowerCase();
		for (String art : articles.keySet()) {
			Article article = articles.get(art);
			// article.setArticleName(art);
			if (article.match(q))
				candidates.add(art);
		}
		return candidates;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), new STEMLinking(
					"D:\\TAC_RESULT\\STEM"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Pattern pattern =
		// Pattern.compile("\\{\\{[Dd]isambig(uation){0,1}\\|{0,1}.*\\}\\}");
		// Matcher matcher =
		// pattern.matcher(" {{disambiguation}} ");
		// while (matcher.find())
		// {
		// System.out.println(matcher.group());
		// }

		// if ("Michael LeMoyne Kennedy".contains("Michael Kennedy")) {
		// System.out.println("YES");
		// } else
		// System.out.println("NO");

		//
		// String str = "Apache (novel)";
		// for (String s : str.split("\\|"))
		// System.out.println(s+"h");
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

	@Override
	protected void setOutputPrefixName() {
		this.outputPrefixName = "STEM";
	}

}
