package com.euroland.matcher.service;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.euroland.matcher.configuration.ConfService;
import com.euroland.matcher.logger.LoggerHandler;
import com.euroland.matcher.model.CrawlData;
import com.euroland.matcher.model.CrawlSource;
import com.euroland.matcher.model.EurolandData;
import com.euroland.matcher.model.MatchData;
import com.euroland.matcher.rabbit.Producer;

@Service
public class MatcherService {

	@Autowired
	ConfService confService;
	
	@Autowired
	Producer producer;
	
	@Autowired
	LoggerHandler logger;
	
	public void processMatching() {
		try { 
			logger.info("Load Crawled Data");
			List<CrawlData> lcd = confService.restTemplate.exchange(
					confService.HOST + confService.EUROLAND_CRAWL_DATA, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<CrawlData>>() {}).getBody();
			lcd.sort(Comparator.comparing(CrawlData::getSourceName));
			logger.info("Crawled Data: " + lcd.size());
			
			logger.info("Load Match Data");
			List<MatchData> lmd = confService.restTemplate.exchange(
					confService.HOST + confService.COMPANY_MATCH_DATA, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<MatchData>>() {}).getBody();
			logger.info("Match Data: " + lmd.size());

			logger.info("Load Euroland Data");
			List<EurolandData> ed = confService.restTemplate.exchange(
					confService.HOST + confService.EUROLAND_COMPANY_DATA, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<EurolandData>>() {}).getBody();
			logger.info("Euroland Data: " + ed.size());
			
			logger.info("Load Source");
			List<CrawlSource> lc = confService.restTemplate.exchange(
					confService.HOST + confService.EUROLAND_CRAWL_SOURCE, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<CrawlSource>>() {}).getBody();
			lc.removeIf(f -> f.getWebsiteRegion()==null);
			logger.info("Source with Market: " + lc.size());
			
			if(lmd.size() != 0) {
				logger.info("Removing Data in Crawled Data that is already Confirmed with Match");
				lcd.removeIf(cd -> checkWithMatchData(lmd, cd));
				logger.info("Updated Crawled Data: " + lcd.size());
			}

			logger.info("Process Matching");
			lcd.parallelStream().forEach(cd -> {
				
				List<EurolandData> d = new ArrayList<>();

				CrawlSource source = lc.stream().filter(c -> c.getWebsiteName().equals(cd.getSourceName())).findAny().orElse(null);
				if(source != null && source.getWebsiteRegion() != null) {
					d = getDataByMarket(source.getWebsiteRegion(), ed);
				} else {
					d = ed;
				}
				
	 			MatchData md = matchCompanyData(cd, d);
	
				if(md != null) {
					logger.info(md.getEurolandCode() + " - " + 
					md.getCrawledName() + " - " + md.getEurolandName() + " - " + 
					md.getCrawledSymbol() + " - " + md.getEurolandSymbol());
					producer.produce(md);
				}
			});
			logger.info("Done Process Matching");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private boolean checkWithMatchData(List<MatchData> lmd, CrawlData cd) {
		return lmd.stream().anyMatch(m -> {
						if (m.getSourceName().equals(cd.getSourceName())) {
							if(cd.getIsin()!=null && !cd.getIsin().equals("") &&
								m.getCrawledIsin()!=null && !m.getCrawledIsin().equals("") &&
								m.getCrawledIsin().equalsIgnoreCase(cd.getIsin())) {
								return true;
							}

							if(cd.getSymbol()!=null && !cd.getSymbol().equals("") &&
									m.getCrawledSymbol().equalsIgnoreCase(cd.getSymbol())) {

								
								if(cd.getCompany()!=null && !cd.getCompany().equals("")) {
									if(m.getCrawledName().equalsIgnoreCase(cd.getCompany())) {
										return true;
									} else {
										return false; 
									}
								} else {
									return true;
								}
							}

							if(cd.getCompany()!=null && !cd.getCompany().equals("") &&
								m.getCrawledName().equalsIgnoreCase(cd.getCompany())) {
								return true;
							}
						}
						return false;
					});
	}
	
	// filtering the list of euroland data by market
	private List<EurolandData> getDataByMarket(String market, List<EurolandData> ed) {
		
		List<EurolandData> result = ed;
		
		if(market != null && !market.equals("")) {
			if(market.contains(",")) {
				List<String> s = Arrays.asList(market.split(","));
				
				result = ed.stream().filter(e -> {
					if(s.stream().anyMatch(p -> p.contains("!"))) {
						return !s.stream().anyMatch(a -> {
							a = a.replace("!", "");
							return a.equalsIgnoreCase(e.getMarket());
						});
					}

					return s.contains(e.getMarket());
				}).collect(Collectors.toList());
				
				
			} else {
				if(market.contains("!")) {
					result = ed.stream().filter(p -> !p.getMarket().equals(market.replace("!", ""))).collect(Collectors.toList());
				} else {
					result = ed.stream().filter(p -> p.getMarket().equals(market)).collect(Collectors.toList());
				}
			}
		}
		return result;
	}
	
	private MatchData matchCompanyData(CrawlData cd, List<EurolandData> ed) {
		
		List<MatchData> ml = new ArrayList<>();
		List<EurolandData> el = new ArrayList<>();

		MatchData em = new MatchData();
		em.setSourceName(cd.getSourceName());
		em.setEurolandCode("");
		em.setCrawledName("");
		em.setEurolandName("");
		em.setCrawledSymbol("");
		em.setEurolandSymbol("");
		em.setCrawledIsin("");
		em.setEurolandIsin("");
		em.setIsConfirmed(false);
		
		// Checking VIA ISIN
		if(cd.getIsin() != null && !cd.getIsin().equals("")) {
			EurolandData data = ed.stream().filter(d -> 
					d.getIsin()!= null && !d.getIsin().equals("") &&
					cd.getIsin().equals(d.getIsin())
				).findAny().orElse(null);
			if(data != null) {
				if(cd.getCompany() != null && !cd.getCompany().equals(""))
					em.setCrawledName(cd.getCompany());
				
				if(cd.getSymbol() != null && !cd.getSymbol().equals(""))
					em.setCrawledSymbol(cd.getSymbol());
				
				if(cd.getIsin() != null && !cd.getIsin().equals(""))
					em.setCrawledIsin(cd.getIsin());
				em.setEurolandCode(data.getEurolandcode());
				em.setEurolandName(data.getName());
				em.setEurolandSymbol(data.getTicker());
				em.setEurolandIsin(data.getIsin());
				em.setIsMatched(true);
				em.setIsConfirmed(true);
				return em;
			}
		}
		
		// Checking VIA Symbol
		if(cd.getSymbol() != null && !cd.getSymbol().equals("") && el.size() == 0) {
			List<EurolandData> data = ed.stream().filter(d -> {
					if (d != null && cd.getSymbol().matches("[0-9]+")) {
						if(!rmChars(d.getTicker()).matches("[0-9]+")) {
							return false;
						} else {
							return String.valueOf(Integer.parseInt(cd.getSymbol())).equals(rmChars(d.getTicker()));
						}
					}
					return rmChars(cd.getSymbol()).equals(rmChars(d.getTicker()));
				}).collect(Collectors.toList());
			if(cd.getCompany() != null && !cd.getCompany().equals("") && data.size() != 0) {
				HashMap<EurolandData, Integer> ei = new HashMap<>();
				
				
				if(data.size() > 1) {
					EurolandData d = data.stream().min(Comparator.comparing(a -> {
						int dif = getLevenshteinDistance(rmChars(a.getName()),rmChars(cd.getCompany()));
						ei.put(a, dif);
						return dif;
					})).orElse(null);
					
					if(d != null && ((rmChars(cd.getCompany()).length()*.50 > ei.get(d)) ||
							(Pattern.compile("^" + rmChars(d.getName())).matcher(rmChars(cd.getCompany())).find() ||
									Pattern.compile("^" + rmChars(cd.getCompany())).matcher(rmChars(d.getName())).find()))) {
						el.add(d);
					}
				} else {
					if(containsHanScript(cd.getCompany()) || (data.get(0) != null && ((rmChars(cd.getCompany()).length()*.50 > 
						getLevenshteinDistance(rmChars(data.get(0).getName()),rmChars(cd.getCompany()))) ||
						(Pattern.compile("^" + rmChars(data.get(0).getName())).matcher(rmChars(cd.getCompany())).find() ||
								Pattern.compile("^" + rmChars(cd.getCompany())).matcher(rmChars(data.get(0).getName())).find())))) {
						el.add(data.get(0));
					} 
				}
			} else if(data.size() != 0) {
				el.addAll(data);
			}
		}
		
		// Checking VIA Company Name
		if(cd.getCompany() != null && !cd.getCompany().equals("") && el.size() == 0) {
			List<EurolandData> data = ed.stream().filter(d -> 
			Pattern.compile("^" + rmChars(d.getName())).matcher(rmChars(cd.getCompany())).find() ||
			Pattern.compile("^" + rmChars(cd.getCompany())).matcher(rmChars(d.getName())).find()).collect(Collectors.toList());
			if(data.size() == 1) {
				el.add(data.get(0));
			} else if (data.size() > 1) {
				HashMap<EurolandData, Integer> ei = new HashMap<>();
				data.stream().forEach(i -> {
					int in = Math.abs(rmChars(i.getName()).length() - rmChars(cd.getCompany()).length());
					ei.put(i, in);
				});
				int min = ei.entrySet().stream().min(Comparator.comparing(c -> c.getValue())).get().getValue();
				ei.entrySet().stream().forEach(i -> {
					if(i.getValue().equals(min)) {
						el.add(i.getKey());
					}
				});
			}
		}

		// Processing Match Data
		if(el.size() != 0) {
			el.stream().filter(distinctByKey(EurolandData::getEurolandcode)).forEach(a -> {
				if(ml.size() == 0) {
					
					if(cd.getCompany() != null)
						em.setCrawledName(cd.getCompany());
					
					if(cd.getSymbol() != null)
						em.setCrawledSymbol(cd.getSymbol());
					
					if(cd.getIsin() != null)
						em.setCrawledIsin(cd.getIsin());

					em.setEurolandCode(a.getEurolandcode());
					em.setEurolandName(a.getName());
					em.setEurolandSymbol(a.getTicker());
					em.setEurolandIsin(a.getIsin());
					em.setIsMatched(true);

					ml.add(em);
				} else {
					
					ml.get(0).setEurolandCode(
							ml.get(0).getEurolandCode() + "(@)" + a.getEurolandcode());
					ml.get(0).setEurolandName(
							ml.get(0).getEurolandName() + "(@)" + a.getName());
					ml.get(0).setEurolandSymbol(
							ml.get(0).getEurolandSymbol() + "(@)" + a.getTicker());
					ml.get(0).setEurolandIsin(
							ml.get(0).getEurolandIsin() + "(@)" + a.getIsin());
				}
			});
			
		} else {
			
			if(cd.getCompany() != null && !cd.getCompany().equals(""))
				em.setCrawledName(cd.getCompany());
			
			if(cd.getSymbol() != null && !cd.getSymbol().equals(""))
				em.setCrawledSymbol(cd.getSymbol());
			
			if(cd.getIsin() != null && !cd.getIsin().equals(""))
				em.setCrawledIsin(cd.getIsin());

			em.setIsMatched(false);
			ml.add(em);
			
		}
//		System.out.println("=======================");
//		System.out.println("Source Name: " + ml.get(0).getSourceName());
//		System.out.println("E Code: " + ml.get(0).getEurolandCode());
//		System.out.println("C Name: " + ml.get(0).getCrawledName());
//		System.out.println("E Name: " + ml.get(0).getEurolandName());
//		System.out.println("C Symbol: " + ml.get(0).getCrawledSymbol());
//		System.out.println("E Symbol: " + ml.get(0).getEurolandSymbol());
//		System.out.println("C Isin: " + ml.get(0).getCrawledIsin());
//		System.out.println("E Isin: " + ml.get(0).getEurolandIsin());
//		System.out.println("Matched: " + ml.get(0).getIsMatched());
//		System.out.println("=======================");
		return ml.get(0);
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    final Set<Object> seen = new HashSet<>();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
	private String rmChars(String s) {

		String result = s.toLowerCase()
				.replaceAll("æ", "ae")
				.replaceAll("œ", "oe");
		if(!containsHanScript(result) && !containsJapaneseScript(result)) {
			result = Normalizer.normalize(result, Form.NFD)
					.replaceAll("[^\\p{ASCII}]", "");
		}
		result = result.replaceAll("['-.´`]", " ")
				.replaceAll("&", " and ")
				.trim().replaceAll("\\s{2,}", " ");
		return result;
	}
	
	public static boolean containsHanScript(String s) {
	    return s.codePoints().anyMatch(
	            codepoint ->
	            Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
	}
	
	public static boolean containsJapaneseScript(String s) {
	    return s.codePoints().anyMatch(
	            codepoint ->
	            (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.KATAKANA) || (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HIRAGANA));
	}
	
	public static int getLevenshteinDistance(String s, String t) {
	      if (s == null || t == null) {
	          throw new IllegalArgumentException("Strings must not be null");
	      }

	      /*
	         The difference between this impl. and the previous is that, rather 
	         than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
	         we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
	         is the 'current working' distance array that maintains the newest distance cost
	         counts as we iterate through the characters of String s.  Each time we increment
	         the index of String t we are comparing, d is copied to p, the second int[].  Doing so
	         allows us to retain the previous cost counts as required by the algorithm (taking 
	         the minimum of the cost count to the left, up one, and diagonally up and to the left
	         of the current cost count being calculated).  (Note that the arrays aren't really 
	         copied anymore, just switched...this is clearly much better than cloning an array 
	         or doing a System.arraycopy() each time  through the outer loop.)

	         Effectively, the difference between the two implementations is this one does not 
	         cause an out of memory condition when calculating the LD over two very large strings.
	       */
	      int n = s.length(); // length of s
	      int m = t.length(); // length of t

	      if (n == 0) {
	          return m;
	      } else if (m == 0) {
	          return n;
	      }

	      if (n > m) {
	          // swap the input strings to consume less memory
	          String tmp = s;
	          s = t;
	          t = tmp;
	          n = m;
	          m = t.length();
	      }

	      final List<Integer> p = new ArrayList<Integer>();
	      final List<Integer> d = new ArrayList<Integer>();
	      
	      IntStream.range(0, n + 1).forEach(i -> p.add(i, i));
	      
	      final int n_final = n;
	      final String s_final = s;
	      final String t_final = t;
	      final List<Integer> cost = new ArrayList<>();
	      final List<String> t_j = new ArrayList<>();
	      
	      IntStream.range(1, m + 1).forEach(j -> {
	    	  
	    	  t_j.add(0, String.valueOf(t_final.charAt(j-1)));
	    	  d.add(0, j);
	    	  IntStream.range(1, n_final + 1).forEach(i -> {
	    		  cost.add(i-1, s_final.charAt(i-1)==t_j.get(0).charAt(0) ? 0 : 1);
	    		  // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
	    		  d.add(i, Math.min(Math.min(d.get(i-1)+1, p.get(i)+1),  p.get(i-1)+cost.get(i-1)));
	    		  
		      });
	    	  cost.clear();
	    	  t_j.clear();
	    	  
	    	  // copy current distance counts to 'previous row' distance counts
	    	  List<Integer> _d = p;
	    	  p.clear();
	    	  p.addAll(d);
	    	  d.clear();
	          d.addAll(_d);
	      });

	      
	      // our last action in the above loop was to switch d and p, so p now 
	      // actually has the most recent cost counts
	      return p.get(n);
	  }
}
