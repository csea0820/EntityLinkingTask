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


public class Top8 {
	String path = "D:\\TAC_RESULT\\";
	
	Map<String, Set<String>> candidateTerms = null;
	Map<String, Double> linkability = null;

	Relatedness relatedness = new Relatedness();
	

	class Top8Entity implements Comparable<Top8Entity>{
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
			else return 0;
		}
		
		
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

					
					String[] content = line.split(",");
					if (content.length < 2)
						continue;

					termsSet = new TreeSet<String>();
					for (int i = 1; i < content.length; i++)
						termsSet.add(content[i]);
					candidateTerms.put(content[0], termsSet);					
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void  termsLinkability() {
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

	public Map<String,Set<String>> getTop8Terms()
	{
		termsMap();
		Map<String,Set<String>> top8 = new TreeMap<String,Set<String>>();
		for (String article : candidateTerms.keySet()){
			Set<Top8Entity> set = countScore(article);
			Set<String> terms = new TreeSet<String>();

			
			int count = 0;
			for (Top8Entity te : set)
			{
				++count;
				if (count > 8)break;
				terms.add(te.term);
			}		
			top8.put(article, terms);		
		}
		
		return top8;
//		return candidateTerms;
	}
	
	//返回article中关键词的得分
	private Set<Top8Entity> countScore(String articleName) {
		
		if (candidateTerms == null)termsMap();
		if (linkability == null)termsLinkability();
		
		Set<String> termsSet;
		
		Set<Top8Entity> termScore = new TreeSet<Top8Entity>();

		termsSet = candidateTerms.get(articleName);
		int i = 0;
		String[] terms = new String[termsSet.size()];
		for (String s : termsSet) {
			terms[i] = s;
			i++;
		}
		
//		String [] terms = (String[]) termsSet.toArray();
		
		// 计算每个term与其他term的relatedness总和，之后和term的linkability求平均即是该term的得分
		for (int j = 0; j < terms.length; j++) {
			double relateSum = 0.0;
			double score = 0.0;
			for (int k = 0; k < terms.length; k++) {
				if (k != j) {
					try {
						relateSum += relatedness.relatedness(terms[j], terms[k]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			score = (linkability.get(terms[j]) + (relateSum / (terms.length-1))) / 2;
			termScore.add(new Top8Entity(terms[j],score));
		}

		return termScore;
	}

	public void close(){
		relatedness.close();
	}


	public static void main(String[] args) {
		System.out.print(new Top8().getTop8Terms().get("1233 ABC Newcastle"));
	}

}
