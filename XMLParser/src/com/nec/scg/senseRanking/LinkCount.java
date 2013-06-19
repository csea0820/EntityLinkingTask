package com.nec.scg.senseRanking;

import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import com.nec.scg.senseGenerator.*;
import com.nec.scg.utility.Utility;

public class LinkCount extends STEMLinking{

	protected LinkCount(String outputPath) {
		super(outputPath);
	}

	@Override
	protected void addToSETM(String source, String target) {
		Article art = getArticle(target);
		art.addSETM(source);
		art.countAllLink++;
	}

	@Override
	public void endDocument() throws SAXException {
		StringBuilder sb = new StringBuilder();
		for (String art : articles.keySet()) {
			Article article = articles.get(art);
			sb.append(art.trim()+"\t"+article.countAllLink).append("\n");
		}
		try {
			Utility.writeToFile("D:\\TAC_RESULT\\linkCount\\"+"linkCount"+".txt", sb.toString().trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String filename = "D:\\KBPÊý¾Ý¼¯\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), new LinkCount("D:\\TAC_RESULT\\LinkCount"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
