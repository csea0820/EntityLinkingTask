
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


public class FirstTable extends DefaultHandler {

	int index = 0;
	String preTag = null;

	long startMili;
	long endMili;

/*	private ArticlePage articlePage;
	private List<ArticlePage> articlePages;*/
	
	private String titleName;
	private String normalizedTitleName = null;
	private String redirectName = null;
	private Map<String,Set<String>> NAR;

	FirstTable() {
	}

	/*public List<ArticlePage> getList() {
		return articlePages;
	}*/
	
	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数
		//articlePages = new ArrayList<ArticlePage>();
		NAR = new TreeMap<String,Set<String>>();
		
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
		if (qName.equals("redirect")) {
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
			redirectName = null;
		}
		preTag = qName;

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("text"))
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

	
	private Set<String> query(String query)
	{
		Set<String> candidates = new TreeSet<String>();
		String q = query.toLowerCase();
		for (String nor : NAR.keySet())
		{
			if (nor.equals(q) || nor.contains(q))
			{
				for (String ar: NAR.get(nor))
					candidates.add(ar);
			}
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

			if (elr.getExpectedResult().get(i).equals("NIL"))
			{
				em.returnedRelevantPagesPlus();
//				continue;
			}
				
			
			candidates = query(elr.getQueries().get(i));
			em.allReturnedPagesPlus(candidates.size());
			if (candidates.contains(elr.getExpectedResult().get(i)))
				em.returnedRelevantPagesPlus();
			
			StringBuilder sb = new StringBuilder();
			for (String can: candidates)
				sb.append(can).append("\n");
			
			Utility.writeToFile("D:\\TAC_RESULT\\NAR\\NAR_"+elr.getQueries().get(i)+"_"+elr.getExpectedResult().get(i)+"_"+i+".txt", sb.toString());
			
			candidates = null;
		}
		
		System.out.println("Recall="+em.recall()+",Precision="+em.precision());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String filename ="C:\\chenxiulongdata\\enwiki-latest-pages-articles.xml";
		String filename = "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		FirstTable firsttable = new FirstTable();
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
