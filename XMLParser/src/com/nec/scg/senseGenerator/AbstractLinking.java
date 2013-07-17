package com.nec.scg.senseGenerator;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nec.scg.senseRanking.ArticleAttributes;
import com.nec.scg.utility.Utility;

/**
 * @Author Xiaofeng
 * @Date 2013-6-5 下午5:02:54
 */

public abstract class AbstractLinking extends DefaultHandler {
	protected int index = 0;
	protected String preTag = null;

	protected long startMili;
	long endMili;

	protected String outputPath = null;
	Map<String, Set<ArticleAttributes>> resultCache = new TreeMap<String, Set<ArticleAttributes>>();
	protected String outputPrefixName = null;

	protected AbstractLinking(String outputPath) {
		this.outputPath = outputPath;
		setOutputPrefixName();
	}

	protected Set<ArticleAttributes> query(String query) {
		
		Set<ArticleAttributes> cache = resultCache.get(query);
		if (cache != null) {
			return cache;
		}

		Set<ArticleAttributes> candidates = senseGenerator(query);

		resultCache.put(query, candidates);
		return candidates;
	}

	abstract protected Set<ArticleAttributes> senseGenerator(String query);

	abstract protected void setOutputPrefixName();

	public void queryTask(String filePath) {
		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult(filePath);

		EvaluationMetric em = new EvaluationMetric();

		int querySize = elr.getQueries().size();
		em.allRelevantPagesPlus(querySize);
		Set<ArticleAttributes> candidates = null;
		for (int i = 0; i < querySize; i++) {

			if (elr.getExpectedResult().get(i).equals("NIL")) {
				em.returnedRelevantPagesPlus();
				// continue;
			}

			candidates = query(elr.getQueries().get(i).toLowerCase());
			em.allReturnedPagesPlus(candidates.size());
//			if (candidates.contains(elr.getExpectedResult().get(i)))
//				em.returnedRelevantPagesPlus();

			StringBuilder sb = new StringBuilder();
			for (ArticleAttributes can : candidates)
				sb.append(can.getName()).append("\t").append(can.isSubstr_test()).append("\t").append(can.isEditDistance_test()).append("\n");

			Utility.writeToFile(outputPath + "\\" + outputPrefixName + "_"
					+ elr.getQueries().get(i) + "_"
					+ elr.getExpectedResult().get(i) + "_" + i + ".txt",
					sb.toString());

		}

		System.out.println("Recall=" + em.recall() + ",Precision="
				+ em.precision());
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

	protected void timeStamp() {
	
		if (++index % 10000 == 0)
		{
			endMili = System.currentTimeMillis();
			System.out.println(index + "," + (endMili - startMili) / 1000);
			System.gc();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
}
