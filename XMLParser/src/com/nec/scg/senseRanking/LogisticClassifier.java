/**
 * @Author Xiaofeng
 * @Date 2013-7-20 ÏÂÎç5:01:30
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import com.nec.scg.senseGenerator.EvaluationMetric;
import com.nec.scg.senseGenerator.ExpectedLinkResult;
import com.nec.scg.utility.Utility;

public class LogisticClassifier {

	Instances data;
	AbstractClassifier classifier = new RandomForest();
	
	public void train(String arffFile) {

		try {
			data = new Instances(new BufferedReader(new FileReader(arffFile)));
			data.setClassIndex(data.numAttributes() - 1);
//			logistic.buildClassifier(data);
			
			classifier.buildClassifier(data);
			
			int cnt = 0;
			for (Instance instance : data){
				if (instance.classValue() == 0.0)
					if (classifier.classifyInstance(instance) == instance.classValue()){
						cnt++;
					}
					
			}
			System.out.println("True Cnt : " + cnt);
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
			ret = classifier.distributionForInstance(instance);
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
			ret = classifier.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		LogisticClassifier lc = new LogisticClassifier();
		TrainingSetGen set = new TrainingSetGen();
		set.trainingSetGenerate("D:\\TAC_RESULT\\linkComboRanking", "linkresult");
		lc.train(set.getOutputFile());
		
		
		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult("linkresult");

		EvaluationMetric em = new EvaluationMetric();
		
		int querySize = elr.getQueries().size();
		em.allRelevantPagesPlus(querySize);
		int cnt = 0;

		StringBuilder sb = new StringBuilder();
		sb.append(ArticleAttributes.getArffHeader()).append("\n");
		sb.append("@data\n");
		File dir = new File("D:\\TAC_RESULT\\linkComboRanking");
		
		List<Integer> ids = new ArrayList<Integer>();
		List<String> names = new ArrayList<String>();
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String[] queryInfo = file.getName().substring(0,file.getName().length()-4).split("_");
				String query = queryInfo[1];
				int id = Integer.parseInt(queryInfo[0]);
				List<ArticleAttributes> senses = ArticleAttributes.readSenses(file);
				if (senses.size() == 0){
					em.returnedRelevantPagesPlus();
					continue;
				}
//				if (senses.get(0).getName().equals(elr.getExpectedResult(id)))
//					senses.get(0).setCorrectSense(true);
				sb.append(senses.get(0).toArffFormat());
				ids.add(id);
				names.add(senses.get(0).getName());
				
//				double [] probs = lc.distributionForInstance(senses.get(0));
				
//				if (senses.get(0).getName().equals(elr.getExpectedResult(id))){
//					System.out.println("MATCH");
//					System.out.println(probs[0]+","+probs[1]);
//					System.out.println(senses.get(0));
//				}
//				if (lc.classifyInstance(senses.get(0)) == 0.0){
////					if (probs[0] > 0.7){
////						System.out.println("HIT:"+probs[0]);
////						System.out.println(query+","+id+","+elr.getExpectedResult(id));
//						if (elr.getExpectedResult(id).equals(query))
//						{
////							System.out.println("HIT2");
//							em.returnedRelevantPagesPlus();
//							cnt++;
//						}
////					}
//						
//				}else{
//					if (elr.getExpectedResult(id).equals("NIL"))
//						em.returnedRelevantPagesPlus();
//				}
			}
			
			Utility.writeToFile("D:\\TAC_RESULT\\evaluationArff.arff", sb.toString());
		}
		
		
		Instances data = new Instances(new BufferedReader(new FileReader("D:\\TAC_RESULT\\evaluationArff.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		
		int index = 0;
		for (Instance instance : data){
			
//			if (instance.classValue() == 0.0)
				if (lc.classifier.classifyInstance(instance) == 0.0)
				{
					if (elr.getExpectedResult(ids.get(index)).equals(names.get(index)))
					{
						cnt++;
						em.returnedRelevantPagesPlus();
					}
				}
				else
				{
					if (elr.getExpectedResult(ids.get(index)).equals("NIL"))
						em.returnedRelevantPagesPlus();
				}
				index++;
		}
		
		System.out.println("True Cnt:" + cnt);
		System.out.println("Precision = " + em.getM_returned_relevant_pages()*1.0/em.getM_all_relevant_pages());
		
		
	}

}
