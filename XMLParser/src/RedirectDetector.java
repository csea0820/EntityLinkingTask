import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RedirectDetector extends DefaultHandler {

	int index = 0;
	String preTag = null;

	long startMili;
	long endMili;

	Map<String, Set<String>> redirect = new TreeMap<String, Set<String>>();

	String title = null;
	String redirectName = null;

	RedirectDetector() {
	}

	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数
	}

	@Override
	public void endDocument() throws SAXException {

		for (String key : redirect.keySet()) {
			Set<String> set = redirect.get(key);
			if (set.size() >= 2) {
				System.out.print("article:" + key + " redirect:");
				for (String v : set)
					System.out.print(v + " ");
			}
		}

		endMili = System.currentTimeMillis();
		System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("redirect")) {
			redirectName = attributes.getValue(0);
			Set<String> set = redirect.get(title);
			if (set == null) {
				redirect.put(title, set = new TreeSet<String>());
			}
			set.add(redirectName);
		}
		preTag = qName;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		preTag = null;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length).trim();
			if (preTag.equals("title")) {

				title = content;

			}
		}
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
			saxParser.parse(new File(filename), new RedirectDetector());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
