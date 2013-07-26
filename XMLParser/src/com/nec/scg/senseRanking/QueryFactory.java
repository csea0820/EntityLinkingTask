/**
 * @Author Xiaofeng
 * @Date 2013-7-26 ÏÂÎç5:14:20
 */
package com.nec.scg.senseRanking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nec.scg.utility.Constant;

public class QueryFactory extends DefaultHandler{
	
	List<Query> queries = new ArrayList<Query>();
	Query currentQuery = null;
	int queryId = 0;
	String curTag = null;
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String content = new String(ch, start, length);
		if ("name".equals(curTag))
		{
			currentQuery.setQuery(content);
		}
		else if ("docid".equals(curTag)){
			currentQuery.setDocID(content);
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("query".equals(qName)){
			queries.add(currentQuery);
		}
		
		curTag = null;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("query")){
			currentQuery = new Query(queryId++);
		}
		
		curTag = qName;
	}
	
	
	public List<Query> getQueries() {
		return queries;
	}
	
	public static List<Query> getQueryInfo(String filename){
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		QueryFactory qf = null;
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename),qf =  new QueryFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return qf.getQueries();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Query> ret = QueryFactory.getQueryInfo(Constant.queryXmlFile);
		System.out.println(ret.size());
		for (Query q : ret){
			System.out.println("id = " + q.id+",name = " + q.query+",docID = " + q.docID+",path = " + q.getDocument().getAbsolutePath());
		}
	}

}
