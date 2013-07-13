package com.nec.scg.utility;

import java.text.SimpleDateFormat;

public class Timer {
	
	long startTime = 0,endTime = 0;
	
	
	public void start()
	{
		startTime = System.currentTimeMillis();
	}
	
	public void end()
	{
		endTime = System.currentTimeMillis();
	}
	
	public void timeElapse(String content)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("mm·Ös");
		System.out.println("Time elapse of "+content+":"+sdf.format((endTime-startTime)));
	}

}
