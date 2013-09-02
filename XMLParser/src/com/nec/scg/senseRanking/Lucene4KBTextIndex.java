package com.nec.scg.senseRanking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nec.scg.utility.Constant;


public class Lucene4KBTextIndex extends DefaultHandler {
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
		startMili = System.currentTimeMillis();// 锟斤拷前时锟斤拷锟接︼拷暮锟斤拷锟斤拷锟�
		
		analyzer = new SimpleAnalyzer(Version.LUCENE_43);
		File indexDir = new File("D:\\KBTextIndex");
		try {
			directory = FSDirectory.open(indexDir);
			 config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
			    //config.setOpenMode(OpenMode.CREATE); 
			 iwriter = new IndexWriter(directory, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	   
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;
		if ("wiki_text".equals(qName))
			text_content = new StringBuilder(1000);
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length);				
			if ("wiki_text".equals(preTag)) {	
				text_content.append(content);
			} 
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("wiki_text".equals(preTag)) {
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
    	 	
        Document doc = new Document();
        doc.add(new Field("fieldname", content, TextField.TYPE_STORED));
        iwriter.addDocument(doc);
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
		//System.out.println("锟斤拷珊锟窖★拷吆锟绞蔽拷锟� + (endMili - startMili) + "锟斤拷锟斤拷");
		super.endDocument();

		endMili = System.currentTimeMillis();
		//System.out.println("锟杰猴拷时为锟斤拷" + (endMili - startMili) + "锟斤拷锟斤拷");
		super.endDocument();
		
		 try {
			iwriter.close();
			 directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		File dir = new File(Constant.KB_DIR);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		Lucene4KBTextIndex luc = new Lucene4KBTextIndex();
		try {
			SAXParser saxParser = spf.newSAXParser();
			if (dir.isDirectory()){
				File[] files = dir.listFiles();
				for (File file : files){
					saxParser.parse(file, luc);
					System.out.println(file.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}