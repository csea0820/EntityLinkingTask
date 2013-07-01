package com.nec.scg.senseRanking;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Relatedness {

	Directory directory = null;
	DirectoryReader ireader = null;
	IndexSearcher isearcher = null;
	
//	private int SearchResultCount(String entityQuery,String term)throws IOException {
//		Term word0 = new Term("fieldname", entityQuery);
//		Term word1 = new Term("fieldname", term);
//		PhraseQuery query = new PhraseQuery();
//		query.add(word0);
//		query.add(word1);
//		query.setSlop(0);
//		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
//		System.out.println("一起查询："+hits.length);
//		return hits.length;
//	}
	
	private int SearchResultCount(String[] searchString) throws IOException
	{
		Term[] words = new Term[searchString.length];
		for (int i = 0; i < searchString.length; i++)
			words[i] = new Term("fieldname",searchString[i]);
		PhraseQuery query = new PhraseQuery();
		for (Term t : words)
			query.add(t);
		query.setSlop(0);
		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
		return hits.length;
	}

//	private int SearchResultCount(String searchString)
//			throws IOException {
//		Term word0 = new Term("fieldname", searchString);
//		PhraseQuery query = new PhraseQuery();
//		query.add(word0);
//		query.setSlop(0);
//		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
//		System.out.println("单个查询："+hits.length);
//
//		return hits.length;
//	}
	public Relatedness() {
		open();
	}
	
	
	public double relatedness(String entityQuery,String term) throws IOException
	{
		return 	(float)(SearchResultCount(new String[]{entityQuery,term}))/(float)(SearchResultCount(new String[]{entityQuery}) + SearchResultCount(new String[]{term}));

	}
	
	public void close()
	{
		try {
			ireader.close();
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void open()
	{
		try {
			directory = FSDirectory.open(new File("D:\\index"));
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		Relatedness r = new Relatedness();
		System.out.println(r.relatedness("software","foundation"));
		System.out.println(r.relatedness("the software","foundation"));
		r.close();
	}

}
