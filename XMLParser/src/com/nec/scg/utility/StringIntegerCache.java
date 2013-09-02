/**
 * @Author Xiaofeng
 * @Date 2013-9-2 ÏÂÎç3:11:25
 */
package com.nec.scg.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class StringIntegerCache implements ICache {

	private String CACHE_FILE = null;
	
	private Map<String,Integer> cache = null;
	
	
	public StringIntegerCache(String cache_file){
		this.CACHE_FILE = cache_file;
		cache = new TreeMap<String,Integer>();
	}
	
	/* (non-Javadoc)
	 * @see com.nec.scg.utility.ICache#readCache()
	 */
	@Override
	public void readCache() {
		File file = new File(CACHE_FILE);
		
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
			}finally{
				try {
					fr.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean containsKey(String key){
		return cache.containsKey(key);
	}
	
	public Integer get(String key){
		return cache.get(key);
	}
	
	public void put(String key,Integer value){
		cache.put(key, value);
	}

	/* (non-Javadoc)
	 * @see com.nec.scg.utility.ICache#saveCache()
	 */
	@Override
	public void saveCache() {
		StringBuilder sb = new StringBuilder();
		for (String key : cache.keySet()) {
			Integer e = cache.get(key);
			sb.append(key).append("\t").append(e).append("\n");
		}

		Utility.writeToFile(CACHE_FILE, sb.toString());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
