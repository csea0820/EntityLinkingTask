/**
 * @Author Xiaofeng
 * @Date 2013-7-4 上午9:14:51
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.nec.scg.utility.Utility;

public class Linkability {

	class Entity {
		int linkCnt;
		int occurrenceCnt;

		public Entity() {
			linkCnt = 0;
			occurrenceCnt = 0;
		}

		public Entity(int linkCnt, int occurrenceCnt) {
			this.linkCnt = linkCnt;
			this.occurrenceCnt = occurrenceCnt;
		}

		public void setLinkCnt(int linkCnt) {
			this.linkCnt = linkCnt;
		}

		public void setOccurrenceCnt(int occurrenceCnt) {
			this.occurrenceCnt = occurrenceCnt;
		}
	}

	// 关键词->链接及出现统计
	Map<String, Entity> linkability = null;

	// 关键词可链接数
	Map<String, Integer> links = null;

	public Linkability() {
		linkability = new TreeMap<String, Entity>();
		links = new TreeMap<String, Integer>();
	}

	public void calcLinkability() {

		// initialize occurrence count
		readOccurrence("D:\\TAC_RESULT\\occurrence.txt");
		readLinks("D:\\TAC_RESULT\\links.txt");
		// get link count
		// String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		// SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			// ArticleKeywordGenerator ag = null;
			// SAXParser saxParser = spf.newSAXParser();
			// saxParser.parse(new File(filename), ag = new
			// ArticleKeywordGenerator(""));
			// Map<String,Set<String>> links = ag.getLinks();

			for (String key : links.keySet()) {
				if (linkability.containsKey(key)) {
					linkability.get(key).linkCnt = links.get(key);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeResult() {
		StringBuilder sb = new StringBuilder();
		sb.append("#word,linkCount,occurrenceCount\n");
		for (String key : linkability.keySet()) {
			Entity e = linkability.get(key);
			sb.append(key).append(",").append(e.linkCnt).append(",")
					.append(e.linkCnt + e.occurrenceCnt).append("\n");
		}

		Utility.writeToFile("D:\\TAC_RESULT\\linkability.txt", sb.toString());
	}

	private void readOccurrence(String path) {
		File file = new File(path);
		if (file.exists()) {
			BufferedReader br = null;
			FileReader fr = null;
			String str;

			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				str = br.readLine();

				while (str != null) {
					String[] content = str.split(",");
					if (content.length == 2)
						linkability.put(content[0],
								new Entity(0, Integer.parseInt(content[1])));
					str = br.readLine();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void readLinks(String path) {
		File file = new File(path);
		if (file.exists()) {
			BufferedReader br = null;
			FileReader fr = null;
			String str;

			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				br.readLine();
				str = br.readLine().trim();

				while (str != null) {
					if (!str.equals("")) {
						String[] content = str.split(",");
						if (content.length == 2) {
							int value = 0;
							try {
								value = Integer.parseInt(content[1]);
							} catch (NumberFormatException n) {
								str = br.readLine();
								continue;
							}
							links.put(content[0].trim().toLowerCase(), value);
						}
					}
					str = br.readLine();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Linkability link = new Linkability();
		link.calcLinkability();
		link.writeResult();
	}

}
