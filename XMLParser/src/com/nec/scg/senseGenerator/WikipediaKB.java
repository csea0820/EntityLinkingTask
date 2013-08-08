/**
 * @Author Xiaofeng
 * @Date 2013-8-6 下午3:25:10
 */
package com.nec.scg.senseGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * 生成KB中所有页面的wikipedia page
 */
public class WikipediaKB extends DefaultHandler {

	List<WikiPage> pages = new ArrayList<WikiPage>();

	WikiPage curPage = null;

	String preTag = null;
	int count = 0;

	Set<String> kbNames = null;
	boolean parse = false;

	private String FILE_PATH = "D:\\TAC_RESULT\\enwiki-kb-articles.xml";

	class WikiPage {
		String title;
		String redirect;
		StringBuilder text;

		public WikiPage() {
			text = new StringBuilder(1000);
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<page>\n");
			sb.append("<title>").append(title).append("</title>\n");
			if (redirect != null)
				sb.append("<redirect title=\"").append(redirect)
						.append("\" />\n");

			sb.append("<text>").append(text.toString()).append("</text>\n");
			sb.append("</page>\n");
			return sb.toString();
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if ("text".equals(preTag) && curPage != null) {
			
			pages.add(curPage);
			curPage = null;
//			if (count >= 130100)return;
			
			if (count % 10000 == 0) {

				StringBuilder sb = new StringBuilder();
				for (WikiPage wp : pages) {
					sb.append(wp);
				}

				appendContentToXML(sb.toString());

				pages = new ArrayList<WikiPage>();
				System.out.println(count);

			}
		}

		preTag = null;
	}

	private void appendContentToXML(String content) {

		// FileWriter pw = null;
		try {
			// pw = new FileWriter(FILE_PATH,true);
			// pw.write(content);

			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(FILE_PATH,true), "UTF-8");
			out.write(content);
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;

		if (qName.equals("redirect") && curPage != null) {
			curPage.redirect = attributes.getValue(0).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
		}
	}

	@Override
	public void startDocument() throws SAXException {

		KnowledgeBase kb = new KnowledgeBase();
		kbNames = kb.getKbArticleNames();

		appendContentToXML("<?xml version=\"1.0\" encoding=\"UTF8\"?>\n<root>\n");
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String content = new String(ch, start, length);
		if ("title".equals(preTag)) {
			if (kbNames.contains(content)) {
				count++;
//				if (count < 120000)return;
				curPage = new WikiPage();
				curPage.title = content.replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
			}
		} else if ("text".equals(preTag)) {
			if (curPage != null)
				curPage.text.append(content.replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
		}
	}

	@Override
	public void endDocument() throws SAXException {
		StringBuilder sb = new StringBuilder();
		for (WikiPage wp : pages) {
			sb.append(wp);
		}
		sb.append("</root>");
		appendContentToXML(sb.toString());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		WikipediaKB firsttable = new WikipediaKB();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), firsttable);
			// firsttable.info();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
