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

import org.apache.lucene.queryparser.classic.ParseException;

import com.nec.scg.utility.Utility;

public class Top8 {
	String path = "D:\\TAC_RESULT\\";

	Map<String, Set<String>> candidateTerms = null;
	Map<String, Double> linkability = null;
	
	public final String cache_file = "D:\\TAC_RESULT\\topTermsCache";

	private int topNTerm = 8;

	Map<String, Set<String>> topTermsCaches = null;

	class Top8Entity implements Comparable<Top8Entity> {
		String term;
		double score;

		public Top8Entity(String term, double score) {
			super();
			this.term = term;
			this.score = score;
		}

		@Override
		public int compareTo(Top8Entity o) {
			if (o.score - score > 0)
				return 1;
			else
				return 0;
		}

	}

	public Top8() {
		topTermsCaches = Utility.readCache(cache_file);
	}

	private void termsMap() {
		candidateTerms = new TreeMap<String, Set<String>>();
		Set<String> termsSet = new TreeSet<String>();
		File articleterms = new File(path + "articleTerms.txt");

		try {
			BufferedReader in = new BufferedReader(new FileReader(articleterms));
			String line;
			try {
				while ((line = in.readLine()) != null) {
					
					String[] content = line.split("\t");
				
					if (content.length < 2)
						continue;

					
					termsSet = new TreeSet<String>();
					for (int i = 1; i < content.length; i++)
						if (!termsSet.contains("<") && !termsSet.contains(">"))
							termsSet.add(content[i]);
					candidateTerms.put(content[0].toLowerCase(), termsSet);
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void termsLinkability() {
		File termLinkability = new File(path + "\\linkability.txt");
		linkability = new TreeMap<String, Double>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(
					termLinkability));
			String line;

			try {
				in.readLine();
				while ((line = in.readLine()) != null) {

					String[] content = line.split(",");
					if (content.length != 3)
						continue;
					linkability.put(content[0], Double.parseDouble(content[1])
							/ Double.parseDouble(content[2]));
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Set<String>> getTop8Terms() {
		termsMap();
		// Map<String,Set<String>> top8 = new TreeMap<String,Set<String>>();
		// int index = 0;
		// for (String article : candidateTerms.keySet()){
		//
		// System.out.println(++index);
		//
		// Set<Top8Entity> set = countScore(article);
		// Set<String> terms = new TreeSet<String>();
		//
		//
		// int count = 0;
		// for (Top8Entity te : set)
		// {
		// ++count;
		// if (count > 8)break;
		// terms.add(te.term);
		// }
		// top8.put(article, terms);
		// }
		//
		// return top8;
		return candidateTerms;
	}

	
	
	public Set<String> getTopTerms(String articleName) {
		if (topTermsCaches.containsKey(articleName))
			return topTermsCaches.get(articleName);

		if (candidateTerms == null)
			termsMap();
		if (linkability == null)
			termsLinkability();
		
		Set<String> termsSet = candidateTerms.get(articleName);
		
		if (termsSet == null || termsSet.size() <= topNTerm)
			return termsSet;

		Set<Top8Entity> termScore = new TreeSet<Top8Entity>();

		int i = 0;
		String[] terms = new String[termsSet.size()];
		for (String s : termsSet) {
			terms[i] = s;
			i++;
		}

		// 计算每个term与其他term的relatedness总和，之后和term的linkability求平均即是该term的得分
		for (int j = 0; j < terms.length; j++) {
			double relateSum = 0.0;
			double score = 0.0;
			for (int k = 0; k < terms.length; k++) {
				if (k != j) {
					try {
						relateSum += Relatedness.getInstance()
								.relatedness(terms[j], terms[k]);
					} catch (IOException | ParseException e) {
						e.printStackTrace();
					}
				}
			}
			double a = 0.0;
			try {
				a = linkability.get(terms[j]);
			} catch (NullPointerException nul) {
			}
			double b = relateSum / (terms.length - 1);
			score = (a + b) / 2;
			termScore.add(new Top8Entity(terms[j], score));
		}

		Set<String> ret = new TreeSet<String>();

		int count = 0;
		for (Top8Entity te : termScore) {
			++count;
			if (count > topNTerm)
				break;
			ret.add(te.term);
		}

		topTermsCaches.put(articleName, ret);
		return ret;
	}

//	// 返回article中关键词的得分
//	private Set<Top8Entity> countScore(String articleName) {
//
//		if (candidateTerms == null)
//			termsMap();
//		if (linkability == null)
//			termsLinkability();
//
//		Set<String> termsSet;
//
//		Set<Top8Entity> termScore = new TreeSet<Top8Entity>();
//
//		termsSet = candidateTerms.get(articleName);
//		if (termsSet.isEmpty())
//			return termScore;
//		int i = 0;
//		String[] terms = new String[termsSet.size()];
//		for (String s : termsSet) {
//			terms[i] = s;
//			i++;
//		}
//
//		// String [] terms = (String[]) termsSet.toArray();
//
//		// 计算每个term与其他term的relatedness总和，之后和term的linkability求平均即是该term的得分
//		for (int j = 0; j < terms.length; j++) {
//			double relateSum = 0.0;
//			double score = 0.0;
//			for (int k = 0; k < terms.length; k++) {
//				if (k != j) {
//					try {
//						relateSum += Relatedness.getInstance()
//								.relatedness(terms[j], terms[k]);
//					} catch (IOException | ParseException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			if (terms[j] == null)
//				System.out.println("null");
//
//			double a = 0.0;
//			try {
//				a = linkability.get(terms[j]);
//			} catch (NullPointerException nul) {
//			}
//			double b = relateSum / (terms.length - 1);
//			score = (a + b) / 2;
//			termScore.add(new Top8Entity(terms[j], score));
//		}
//
//		return termScore;
//	}

	public void close() {
//		Relatedness.getInstance().close();
		Utility.saveCache(topTermsCaches, cache_file);
	}

	public static void main(String[] args) {
//		System.out.print(new Top8().getTop8Terms().get("1233 ABC Newcastle"));
	}

}
