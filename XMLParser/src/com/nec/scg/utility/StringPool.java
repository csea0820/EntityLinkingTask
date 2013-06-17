package com.nec.scg.utility;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author Xiaofeng
 * @Date 2013-6-3 обнГ2:42:01
 */

public class StringPool {
	private static StringPool pool = null;
	private Map<String, String> map = new TreeMap<String, String>();

	private StringPool()
	{
		
	}
	
	public static StringPool getInstance()
	{
		if (pool == null)
			pool = new StringPool();
		return pool;
	}
	
	
	public String getCanonicalVersion(String str) {
		
		String canon = map.get(str);
		if (canon != null)
			return canon;
		else{
			map.put(str, str);
			return str;
		}
	}
}
