package com.euroland.matcher.model;

public class CrawlData {

	private String sourceName;
	private String company;
	private String symbol;
	private String isin;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public CrawlData(String sourceName, String company, String symbol, String isin) {
		super();
		this.sourceName = sourceName;
		this.company = company;
		this.symbol = symbol;
		this.isin = isin;
	}

	public CrawlData() {
		super();
	}

	@Override
	public String toString() {
		return "CrawlData [sourceName=" + sourceName + ", company=" + company + ", symbol=" + symbol + ", isin=" + isin
				+ "]";
	}
	
	
}
