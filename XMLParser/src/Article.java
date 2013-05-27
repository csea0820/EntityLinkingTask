import java.util.Set;
import java.util.TreeSet;


public class Article implements Comparable<Article>{
	String articleName;
	int id;
	Set<String> referencedArticle = null;
	
	Set<String> dps = null;
	Set<String> eab = null;
	Set<String> setm = null;
	
	Article()
	{
		referencedArticle = new TreeSet<String>();
		dps = new TreeSet<String>();
		setm = new TreeSet<String>();
		eab = new TreeSet<String>();
	}
	
	Article(String articleName)
	{
		this();
		this.articleName = articleName;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getArticleName() {
		return articleName;
	}
	
	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}
	
	public Set<String> getReferencedArticle() {
		return referencedArticle;
	}
	
	public void addReferencedArticle(String article)
	{
		referencedArticle.add(article);
	}
	
	public void addSETM(String source)
	{
		setm.add(source);
		
	}
	
	public void setDps(Set<String> dps) {
		this.dps = dps;
	}
	
	public void addDP(String dp)
	{
		dps.add(dp);
	}
	
	public void addEAB(String shortName)
	{
		eab.add(shortName);
	}
	
	public Set<String> getSetm() {
		return setm;
	}

	@Override
	public int compareTo(Article o) {
		return articleName.compareTo(o.articleName);
	}	
}
