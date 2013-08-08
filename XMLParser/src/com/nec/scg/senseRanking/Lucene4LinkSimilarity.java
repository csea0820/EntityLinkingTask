package com.nec.scg.senseRanking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nec.scg.senseGenerator.STEMLinking;

public class Lucene4LinkSimilarity extends DefaultHandler {
	int index = 0;
	String preTag = null;
	long startMili;
	long endMili;
	StringBuilder text_content = null;
	
	Analyzer analyzer = null;
	File indexDir = null;
	Directory directory = null;
    IndexWriterConfig config = null;
    //config.setOpenMode(OpenMode.CREATE); 
    IndexWriter iwriter = null;
    List<Document> list = new ArrayList<Document>();

	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数
		
		analyzer = new SimpleAnalyzer(Version.LUCENE_43);
		File indexDir = new File("D:\\index");
		try {
			directory = FSDirectory.open(indexDir);
			 config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
			    //config.setOpenMode(OpenMode.CREATE); 
			 iwriter = new IndexWriter(directory, config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;
		if (preTag.equals("text"))
			text_content = new StringBuilder(1000);
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length);				
			if (preTag.equals("text")) {	
				text_content.append(content);
			} 
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("text".equals(preTag)) {
			try {
				createIndex();
			} catch (Exception e) {
				e.printStackTrace();
			}
			timeStamp();
		}
		preTag = null;
	}

	private void createIndex() throws Exception{
		String content = text_content.toString().trim();
		text_content.delete(0, text_content.length());
		text_content = null;
    	
        // Store the index in memory:
       // Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
    	
        Document doc = new Document();
        doc.add(new Field("fieldname", content, TextField.TYPE_STORED));
        iwriter.addDocument(doc);
//        list.add(doc);
//        if (list.size() == 20)
//        {
//        	iwriter.addDocuments(list);
//        	list = new ArrayList<Document>();
//        }
       
        
/*        
        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        // Parse a simple query that searches for "text":
  //      QueryParser parser = new QueryParser(Version.LUCENE_43, "fieldname", analyzer);
  //      Query query = parser.parse("This TokenStream");
  //      TermQuery tq = new TermQuery(new Term("fieldname", "TokenStream"));
        
        Term word0 = new Term("fieldname", "the");
//        Term word1 = new Term("fieldname", "apache");
//        Term word2 = new Term("fieldname","software");
//        Term word3 = new Term("fieldname","foundation");
        
        PhraseQuery query = new PhraseQuery();
        query.add(word0);
//        query.add(word1);
//        query.add(word2);
//        query.add(word3);
        query.setSlop(0);
        ScoreDoc[] hits = isearcher.search(query,1000).scoreDocs;
        if (hits.length == 1)System.out.println("hits.length == 1");
        else System.out.println("hits.length != 1");
        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
          Document hitDoc = isearcher.doc(hits[i].doc);
          System.out.println(hitDoc.get("fieldname"));
        }
        ireader.close();*/
       

	}
	protected void timeStamp() {
		if (++index % 10000 == 0) {
			endMili = System.currentTimeMillis();
			System.out.println(index + "," + (endMili - startMili) / 1000);
			System.gc();
		}
	}


	@Override
	public void endDocument() throws SAXException {
		endMili = System.currentTimeMillis();
		System.out.println("生成候选者耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();

		endMili = System.currentTimeMillis();
		System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
		super.endDocument();
		
		 try {
			iwriter.close();
			 directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String filename =  "D:\\KBP数据集\\enwiki-latest-pages-articles.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		Lucene4LinkSimilarity luc = new Lucene4LinkSimilarity();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), luc);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}