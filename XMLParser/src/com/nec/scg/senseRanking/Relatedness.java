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

	Directory directory = FSDirectory.open(new File("D:\\index"));
	DirectoryReader ireader = DirectoryReader.open(directory);
	IndexSearcher isearcher = new IndexSearcher(ireader);
	String entityQuery;
	String term;
	
	public int SearchResultCount()throws IOException {
		Term word0 = new Term("fieldname", entityQuery);
		Term word1 = new Term("fieldname", term);
		PhraseQuery query = new PhraseQuery();
		query.add(word0);
		query.add(word1);
		query.setSlop(0);
		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
		System.out.println("一起查询："+hits.length);
		return hits.length;
	}

	public int SearchResultCount(String searchString)
			throws IOException {
		Term word0 = new Term("fieldname", searchString);
		PhraseQuery query = new PhraseQuery();
		query.add(word0);
		query.setSlop(0);
		ScoreDoc[] hits = isearcher.search(query, Integer.MAX_VALUE).scoreDocs;
		System.out.println("单个查询："+hits.length);

		return hits.length;
	}
	public Relatedness(String entityQuery,String term) throws IOException {
		this.entityQuery=entityQuery;
		this.term=term;
		float relatedness =(float)(SearchResultCount())/(float)(SearchResultCount(entityQuery) + SearchResultCount(term));
		System.out.println(relatedness);
		entityQuery=null;
		term=null;
		ireader.close();
		directory.close();
	}

	public static void main(String[] args) throws IOException {
		new Relatedness("software","foundation");
	}

}
