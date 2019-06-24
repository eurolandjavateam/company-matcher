package com.euroland.matcher.configuration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

//import com.euroland.logger.LoggerHandler;
import com.google.gson.Gson;

@Service
public class ConfService {
	
	@Value("${crawler.host}")
	public String HOST;
	@Value("${matcher.rabbit.exchange}")
	public String RABBIT_EXCHANGE;
	@Value("${matcher.rabbit.routingkey}")
	public String RABBIT_ROUTING_KEY;
	@Value("${euroland.company.data}")
	public String EUROLAND_COMPANY_DATA;
	@Value("${company.match.data}")
	public String COMPANY_MATCH_DATA;
	@Value("${euroland.crawl.data}")
	public String EUROLAND_CRAWL_DATA;
	@Value("${euroland.crawl.source}")
	public String EUROLAND_CRAWL_SOURCE;


	@Autowired
	public RestTemplate restTemplate;

//	@Autowired
//	LoggerHandler logger;
	
	public Object prepareTestConf(String path, Object obj) {
		
		String json = null;
		try {
			if(path.contains("http")) {
				ResponseEntity<? extends Object> response = restTemplate.getForEntity(path, obj.getClass());
				return response.getBody();
			} else {
				json = getStrFromFile(path);
			}
		} catch (Exception e) {
			
//			logger.error("Failed Loading Config: " + obj.getClass());
			
			return null;
		}
		
		obj = new Gson().fromJson(json, obj.getClass());
		
		return obj;
	}
	
	/**
	 * read text file into string from given file+path
	 * 
	 * @param pathname
	 * @return String
	 * @throws IOException
	 */
	public String getStrFromFile(String pathname) throws IOException {
		FileInputStream fis = new FileInputStream(pathname);
		StringBuilder sb = new StringBuilder();
		Reader r = new InputStreamReader(fis, "utf-8");
		char[] buf = new char[1024];
		int amt = r.read(buf);
		
		while (amt > 0) {
			sb.append(buf, 0, amt);
			amt = r.read(buf);
		}
		fis.close();
		return sb.toString();
	}
	
	public String readUrl(String urlString) throws Exception {

	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer(); 
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	        
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}

}
