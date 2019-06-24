package com.euroland.matcher.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggerHandler {

	
	public void info(String m) {
		
		Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.info(m);
	}
	
	public void error(String m) {

		Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.error(m);
	}

	public void debug(String m) {
		
		Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.debug(m);
	}
}
