/**
 * @Author Xiaofeng
 * @Date 2013-7-7 ����10:06:56
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.queryparser.classic.ParseException;

import com.nec.scg.utility.Constant;
import com.nec.scg.utility.Utility;

public class CTX_SIM {

	Map<Query, Set<ArticleAttributes>> candidates = new TreeMap<Query, Set<ArticleAttributes>>();
//	Map<String, Set<ArticleAttributes>> cxt_sim_score = new TreeMap<String, Set<ArticleAttributes>>();

	
	class Entity implements Comparable<Entity> {
		String articleName;
		double CTX_SIM;

		public Entity(String articleName) {
			this.articleName = articleName;
		}

		public Entity(String articleName, double cTX_SIM) {
			super();
			this.articleName = articleName;
			CTX_SIM = cTX_SIM;
		}

		public void setArticleName(String articleName) {
			this.articleName = articleName;
		}

		public void setCTX_SIM(double cTX_SIM) {
			CTX_SIM = cTX_SIM;
		}

		@Override
		public int compareTo(Entity o) {
			if (o.CTX_SIM - CTX_SIM > 0)
				return 1;
			else
				return -1;
		}
	}

	public void calc_CTX_SIM() throws IOException {
		readCandidates("D:\\TAC_RESULT\\TOTAL");
		// System.out.println(candidates.get("ABC"));

		Top8 top8 = new Top8();
//		Map<String, Set<String>> top8Terms = top8.getTop8Terms();

		int index = 1;
		for (Query query : QueryFactory.getQueryInfo(Constant.queryXmlFile)) {

			System.out.println("query " + index++);
			Set<String> keyword_query = getTopTermFromQuery(query);
//			Set<String> keyword_query = top8.getQueryDocTopTerms(query);
			keyword_query.add(query.getQuery());
			// System.out.println(candidates.get(query));
			for (ArticleAttributes article : candidates.get(query)) {
				double ctx_sim = 0;
				Set<String> topTerms = top8.getTopTerms(article.getName().toLowerCase());
				topTerms.add(article.getName());
//				Set<String> topTerms = top8Terms.get(article.getName().toLowerCase());
				if (topTerms != null) {
					int size = 0;
					for (String topTerm : topTerms) {
						try {
//							if (topTerm.contains("<") || topTerm.contains(">")
//									|| topTerm.length() > 20 || size > 8)
//								continue;
//							ctx_sim += Relatedness.getInstance().relatedness(query.query, topTerm);
//							size++;
							for (String kq : keyword_query)
							{
								ctx_sim += Relatedness.getInstance().relatedness(kq, topTerm);
								size++;
							}
							
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					if (size != 0)
						article.setCtx_sim(ctx_sim / size);
					article.setCtx_ct(size);
					article.setCtx_wt(ctx_sim);
				}
			}
		}

		writeToFile();
		Relatedness.getInstance().close();
		top8.close();
	}

	private void writeToFile() {

		for (Query query : candidates.keySet()) {
			StringBuilder sb = new StringBuilder();
			for (ArticleAttributes e : candidates.get(query))
				sb.append(e.name).append("\t").append(e.ctx_sim).append("\t").append(e.ctx_wt).
				append("\t").append(e.ctx_ct).append("\t").append(e.substr_test).append("\t").append(e.editDistance_test).append("\n");
			Utility.writeToFile("D:\\TAC_RESULT\\cxt_sim\\" + query.id + "_" + query.query + ".txt",
					sb.toString());
		}

	}
	
	private Set<String> getTopTermFromQuery(Query query){
		Set<String> res = DocumentKeyword.getinstance().getKeywords(query);
		if (res.size() < 4)return res;
		
		Set<String> ret = new TreeSet<String>();
		for (String v : res){
			if (v.length() >= 5)
				ret.add(v);
			if (ret.size() > 3)break;
		}
		
		return res;
	}

	private void readCandidates(String diretory) {

		File dir = new File(diretory);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {

				String[] queryInfo = file.getName().split("_");
				String query = queryInfo[1];

				int id = Integer.parseInt(queryInfo[3].substring(0, queryInfo[3].length()-4));
				Set<ArticleAttributes> candidate = null;

//				if (candidates.containsKey(query))
//					continue;

				BufferedReader br = null;
				FileReader fr = null;
				String str;

				try {
					fr = new FileReader(file);
					br = new BufferedReader(fr);

					str = br.readLine();
					candidate = new TreeSet<ArticleAttributes>();
					while (str != null) {
						String[] contents = str.split("\t");
						if (contents.length == 3)
							candidate.add(new ArticleAttributes(contents[0], Boolean.parseBoolean(contents[2]), Boolean.parseBoolean(contents[1])));
						str = br.readLine();
					}
					candidates.put(new Query(query,id), candidate);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						fr.close();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startMili;
		long endMili;

		startMili = System.currentTimeMillis();
		CTX_SIM ctx = new CTX_SIM();
		try {
			ctx.calc_CTX_SIM();
		} catch (IOException e) {
			e.printStackTrace();
		}
		endMili = System.currentTimeMillis();
		System.out.println("�ܺ�ʱΪ��" + (endMili - startMili) + "����");
	}

}
