package com.euroland.matcher.model;

public class EurolandData {

	private String eurolandcode;
	private String name;
	private String ticker;
	private String isin;
	private String market;
	
	public EurolandData(String eurolandcode, String name, String ticker, String isin, String market) {
		super();
		this.eurolandcode = eurolandcode;
		this.name = name;
		this.ticker = ticker;
		this.isin = isin;
		this.market = market;
	}

	public EurolandData() {
		super();
	}
	public String getEurolandcode() {
		return eurolandcode;
	}
	public void setEurolandcode(String eurolandcode) {
		this.eurolandcode = eurolandcode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public String getIsin() {
		return isin;
	}
	public void setIsin(String isin) {
		this.isin = isin;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}
	
	@Override
	public String toString() {
		return "[ Euroland Code: " + eurolandcode + 
				", Company Name: " + name + 
				", Ticker: " + ticker + 
				", ISIN: " + isin + 
				", Market: " + market + 
				" ]\n";
	}
}
