package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.nec.scg.utility.Timer;
import com.nec.scg.utility.Utility;

public class Relatedness {

	Directory directory = null;
	DirectoryReader ireader = null;
	IndexSearcher isearcher = null;
	
	private final String CACHE_FILE = "D:\\TAC_RESULT\\relatednessCache.txt";
	
	Map<String,Integer> cache = new TreeMap<String,Integer>();
	
	String defaultField = "fieldname";
	Analyzer analyzer = null;
	QueryParser queryParser = null;
	
	private int SearchResultCount(String searchString,boolean useCache){
		
		if (useCache && cache.containsKey(searchString))return cache.get(searchString);

		
		Query query = null;
		int res = 0;
		try{
			query = queryParser.parse(searchString);
			
			ScoreDoc[] hits = isearcher.search(query, 1000000).scoreDocs;
			if (useCache)cache.put(searchString, hits.length);
			res = hits.length;
		}catch(Exception e){
			res = 0;
		}
//		System.out.println(searchString+","+hits.length);
		return res;
	}
	
	
	
	private int searchCoOccurrence(PhraseQuery q1,PhraseQuery q2) throws IOException{
		BooleanQuery query = new BooleanQuery();
		query.add(q1, Occur.MUST);
		query.add(q2,Occur.MUST);
		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
		return hits.length;
	}
	
	private int search(PhraseQuery query) throws IOException
	{
		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
		System.out.println(hits.length);
		return hits.length;
	}
	
	private PhraseQuery formPhraseQuery(String string){
		
		String[] searchString = string.trim().split("\\s+");
		Term[] words = new Term[searchString.length];
		for (int i = 0; i < searchString.length; i++)
			words[i] = new Term("fieldname",searchString[i]);
		PhraseQuery query = new PhraseQuery();
		for (Term t : words)
			query.add(t);
		query.setSlop(0);
		System.out.println(query.toString());
		return query;
	}

	public Relatedness() {
		open();
	}
	
	
	public double relatedness(String entityQuery,String term) throws IOException, ParseException
	{
		return SearchResultCount("\""+entityQuery+"\"" + "AND\"" + term + "\"",true)*1.0/(SearchResultCount("\""+entityQuery+"\"",true)+SearchResultCount("\""+term+"\"",true));
		
		
//		PhraseQuery q1 = formPhraseQuery(entityQuery);
//		PhraseQuery q2 = formPhraseQuery(term);
//		return searchCoOccurrence(q1, q2)*1.0/(search(q1)+search(q2));
	}
	
	public void close()
	{
		try {
			writeCache();
			ireader.close();
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeCache(){
		StringBuilder sb = new StringBuilder();
		for (String key : cache.keySet()) {
			Integer e = cache.get(key);
			sb.append(key).append("\t").append(e).append("\n");
		}

		Utility.writeToFile(CACHE_FILE, sb.toString());
	}
	
	private void readCache(String path){
		
		File file = new File(path);
		if (file.exists()) {
			BufferedReader br = null;
			FileReader fr = null;
			String str;

			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				str = br.readLine();

				while (str != null) {
					if (!str.equals("")) {
						String[] content = str.split("\t");
						if (content.length == 2) {
							int value = 0;
							try {
								value = Integer.parseInt(content[1]);
							} catch (NumberFormatException n) {
								str = br.readLine();
								continue;
							}
							cache.put(content[0].trim(), value);
						}
					}
					str = br.readLine();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void open()
	{
		try {
			directory = FSDirectory.open(new File("D:\\index"));
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			readCache(CACHE_FILE);
			analyzer = new SimpleAnalyzer(Version.LUCENE_43);
			queryParser =  new QueryParser(Version.LUCENE_43,
	 				defaultField, analyzer);
//			new Analyzer() {
//		 		  @Override
//		 		   protected TokenStreamComponents createComponents(String fieldName, Reader reader) {	    
//		 		    return new TokenStreamComponents(new WhitespaceTokenizer(Version.LUCENE_43, reader));
//		 		   }
//		 		 };
		 		 
		 	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException {
		Relatedness r = new Relatedness();
		try {
			
			Timer timer = new Timer();
			timer.start();
			for(int i = 0; i < 1000; i++)
				System.out.println(i+","+r.relatedness("Worcester, New York".toLowerCase(),"GNIS".toLowerCase()));
			
			timer.end();
			timer.timeElapse("relatedness");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//System.out.println(r.relatedness("lucene","software")); //查询词区分大小写
		r.close();
	}

}

