
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class ArticleRedirectName{
	private String articleName;
	private String redirect;
	
	public void setArticleName(String articleName){
		this.articleName = articleName;
	}
	
	public String getArticleName(){
		return articleName;
	}
	
	public void setRedirect(String redirect){
		this.redirect = redirect;
	}
	
	public String getRedirect(){
		return redirect;
	}
}

class Nomalized{	
	private String nomalized;

	public void setNomalized(String nomalized){
		this.nomalized = nomalized;
	}
	
	public String getNomalized(){
		return nomalized;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(this == obj){
			return true;
		}
		if(obj !=null&&obj.getClass()==Nomalized.class){
			Nomalized nom = (Nomalized)obj;
			if(nom.nomalized.equals(this.nomalized)){
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.nomalized.hashCode();
	}
	
}

public class FirstTable extends DefaultHandler {

	int index = 0;
	String preTag = null;

	long startMili;
	long endMili;

/*	private ArticlePage articlePage;
	private List<ArticlePage> articlePages;*/
	
	private ArticleRedirectName articleRedirectNameObject;
	private Nomalized nomalizedObject;
	private Map<Nomalized,Set<ArticleRedirectName>> NAR;
	private Set<ArticleRedirectName> sameNomalizedObject;

	FirstTable() {
	}

	/*public List<ArticlePage> getList() {
		return articlePages;
	}*/
	public Map<Nomalized, Set<ArticleRedirectName>> getMap() {
		return NAR;
	}
	
	@Override
	public void startDocument() throws SAXException {
		startMili = System.currentTimeMillis();// 当前时间对应的毫秒数
		//articlePages = new ArrayList<ArticlePage>();
		NAR = new HashMap<Nomalized,Set<ArticleRedirectName>>();
		
	}

	@Override
	public void endDocument() throws SAXException {

		// System.out.println("Total pages:"+index);
		endMili = System.currentTimeMillis();
		// System.out.println("总耗时为："+(endMili-startMili)+"毫秒");
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("redirect")) {
			articleRedirectNameObject.setRedirect(attributes.getValue(0));
		}
		preTag = qName;

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		preTag = null;

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (preTag != null) {
			String content = new String(ch, start, length);
			if (preTag.equals("title")) {
				articleRedirectNameObject = new ArticleRedirectName();
				articleRedirectNameObject.setArticleName(content);
				sameNomalizedObject = new HashSet<ArticleRedirectName>();
				sameNomalizedObject.add(articleRedirectNameObject);
				/*
				 * normalizedName 通过调研发现title的（）目前只出现在句尾。所以去（）标签同时转化为小写
				 */
				nomalizedObject = new Nomalized();
				if (content.contains("(") && content.contains(")")) {
					nomalizedObject.setNomalized(content.substring(0,content.indexOf("(")).toLowerCase());
				} 
				else {
					nomalizedObject.setNomalized(content.toLowerCase());
				}
				/*
				 * 判断map中是否有重复的key,如果有重复的key就将value加到set中
				 */
				if(!NAR.containsKey(nomalizedObject)){
					NAR.put(nomalizedObject,sameNomalizedObject);
				}
				else{
				   NAR.get(nomalizedObject).add(articleRedirectNameObject);
				}
				
			}
			
		}
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String filename ="C:\\chenxiulongdata\\enwiki-latest-pages-articles.xml";
		String filename = "C:\\Users\\sdcpku\\Desktop\\test3.xml";
		SAXParserFactory spf = SAXParserFactory.newInstance();
		FirstTable firsttable = new FirstTable();
		try {
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(new File(filename), firsttable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * 遍历输出
		 */
		Map<Nomalized, Set<ArticleRedirectName>> narmap = firsttable.getMap();
		Iterator iterator = narmap.entrySet().iterator();
		while (iterator.hasNext()) {
			System.out.println();
			Map.Entry entry = (Map.Entry) iterator.next();
			Nomalized nomalizedname = (Nomalized) entry.getKey();
			System.out.println("nomalizedname  "+nomalizedname.getNomalized() );
			Set redirectNomalized = (Set) entry.getValue();
			Iterator setIterator = redirectNomalized.iterator();
			
			while(setIterator.hasNext()){
				ArticleRedirectName artredic = (ArticleRedirectName)setIterator.next();
				System.out.println("articlename:   "+artredic.getArticleName());
				System.out.println("redirect:   "+artredic.getRedirect());
			}
/*			System.out.println("redirect "+redirectNomalized.getRedirect() );
			System.out.println("nomalized "+redirectNomalized.getNomalized());*/
		}
	}

}
