package com.euroland.matcher.model;

public class CrawlSource {
	private String websiteName;
	private String websiteRegion;
	
	public String getWebsiteName() {
		return websiteName;
	}

	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}

	public String getWebsiteRegion() {
		return websiteRegion;
	}

	public void setWebsiteRegion(String websiteRegion) {
		this.websiteRegion = websiteRegion;
	}

	public CrawlSource(String websiteName, String websiteRegion) {
		super();
		this.websiteName = websiteName;
		this.websiteRegion = websiteRegion;
	}

	public CrawlSource() {
		super();
	}
}
