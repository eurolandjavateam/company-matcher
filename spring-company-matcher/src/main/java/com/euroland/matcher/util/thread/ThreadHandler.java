package com.euroland.matcher.util.thread;

import org.springframework.stereotype.Component;

@Component
public class ThreadHandler {
	
	public static void sleep(int s) {
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
