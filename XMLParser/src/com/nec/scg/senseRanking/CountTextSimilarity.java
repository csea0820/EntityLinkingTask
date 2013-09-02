package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nec.scg.utility.Constant;
import com.nec.scg.utility.StringIntegerCache;
import com.nec.scg.utility.Utility;

public class CountTextSimilarity {

	int AllArticle = 800000;
	Analyzer analyzer = null;
	Directory directory = null;
	DirectoryReader ireader = null;
	IndexSearcher isearcher = null;
	ScoreDoc[] hits = null;

	private StringIntegerCache cache = null;
	private String cache_file = "D:\\TAC_RESULT\\textSimCache";
	private String OUTPUT_DIR = "D:\\TAC_RESULT\\textSim\\";

	private static CountTextSimilarity instance = null;

	private CountTextSimilarity() {
		init();
	}

	public static CountTextSimilarity getInstance() {
		if (instance == null) {
			instance = new CountTextSimilarity();
		}
		return instance;
	}

	private void init() {
		analyzer = new StandardAnalyzer(Version.LUCENE_43);
		try {
			directory = FSDirectory.open(new File("D:\\KBTextIndex"));
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cache = new StringIntegerCache(cache_file);
		cache.readCache();
	}

	public double getVSMSim(String docContent1, String docContent2) {
		Map<String, Float> allTerm = new TreeMap<String, Float>();
		Map<String, Float> termVector1 = CountTF_IDF(docContent1, analyzer);
		Map<String, Float> termVector2 = CountTF_IDF(docContent2, analyzer);
		allTerm.putAll(termVector1);
		allTerm.putAll(termVector2);
		for (String str : allTerm.keySet()) {
			if (!termVector1.containsKey(str)) {
				termVector1.put(str, 0f);
			}
			if (!termVector2.containsKey(str)) {
				termVector2.put(str, 0f);
			}
		}

		double z = 0f;
		double m = 0f;
		double n = 0f;
		double x = 0f;
		double y = 0f;
		for (String str : allTerm.keySet()) {
			x = termVector1.get(str);
			y = termVector2.get(str);
			z = z + x * y;
			m = m + x * x;
			n = n + y * y;
		}
		return (z / Math.sqrt(m * n));
	}

	public Map<String, Float> CountTF_IDF(String str, Analyzer a) {
		Map<String, Float> termVector = new TreeMap<String, Float>();

		try {
			TokenStream stream = a
					.tokenStream("content", new StringReader(str));
			PorterStemFilter filter = new PorterStemFilter(stream);
			CharTermAttribute cta = filter
					.addAttribute(CharTermAttribute.class);
			filter.reset();
			String strcat = null;
			int wordCount = 0;
			while (filter.incrementToken()) {
				strcat = cta.toString();
				// System.out.print("["+strcat+"]");
				if (!termVector.containsKey(strcat)) {
					termVector.put(strcat, 1f);
					wordCount++;
				} else {
					termVector.put(strcat, termVector.get(strcat) + 1);
					wordCount++;
				}
			}
			for (String ter : termVector.keySet()) {
				int hits = searchIndexforIDF(ter) + 1;
				float idf = (float) (Math.log(AllArticle * 1.0 / hits) + 1.0);
				float tf = termVector.get(ter) / wordCount;
				termVector.put(ter, tf * idf);
			}

			filter.end();
			stream.end();
			filter.close();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return termVector;
	}

	public int searchIndexforIDF(String word) {

		if (cache.containsKey(word))
			return cache.get(word);

		try {
			Term word0 = new Term("fieldname", word);
			PhraseQuery query = new PhraseQuery();
			query.add(word0);
			query.setSlop(0);
			hits = isearcher.search(query, AllArticle).scoreDocs;
			cache.put(word, hits.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hits.length;
	}

	public void close() {
		try {
			ireader.close();
			directory.close();
			cache.saveCache();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void calcCandidateTextSim() {

		File dir = new File("D:\\TAC_RESULT\\topicModel");
		if (dir.isDirectory()){
			File[] files = dir.listFiles();
			int index = 0;
			for (File file : files)
			{
				System.out.println("File " + ++index);
				processFile(file);
			}
		}
	}
	
	private void processFile(File file){

		BufferedReader br = null;
		FileReader fr = null;
		String str;

	
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			str = br.readLine();
			
			String queryContent = str.split("\t")[2];
			
			while ((str = br.readLine()) != null){
				String[] contents = str.split("\t");
				double sim = getVSMSim(queryContent, contents[2]);
				Utility.appendContentToFile(OUTPUT_DIR+file.getName(), contents[0]+"\t"+sim+"\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		
			
	}
	
	private String generateOuputPath(Query query){
		return OUTPUT_DIR+query.getId()+"_"+query.getQuery();
	}

	public static void main(String[] args) throws Exception {
//		System.out
//				.println(CountTextSimilarity
//						.getInstance()
//						.getVSMSim(
//								"Taipei,  May 14 (CNA) 05/14/08  17:16:51 (By Deborah Kuo) An Air Macau plane is scheduled to depart from Taipei for China's Sichuan Province Thursday, marking the first direct cross-strait charter flight to transport relief goods to the southwestern province following Monday's devastating earthquake, Kuomintang Legislator John Chiang said Wednesday. Chiang said at a news conference that the Air Macau charter flight, with some 46 tons of relief goods aboard, was scheduled to depart from the Taoyuan International Airport at 10 a.m. Thursday for Sichuan's Shuangliu International Airport. \"The Cabinet-level Mainland Affairs Council and the Chinese authorities have reached a consensus on the flight,\" Chiang added. The relief goods, including tents, sleeping bags, bodybags and blankets, were donated by several Taiwanese religious and charity organizations, including the Buddhist Compassionate Relief Tzu Chi Foundation, the Buddhist Ling Jiou Mountain and Fukuangshan Temple, Chiang noted. Noting that relief, search and rescue work in the wake of major natural disasters should not be subject to political or religious boundaries, Chiang called for Taiwanese people from all walks of life to pitch in to help the quake victims. The magnitude 7.8 earthquake struck at 2: 28 p.m. Monday, with the epicenter located in Wenchuan, some 100 km from the provincial capital of Chengdu. The death toll from the quake had climbed to well above 12,000 as of Wednesday, with thousands more still trapped under collapsed buildings or reported missing. Air_Macau	X	Air Macau  Air Macau Company Limited (traditional Chinese: 澳門航空) is an airline based in Macau. It is the flag carrier of Macau, operating services to 12 destinations in mainland China, as well as regional international services. Its main base is Macau International Airport.   History  The airline was established on 13 September, 1994, and began commercial operations on 9 November, 1995, with a flight from Macau to Beijing and Shanghai (prior to 1995, there was no air service to Macau other than the helicopter service). The unique one-aircraft service between Beijing, Shanghai and Taipei began on 8 December, 1995. The first pure-freighter service was launched on 7 October, 2002, between Taipei and Shenzhen via Macau. Air Macau is owned by China National Aviation (CNAC) (51%), TAP Portugal (20%), STDM (14%), Eva Air (5%), the Government of Macau (5%) and Macau investors (5%) and employs 1,023 staff (at March 2007).  Recent reports have stated that Air Macau is on the cusp of bankruptcy. A recent article in the Macau Daily Times on July 9th states:  Air Macau crashes Wednesday, 09 July 2008 by Rodolfo Ascenso  \"Air Macau is close to bankruptcy. With losses up to MOP 100 million just within May and June, following another MOP 100 million during the first quarter of this year, chairman of the Board of Directors Zhao Xiaohang, had no choice but to ask for a meeting of shareholders “in order to have a resolution”. In a letter dated July 1, 2008 Zhao stated he, “with deep regret” is forced to inform the shareholders “that since April 30, 2008 the net worth of the company had fallen below half of the value of the company’s capital”, and had triggered the Macau Commercial Code Article 206. Article 206 of the Commercial Code states that when a company administration apprehend to have losses up to half of its capital, the body must propose that “the company be dissolved or that the capital be reduced, unless shareholders, within 60 days from the resolution that arises from such a proposal, pay amounts in money that replenish the assets to a measure equal to the value of the company capital”. Air Macau’s authorised capital is supposed to be MOP 400 million. However, a search in the Trade Register shows that the decision to upgrade from 200 million to 400 million was never registered. Nevertheless a decision in that direction was made and approved by shareholders and the capital realised. This question is important if the shareholders’ decision is carried out via the third option. With losses of MOP 220 million accumulated over the past years, as well as MOP 105 million during the first quarter of 2008, and more than MOP 100 million during the past two month, it is still not clear how much shareholder have to input. Nevertheless, it is already clear that the decision will be made by the main shareholder of Air Macau, Air China Limited. Owning 51 percent of the capital, Air China Limited has the final word as they hold the responsibility for the present situation. In fact, the actual board of directors, with David Fei as CEO, was named and supported by Air China Limited for the last year despite the accumulated losses. Macau Daily Times understands that other shareholders are not keen to inject more money into a company that has a record of losses in a region where business is flourishing. Thus, the future of Air Macau is in the hands of Air China Limited and it is still unclear if they wish to inject more money or just allow Air Macau to go into bankruptcy. Air Macau is owned by Air China Limited (51 percent), SEAP – an investment fund of Portuguese airline TAP – (20 percent), Stanley Ho’s STDM (14 percent), Evergreen Airways Service (Macau) (5 percent), Macau SAR Government (5 percent) and several others (5 percent). So far the shareholders’ meeting (EGM) requested by the chairman of the Board of Directors had not been appointed. An ordinary meeting of directors is scheduled for July 31.\"  Subsequent international reports have equally reported the current financial crisis as well as severe concerns over safety and operations and not just the impending doom of Air Macau. These reports have called for a dramatic change to Macau SAR's current and antiquated aviation regulatory situation. This has been led by Jose Pereira Coutinho who submitted a written interpellation to the Macau SAR Government. This was also reported in the Macau Daily Times:  \"Worrying situation: Air Macau's losses, government's lack of action  01 July 2008  Flagship airline, Air Macau, has recorded losses of up to 100 million patacas in the first quarter of this year, almost the same amount as the losses recorded during the whole of last year. This plus the fact that the government seems too “little concerned” about the situation is worrying lawmaker Jose Pereira Coutinho. In a written interpellation sent to the government yesterday, Coutinho questions the government\'s actions, or lack of them, for allowing a company to continue flying when services are constantly deteriorating with flight attendants barely speaking enough Cantonese or English, and most shockingly when the company has been recording continuous losses in the past four years. “While other local companies have recorder great profits, Air Macau has been accumulating losses, without anyone from the government having the courage to assess the causes of such losses,” Coutinho said. During the whole of last year, Air Macau recorded a loss of 109 million patacas, and when comparing its results for the past four years with that of other international airlines, Air Macau has blamed its losses on the hike in fuel price. “That doesn't stick,” Coutinho said, “we can't forget that the losses recorded in the last three years were mainly to do with the way in which the company was run.” For the past ten years Air Macau has cancelled flights to destinations such as Kuala Lumpur, Siem Reap, Singapore and Pusan, “but what is more worrying is that other low cost airlines have started flying to these destinations and have been successful,” Coutinho added. Last year alone, Air Macau cancelled some 3,000 flights, which is 20 per cent of its total flights held for the twelve months. “It's one of the world's worst recorded number of cancelled flights,” Coutinho added in his interpellation. The local lawmaker, who is mainly known for defending the “little people” also said that now locals rely “more and more” on foreign airlines to travel. “Considering that Macau has been an active member of the World Trade Organisation (WTO) for the past 12 years, it has the obligation to liberalise the market and all its monopolist economies,” he said. “Is the government ever going to review its monopolist market related to the Macau International Airport? When is the government planning on ending these concessions that lack transparency?” And why hasn't the government carried out an audit report into the way Air Macau is managed and run? These are questions to which Coutinho is now waiting for a reply on.\"   Destinations   Fleet  The Air Macau passenger fleet consists of the following aircraft (at March 2007) :   Cargo  Air Macau Cargo operates the following freighter aircraft to points in China and Taiwan.   Retired Fleet  2 Boeing 727-100F   Taiwan Strait  More than 70% of Air Macau's revenue comes from transporting passengers across the Taiwan Strait to Macau. Every week, Air Macau has 72 round-trip flights scheduled between Macau and Taipei and 28 round-trip flights scheduled between Macau and Kaohsiung.  Since Air Macau began its operation in 1995, no other airline has yet to offer a one-plane service for passengers traveling between Taiwan and Beijing, Shanghai, Xiamen, and other cities in the Chinese mainland. Although they must complete a brief transfer procedure in the Macau International Airport and wait for about 30 minutes with their carry-on luggage in the departure lobby, passengers can board the same plane once again and continue to their destination. The Air Macau staff are also stationed to assist Taiwanese passengers in obtaining their PRC Entry Endorsement.",
//								"Air Macau  Air Macau Company Limited (traditional Chinese: 澳門航空) is an airline based in Macau. It is the flag carrier of Macau, operating services to 12 destinations in mainland China, as well as regional international services. Its main base is Macau International Airport.   History  The airline was established on 13 September, 1994, and began commercial operations on 9 November, 1995, with a flight from Macau to Beijing and Shanghai (prior to 1995, there was no air service to Macau other than the helicopter service). The unique one-aircraft service between Beijing, Shanghai and Taipei began on 8 December, 1995. The first pure-freighter service was launched on 7 October, 2002, between Taipei and Shenzhen via Macau. Air Macau is owned by China National Aviation (CNAC) (51%), TAP Portugal (20%), STDM (14%), Eva Air (5%), the Government of Macau (5%) and Macau investors (5%) and employs 1,023 staff (at March 2007).  Recent reports have stated that Air Macau is on the cusp of bankruptcy. A recent article in the Macau Daily Times on July 9th states:  Air Macau crashes Wednesday, 09 July 2008 by Rodolfo Ascenso  \"Air Macau is close to bankruptcy. With losses up to MOP 100 million just within May and June, following another MOP 100 million during the first quarter of this year, chairman of the Board of Directors Zhao Xiaohang, had no choice but to ask for a meeting of shareholders “in order to have a resolution”. In a letter dated July 1, 2008 Zhao stated he, “with deep regret” is forced to inform the shareholders “that since April 30, 2008 the net worth of the company had fallen below half of the value of the company’s capital”, and had triggered the Macau Commercial Code Article 206. Article 206 of the Commercial Code states that when a company administration apprehend to have losses up to half of its capital, the body must propose that “the company be dissolved or that the capital be reduced, unless shareholders, within 60 days from the resolution that arises from such a proposal, pay amounts in money that replenish the assets to a measure equal to the value of the company capital”. Air Macau’s authorised capital is supposed to be MOP 400 million. However, a search in the Trade Register shows that the decision to upgrade from 200 million to 400 million was never registered. Nevertheless a decision in that direction was made and approved by shareholders and the capital realised. This question is important if the shareholders’ decision is carried out via the third option. With losses of MOP 220 million accumulated over the past years, as well as MOP 105 million during the first quarter of 2008, and more than MOP 100 million during the past two month, it is still not clear how much shareholder have to input. Nevertheless, it is already clear that the decision will be made by the main shareholder of Air Macau, Air China Limited. Owning 51 percent of the capital, Air China Limited has the final word as they hold the responsibility for the present situation. In fact, the actual board of directors, with David Fei as CEO, was named and supported by Air China Limited for the last year despite the accumulated losses. Macau Daily Times understands that other shareholders are not keen to inject more money into a company that has a record of losses in a region where business is flourishing. Thus, the future of Air Macau is in the hands of Air China Limited and it is still unclear if they wish to inject more money or just allow Air Macau to go into bankruptcy. Air Macau is owned by Air China Limited (51 percent), SEAP – an investment fund of Portuguese airline TAP – (20 percent), Stanley Ho’s STDM (14 percent), Evergreen Airways Service (Macau) (5 percent), Macau SAR Government (5 percent) and several others (5 percent). So far the shareholders’ meeting (EGM) requested by the chairman of the Board of Directors had not been appointed. An ordinary meeting of directors is scheduled for July 31.\"  Subsequent international reports have equally reported the current financial crisis as well as severe concerns over safety and operations and not just the impending doom of Air Macau. These reports have called for a dramatic change to Macau SAR's current and antiquated aviation regulatory situation. This has been led by Jose Pereira Coutinho who submitted a written interpellation to the Macau SAR Government. This was also reported in the Macau Daily Times:  \"Worrying situation: Air Macau's losses, government's lack of action  01 July 2008  Flagship airline, Air Macau, has recorded losses of up to 100 million patacas in the first quarter of this year, almost the same amount as the losses recorded during the whole of last year. This plus the fact that the government seems too “little concerned” about the situation is worrying lawmaker Jose Pereira Coutinho. In a written interpellation sent to the government yesterday, Coutinho questions the government's actions, or lack of them, for allowing a company to continue flying when services are constantly deteriorating with flight attendants barely speaking enough Cantonese or English, and most shockingly when the company has been recording continuous losses in the past four years. “While other local companies have recorder great profits, Air Macau has been accumulating losses, without anyone from the government having the courage to assess the causes of such losses,” Coutinho said. During the whole of last year, Air Macau recorded a loss of 109 million patacas, and when comparing its results for the past four years with that of other international airlines, Air Macau has blamed its losses on the hike in fuel price. “That doesn't stick,” Coutinho said, “we can't forget that the losses recorded in the last three years were mainly to do with the way in which the company was run.” For the past ten years Air Macau has cancelled flights to destinations such as Kuala Lumpur, Siem Reap, Singapore and Pusan, “but what is more worrying is that other low cost airlines have started flying to these destinations and have been successful,” Coutinho added. Last year alone, Air Macau cancelled some 3,000 flights, which is 20 per cent of its total flights held for the twelve months. “It's one of the world's worst recorded number of cancelled flights,” Coutinho added in his interpellation. The local lawmaker, who is mainly known for defending the “little people” also said that now locals rely “more and more” on foreign airlines to travel. “Considering that Macau has been an active member of the World Trade Organisation (WTO) for the past 12 years, it has the obligation to liberalise the market and all its monopolist economies,” he said. “Is the government ever going to review its monopolist market related to the Macau International Airport? When is the government planning on ending these concessions that lack transparency?” And why hasn't the government carried out an audit report into the way Air Macau is managed and run? These are questions to which Coutinho is now waiting for a reply on.\"   Destinations   Fleet  The Air Macau passenger fleet consists of the following aircraft (at March 2007) :   Cargo  Air Macau Cargo operates the following freighter aircraft to points in China and Taiwan.   Retired Fleet  2 Boeing 727-100F   Taiwan Strait  More than 70% of Air Macau's revenue comes from transporting passengers across the Taiwan Strait to Macau. Every week, Air Macau has 72 round-trip flights scheduled between Macau and Taipei and 28 round-trip flights scheduled between Macau and Kaohsiung.  Since Air Macau began its operation in 1995, no other airline has yet to offer a one-plane service for passengers traveling between Taiwan and Beijing, Shanghai, Xiamen, and other cities in the Chinese mainland. Although they must complete a brief transfer procedure in the Macau International Airport and wait for about 30 minutes with their carry-on luggage in the departure lobby, passengers can board the same plane once again and continue to their destination. The Air Macau staff are also stationed to assist Taiwanese passengers in obtaining their PRC Entry Endorsement."));
		// ,"Air (film)  Air is a 2005 Japanese animated film directed by Osamu Dezaki and written by Makoto Nakamura based on the visual novel of the same name by Key. Originally, the movie was set for a release date in autumn 2004, but was delayed; the movie finally premiered in Japanese theaters on February 5 2005. The film, animated by Toei Animation, is a reinterpretation of the original Air storyline which centers on the story arc of the female lead Misuzu Kamio. Yukito Kunisaki arrives in the town of Kami for a chance to earn money at the summer festival and meets Misuzu on his first day in town. They soon become friends and a story one thousand years old begins to unfold.  Before going to DVD, a thirty-minute sample of the film was streamed online by Animate between June 2 2005 and June 16 two weeks later. The film was later sold on DVD and released in three editions: the Collector's Edition, the Special Edition, and the Regular Edition on August 5 2005. The Air movie was licensed for English language distribution by ADV Films and was released on December 11 2007. The license of the movie was transferred to Funimation in July 2008 who will continue to release the movie in North America. To commemorate the release of the Clannad movie, Animate streamed the Air movie on their website which was split into three parts.   Plot  Yukito Kunisaki (Hikaru Midorikawa), a traveling puppeteer, arrives in a small sea-side town in the hopes of earning money at the upcoming summer festival. At the same time Misuzu Kamio (Tomoko Kawakami) is just leaving school after discussing her summer project with one of her teachers. Choosing to do a project on the history of the town, Misuzu finds a book containing the story of Kannabi no Mikoto (Chinami Nishimura), the inspiration for the upcoming festival. After crashing her bike and encountering Yukito on the beach, Misuzu invites Yukito to stay at her home until the festival begins after learning that he has no place to stay. After meeting Misuzu's eccentric aunt Haruko (Aya Hisakawa), and getting a hangover the next morning from drinking with her, Yukito accompanies Misuzu throughout the town as she does research for her project.  As the two become closer, the story of Kannabi no Mikoto, or Kanna for short, begins to unfold, telling how Kanna, the last of the winged beings, fell in love with her guardian Ryūya (Nobutoshi Canna) while being sequestered in a palace under penalty of death if she attempted to leave. As the two eventually become lovers, Kanna reveals her desire to escape and use her wings to fly to her mother, whom she was separated at birth from. Eventually, Ryūya decides to help Kanna see her dream and the two plot their escape.  In the present day, Misuzu's mysterious illness from her childhood resurfaces, leading Haruko to arrange for Misuzu's father to take her to a hospital where she can be treated. In a flashback, Kanna is seen with similar symptoms and tells Ryūya that the reason for her illness is punishment because she has fallen in love with him, which goes against the laws of her kind. Yukito becomes conflicted by both his feelings for Misuzu and his wish to continue wandering and leaves during the night. However, soon after he arrives at the bus stop, he remembers his real reason for coming to the town of Kami: to earn money; Yukito heads back into town for the festival. Meanwhile at the Kamio house, Haruko is preparing to take Misuzu to the festival when Misuzu's father arrives to take his daughter away. An emotional Haruko tells a shocked Misuzu that the reason she called her father is because Haruko cannot stand to see Misuzu becoming increasingly ill and wishes to be rid of her, but as Misuzu and her father leave Haruko is seen drinking and crying at the loss. While driving through the crowds at the festival, Misuzu suddenly leaves her father's car after seeing a float of Kannabi no Mikoto pass and prompts a panicked search by her father and Haruko.  During her search, Haruko finds Yukito as he his performing, and while he is at first unwilling, after recalling how he failed Misuzu in his past life as Ryūya, he joins in the search and frantically runs to the temple of Kannabi no Mikoto. Misuzu herself recalls her past life and her fateful escape from her confinement, remembering that both Ryūya and her mother died soon after she took flight; the former by a barrage of arrows in retaliation from the guards and the latter by leaving her prison to see her daughter after hearing her voice calling. Kanna herself was impaled by hundreds of arrows, but strangely never hit the ground and simply remained in the air. As the film concludes, Yukito arrives at the temple and confesses his love for Misuzu, and after reuniting with Haruko, the trio returns to the Kamio residence.  A short time later Haruko and Yukito decide to send Misuzu to a hospital in order to treat her, cutting her hair before she leaves and taking her to the ocean as per her request. At the ocean, a weakened Misuzu gets up and tries to reach Haruko and Yukito, the two most important people in her life. She finally reaches them only to collapse in Yukito's arms and die having finally reached her goal. Yukito is last seen leaving town in the autumn and promising to find Misuzu wherever she appears next in the hope that he will someday be able to break her curse and let her be free.   TV and film differences  Being done by two different production teams and having their own take on the story from the Air visual novel, there are many differences between the Air TV series and film. However, the TV series follows the original visual novel's storyline quite closely while the movie makes several tangents, especially the explicit romance between Yukito and Misuzu. Yukito's general attitude is considerably more cynical and gloomy than his TV counterpart. This version of Yukito is drawn and voiced (by a different voice actor) in such a way that he appears older. Yukito's puppet business is more successful in the film, while in the TV he had a hard time pleasing the children. It is never explicitly stated in the TV series that Yukito is a reincarnation of Ryūya, but in the film it is clear he remembers his past with Kanna and believes he failed her in her escape. The crow Sora is actually Yukito in the TV series, while the one who briefly appears in the movie is merely a friend of Misuzu's who does not have any apparent connection to Yukito.  Haruko and Misuzu have a much closer relationship than throughout most of the TV adaptation, and Haruko treats Misuzu as her daughter while in the TV series the two rarely interacted with each other unless absolutely necessary. Misuzu only says her famous \"gao\" line once in the film while she and Yukito are on the beach, and in general the film depiction of Misuzu appears more mature and able to get along with her classmates with more ease.  Uraha is only seen as a background character in the film and does not appear to have any sort of romantic relationship with Ryūya. Similarly, Ryūya and Kanna were actually shown to be lovers; in the TV series Kanna never acted on her feelings for Ryūya until the end of her life. Kanna is not cursed by Buddhist monks as she is in the TV series; instead she simply dies in midair and never returns to the ground. Since Ryūya dies in the film during Kanna's escape, it is unclear how he and Yukito are genetically related, if at all. It is more likely that Yukito is the reincarnation of Ryūya.  Kano Kirishima and Minagi Tohno appear only as background characters during the festival along with Michiru, who is among the group of children Yukito is entertaining in an early part of the film. The last scene is quite similar to the one in the TV series, with the exception of Yukito's presence.   Media releases   DVDs  The original version of the film was later sold on DVD and released in three editions: the Collector's Edition, the Special Edition, and the Regular Edition on August 5 2005. The Collector's Edition, retailing for 9,500 yen (~$US79.48), was sold as a specialized box set including, with the movie DVD, a separate DVD containing four promotional images and four television commercials advertising the movie. A 402-page booklet was included in the box set containing detailed storyboards, and a draft of the movie's scenario. The Special Edition, also retailing for 9,500 yen, was similarly released in a box set containing the movie DVD, along with a sixty-one minute drama CD containing twelve tracks, and a forty-minute full orchestra entitled Shinwa e no Izanai CD featuring four songs in the movie. The Regular Edition, retailing for 6,800 yen (~US$56.87), contained no special features and was sold in a normal DVD case containing only the movie DVD. All prices are before tax. The English language version of the film was released by ADV Films on December 11 2007. In July 2008, the license of the movie was transferred to Funimation Entertainment who will continue to release the movie in North America.   Soundtrack  The original soundtrack entitled Air Movie Soundtrack was first released on March 25 2005 by Frontier Works. The soundtrack contained one disc with twenty- three tracks. The first twenty-two tracks are the background music played throughout the movie composed by Japanese composer Yoshikazu Suo. The final track on the CD, \"If Dreams Came True\", is a song based on the song \"Two people\" (ふたり, Futari) on the Air Original Soundtrack for the original visual novel; \"If Dreams Came True\" is sung by Japanese singer Eri Kawai. "));
		CountTextSimilarity.getInstance().calcCandidateTextSim();
		CountTextSimilarity.getInstance().close();
	}

}