/**
 * @Author Xiaofeng
 * @Date 2013-7-13 下午3:54:02
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.nec.scg.senseGenerator.EvaluationMetric;
import com.nec.scg.senseGenerator.ExpectedLinkResult;
import com.nec.scg.utility.Utility;

public class LinkComboRanking {

	Map<Query, Set<ArticleAttributes>> candidates = new TreeMap<Query, Set<ArticleAttributes>>();
	Map<String, Set<ArticleAttributes>> link_prob = new TreeMap<String, Set<ArticleAttributes>>();
	Map<Query, Set<ArticleAttributes>> text_sim = new TreeMap<Query, Set<ArticleAttributes>>();

	public void linkComboRanking() {
		readCandidatesCtxSim("D:\\TAC_RESULT\\cxt_sim");
		readCandidatesLinkProb("D:\\TAC_RESULT\\link_prob");
		readCandidatesTextSim("D:\\TAC_RESULT\\textSim");

		for (String source : link_prob.keySet()) {

			for (Query query : candidates.keySet()) {

				if (query.query.equals(source)) {
					Set<ArticleAttributes> articles = candidates.get(query);

					if (articles != null) {
						Set<ArticleAttributes> set = link_prob.get(source);

						for (ArticleAttributes art : articles) {
							for (ArticleAttributes aa : set) {
								if (art.name.equals(aa.name)) {
									art.setLink_prob(aa.getLink_prob());
									break;
								}
							}
							art.calLink_combo();
						}
					}
				}
			}
		}

		for (Query query1 : text_sim.keySet()) {
			for (Query query2 : candidates.keySet()) {
				if (query2.equals(query1)) {
					Set<ArticleAttributes> articles = candidates.get(query2);
					for (ArticleAttributes art : text_sim.get(query1)) {
						for (ArticleAttributes aa : articles) {
							if (aa.name.equals(art.name)) {
								aa.setText_sim(art.getText_sim());
								aa.calLink_combo();
								break;
							}
						}
					}
				}
			}
		}

		evaluate();

		writeArticlesTofile();

	}

	private void evaluate() {
		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult("linkresult");

		EvaluationMetric em = new EvaluationMetric();

		int querySize = elr.getQueries().size();
		em.allRelevantPagesPlus(querySize);

		int cnt = 0;
		for (Query query : candidates.keySet()) {

			double maxScore = 0.0;
			ArticleAttributes target = null;

			Set<ArticleAttributes> temp_art = new TreeSet<ArticleAttributes>(); // 按link_combo字段排序
			for (ArticleAttributes art : candidates.get(query))
				temp_art.add(art);

			for (ArticleAttributes art : temp_art) {
				target = art;
				maxScore = target.link_combo;
				break;
			}
			if ((maxScore < 0.7 && elr.getExpectedResult(query.id)
					.equals("NIL"))
					|| (target != null && target.name.equals(elr
							.getExpectedResult(query.id)))) {
				em.returnedRelevantPagesPlus();
				if ((target != null && target.name.equals(elr
						.getExpectedResult(query.id)))) {
					cnt++;
					// System.out.println(query+","+target);
				}
			}
		}
		System.out.println(em.getM_returned_relevant_pages());
		System.out.println("Precision = " + em.getM_returned_relevant_pages()
				* 1.0 / em.getM_all_relevant_pages());
		System.out.println("Match Count = " + cnt);

	}

	private void writeArticlesTofile() {
		for (Query query : candidates.keySet()) {
			StringBuilder sb = new StringBuilder();
			Set<ArticleAttributes> article = candidates.get(query);
			Set<ArticleAttributes> temp_art = new TreeSet<ArticleAttributes>(); // 按link_combo字段排序
			for (ArticleAttributes art : article)
				temp_art.add(art);
			for (ArticleAttributes art : temp_art)
				sb.append(art);
			Utility.writeToFile("D:\\TAC_RESULT\\linkComboRanking\\" + query
					+ ".txt", sb.toString());
		}
	}

	private void readCandidatesTextSim(String directory) {
		File dir = new File(directory);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String query = file.getName().split("_")[1];
				String id = file.getName().split("_")[0];
				
				Set<ArticleAttributes> result = new TreeSet<ArticleAttributes>();
				BufferedReader br = null;
				FileReader fr = null;
				String str;
				// 合并前将结果再做一次过滤，将有{，{，=，：之类的行都去掉
				try {
					fr = new FileReader(file);
					br = new BufferedReader(fr);

					str = br.readLine();
					while (str != null) {
						String[] contents = str.split("\t");

						ArticleAttributes articleAttr = new ArticleAttributes(
								contents[0].replaceAll("_", " "));
						articleAttr
								.setText_sim(Double.parseDouble(contents[1]));
						result.add(articleAttr);
						str = br.readLine();
					}

					text_sim.put(new Query(query,Integer.parseInt(id)), result);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
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

	private void readCandidatesLinkProb(String directory) {
		File dir = new File(directory);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String source = file.getName().substring(0,
						file.getName().length() - 4);

				Set<ArticleAttributes> result = new TreeSet<ArticleAttributes>();
				BufferedReader br = null;
				FileReader fr = null;
				String str;
				// 合并前将结果再做一次过滤，将有{，{，=，：之类的行都去掉
				try {
					fr = new FileReader(file);
					br = new BufferedReader(fr);

					str = br.readLine();
					while (str != null) {
						String[] contents = str.split("\t");

						ArticleAttributes articleAttr = new ArticleAttributes(
								contents[0]);
						articleAttr.setLink_prob(Double
								.parseDouble(contents[1]));
						result.add(articleAttr);
						str = br.readLine();
					}

					link_prob.put(source, result);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
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

	private void readCandidatesCtxSim(String directory) {
		File dir = new File(directory);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String[] queryInfo = file.getName()
						.substring(0, file.getName().length() - 4).split("_");
				String query = queryInfo[1];
				int id = Integer.parseInt(queryInfo[0]);

				Set<ArticleAttributes> result = new TreeSet<ArticleAttributes>();
				BufferedReader br = null;
				FileReader fr = null;
				String str;
				// 合并前将结果再做一次过滤，将有{，{，=，：之类的行都去掉
				try {
					fr = new FileReader(file);
					br = new BufferedReader(fr);

					str = br.readLine();
					while (str != null) {
						String[] contents = str.split("\t");

						ArticleAttributes articleAttr = new ArticleAttributes(
								contents[0]);
						double ctx_sim = 0.0;
						if (!contents[1].equals("NaN"))
							ctx_sim = Double.parseDouble(contents[1]);
						articleAttr.setCtx_wt(Double.parseDouble(contents[2]));
						articleAttr.setCtx_ct(Integer.parseInt(contents[3]));
						articleAttr.setSubstr_test(Boolean
								.parseBoolean(contents[4]));
						articleAttr.setEditDistance_test(Boolean
								.parseBoolean(contents[5]));

						articleAttr.setCtx_sim(ctx_sim);
						result.add(articleAttr);
						str = br.readLine();
					}

					candidates.put(new Query(query, id), result);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
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
		LinkComboRanking lcr = new LinkComboRanking();
		lcr.linkComboRanking();
	}

}
