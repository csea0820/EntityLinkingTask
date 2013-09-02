/**
 * @Author Xiaofeng
 * @Date 2013-8-23 ÏÂÎç3:29:04
 */
package com.nec.scg.senseRanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

import com.nec.scg.utility.Constant;
import com.nec.scg.utility.Utility;

public class TopicModels {

	private String OUTPUT_DIR = "D:\\TAC_RESULT\\topicModel\\";
	
	
	public void formInstances(){
		
		
		List<Query> queries = QueryFactory.getQueryInfo(Constant.queryXmlFile);
	
		for (Query query : queries) {
			String content = query.getId()+"\tX\t"+DocumentKeyword.getinstance().getDocContent(query.getDocument())+"\n";
			Utility.appendContentToFile(generateOuputPath(query), content);
		}
		
		Map<Query,Set<ArticleAttributes>> candidates = Utility.readCandidates(Constant.candidateDirectory);
		Set<String> allCandidates = new TreeSet<String>();
		Map<String,List<Query>> candidateToQuery = new TreeMap<String,List<Query>>();
		
		for (Query query : candidates.keySet()){
			for (ArticleAttributes art : candidates.get(query)){
				allCandidates.add(art.getName());
				List<Query> list = candidateToQuery.get(art.getName());
				if (list == null){
					list = new ArrayList<Query>();
					candidateToQuery.put(art.getName(), list);
				}
				list.add(query);
			}
		}
		
		int index = 0;
		File dir = new File(Constant.KB_DIR);
		if (dir.isDirectory()){
			File[] files = dir.listFiles();
			for (File file : files){
				DocumentBuilderFactory domFactory = DocumentBuilderFactory
						.newInstance();
				domFactory.setNamespaceAware(true); // never forget this!
				DocumentBuilder builder;
				System.out.println("File " + ++index);
				try {
					builder = domFactory.newDocumentBuilder();
					Document doc = builder.parse(file);

					XPathFactory factory = XPathFactory.newInstance();
					XPath xpath = factory.newXPath();
					XPathExpression expr = xpath.compile("/knowledge_base/entity");

					Object result = expr.evaluate(doc, XPathConstants.NODESET);
					NodeList nodes = (NodeList) result;
					for (int i = 0; i < nodes.getLength(); i++) {
						String title = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						if (allCandidates.contains(title)){
							List<Query> list = candidateToQuery.get(title);
							
							String content = title.replaceAll(" ", "_")+"\tX\t"+nodes.item(i).getChildNodes().item(3).getTextContent().replaceAll("\n", " ")+"\n";
							for (Query q : list)
								Utility.appendContentToFile(generateOuputPath(q), content);
						}
					}
				} catch (ParserConfigurationException | XPathExpressionException
						| SAXException | IOException e) {
					e.printStackTrace();
				}
			}					
		}
	}
	
	
	private String generateOuputPath(Query query){
		return OUTPUT_DIR+query.getId()+"_"+query.getQuery();
	}
	
	
	public String calcTopicSim(File file) throws IOException
	{
		  // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("C:\\Users\\Xiaofeng\\Desktop\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        
        Reader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields
        if (instances.size() <= 2){
        	if (instances.size() == 1)return null;
        	else return (String) instances.get(1).getName();
        }

        int numTopics = 10;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);
        model.setNumThreads(10);

        model.setNumIterations(500);
        model.estimate();

        int instanceID = 0;
        
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(instanceID);
        double []sim = new double [instances.size()];
        for (int i = 1; i < instances.size(); i++){
        	sim[i] = KLDivergence(topicDistribution, model.getTopicProbabilities(i));
        }
        
        double maxSim = 0.0;
        int maxIndex = -1;
        for (int i = 1; i < instances.size(); i++)
        {
        	if (maxSim < sim[i]){
        		maxSim = sim[i];
        		maxIndex = i;
        	}
        }
        System.out.println("Sim = " + sim[1]);
        System.out.println("Similarity = " + maxSim+",Index = " + maxIndex +", Title = " + instances.get(maxIndex).getName());
        return ((String) instances.get(maxIndex).getName()).replaceAll("_", " ");
	}
	
	private double KLDivergence(double []a,double []b){
		return (KL(a,b)+KL(b,a))/2;
	}
	
	private double KL(double []a,double []b){
		double res = 0.0;
		for (int i = 0; i < a.length; i++){
			res += a[i]*Math.log(a[i]/b[i]);
		}
		
		return res;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new TopicModels().calcTopicSim(new File("D:\\TAC_RESULT\\topicModel\\3301_Texas"));
		} catch (IOException e) {
			e.printStackTrace();
		}
//		new TopicModels().formInstances();
	}

}
