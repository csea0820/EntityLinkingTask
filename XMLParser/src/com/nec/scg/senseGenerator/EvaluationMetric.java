package com.nec.scg.senseGenerator;
/**
 * @Author Xiaofeng
 * @Date 2013-5-30 上午10:46:05
 */

public class EvaluationMetric {

	int m_all_relevant_pages;  //所有的相关文件数 
	int m_returned_relevant_pages; //系统返回的相关文件数
	int m_all_returned_pages;    //系统返回的所有文件数
	
	
	
	EvaluationMetric()
	{
		m_all_relevant_pages = 0;
		m_returned_relevant_pages = 0;
		m_all_returned_pages = 0;
	}
	
	public EvaluationMetric(int m_all_relevant_pages, int m_returned_relevant_pages,
			int m_all_returned_pages) {
		super();
		this.m_all_relevant_pages = m_all_relevant_pages;
		this.m_returned_relevant_pages = m_returned_relevant_pages;
		this.m_all_returned_pages = m_all_returned_pages;
	}
	
	public void allRelevantPagesPlus(int pagesCount)
	{
		m_all_relevant_pages += pagesCount;
	}
	
	public void returnedRelevantPagesPlus()
	{
		m_returned_relevant_pages++;
	}
	
	public void allReturnedPagesPlus(int pagesCount)
	{
		m_all_returned_pages += pagesCount;
	}
	
	public double recall()
	{
		return getM_returned_relevant_pages()*1.0/getM_all_relevant_pages();
	}
	
	
	public double precision()
	{
		return getM_returned_relevant_pages()*1.0/getM_all_returned_pages();
	}



	public int getM_all_relevant_pages() {
		return m_all_relevant_pages;
	}



	public void setM_all_relevant_pages(int m_all_relevant_pages) {
		this.m_all_relevant_pages = m_all_relevant_pages;
	}



	public int getM_returned_relevant_pages() {
		return m_returned_relevant_pages;
	}



	public void setM_returned_relevant_pages(int m_returned_relevant_pages) {
		this.m_returned_relevant_pages = m_returned_relevant_pages;
	}



	public int getM_all_returned_pages() {
		return m_all_returned_pages;
	}



	public void setM_all_returned_pages(int m_all_returned_pages) {
		this.m_all_returned_pages = m_all_returned_pages;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
