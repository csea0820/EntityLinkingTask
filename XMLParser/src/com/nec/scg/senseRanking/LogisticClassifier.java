/**
 * @Author Xiaofeng
 * @Date 2013-7-20 ÏÂÎç5:01:30
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import weka.classifiers.functions.Logistic;
import weka.core.Instance;
import weka.core.Instances;

import com.nec.scg.senseGenerator.EvaluationMetric;
import com.nec.scg.senseGenerator.ExpectedLinkResult;

public class LogisticClassifier {

	Logistic logistic = new Logistic();
	Instances data;

	public void train(String arffFile) {

		try {
			data = new Instances(new BufferedReader(new FileReader(arffFile)));
			data.setClassIndex(data.numAttributes() - 1);
			logistic.buildClassifier(data);
			
//			System.out.println(logistic.classifyInstance(data.firstInstance()));
//			for (double d : logistic.distributionForInstance(data.firstInstance()))
//				System.out.println(d);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double[] distributionForInstance(ArticleAttributes art){
		Instance instance = art.toWekaInstance(data);
		instance.setDataset(data);
		double [] ret = null;
		try {
			ret = logistic.distributionForInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public double classifyInstance(ArticleAttributes art){
		

		Instance instance = art.toWekaInstance(data);
		instance.setDataset(data);
		double ret = 0.0;
		try {
			ret = logistic.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LogisticClassifier lc = new LogisticClassifier();
		lc.train("D:\\TAC_RESULT\\training.arff");
		
		
		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult("linkresult");

		EvaluationMetric em = new EvaluationMetric();
		
		int querySize = elr.getQueries().size();
		em.allRelevantPagesPlus(querySize);
		
		File dir = new File("D:\\TAC_RESULT\\linkComboRanking");
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String query = file.getName().substring(0,file.getName().length()-4);
				List<ArticleAttributes> senses = ArticleAttributes.readSenses(file);
				if (senses.size() == 0){
					em.returnedRelevantPagesPlus();
					continue;
				}
				double [] probs = lc.distributionForInstance(senses.get(0));
				if (lc.classifyInstance(senses.get(0)) == 0.0){
					if (probs[0] > 0.9){
						if (elr.getExpectedResult(query).equals(query))
							em.returnedRelevantPagesPlus();
					}
				}else{
					if (elr.getExpectedResult(query).equals("NIL"))
						em.returnedRelevantPagesPlus();
				}
			}
		}
		System.out.println("Precision = " + em.getM_returned_relevant_pages()*1.0/em.getM_all_relevant_pages());
		
		
	}

}
