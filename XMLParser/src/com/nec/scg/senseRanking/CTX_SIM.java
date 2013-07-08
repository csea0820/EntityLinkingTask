/**
 * @Author Xiaofeng
 * @Date 2013-7-7 上午10:06:56
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
import java.util.TreeSet;

public class CTX_SIM {

	Relatedness relatedness = new Relatedness();
	Map<String, Set<String>> candidates = new TreeMap<String, Set<String>>();
	Map<String, Set<Entity>> cxt_sim_score = new TreeMap<String, Set<Entity>>();

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
//		System.out.println(candidates.get("ABC"));
		
		Top8 top8 = new Top8();
		Map<String, Set<String>> top8Terms = top8.getTop8Terms();

		top8.close();
		int index = 1;
		for (String query : candidates.keySet()) {
			Set<Entity> set = new TreeSet<Entity>();
			long startMili;
			long endMili;

			startMili = System.currentTimeMillis();
			System.out.println("query " + index);
//			System.out.println(candidates.get(query));
			for (String article : candidates.get(query)) {
				double ctx_sim = 0;
				Set<String> topTerms = top8Terms.get(article);
				
				if (topTerms != null) {
//					System.out.println(article);
//					System.out.println(topTerms);
					for (String topTerm : topTerms) {
						ctx_sim += relatedness.relatedness(query, topTerm);
					}
					set.add(new Entity(article, ctx_sim / topTerms.size()));
				} else
					set.add(new Entity(article, 0));
			}
			cxt_sim_score.put(query, set);
			
			endMili = System.currentTimeMillis();
			System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
		}

		relatedness.close();
	}

	private void readCandidates(String diretory) {

		File dir = new File(diretory);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {

				String query = file.getName().split("_")[1];
				Set<String> candidate = null;

				BufferedReader br = null;
				FileReader fr = null;
				String str;

				try {
					fr = new FileReader(file);
					br = new BufferedReader(fr);

					str = br.readLine();
					candidate = new TreeSet<String>();
					while (str != null) {
						candidate.add(str);
						str = br.readLine();
					}
					candidates.put(query, candidate);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
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
		System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
	}

}
