package com.euroland.matcher.model;

public class MatchData {
	
	private String sourceName;
	private String eurolandCode;
	private String crawledName;
	private String eurolandName;
	private String crawledSymbol;
	private String eurolandSymbol;
	private String crawledIsin;
	private String eurolandIsin;
	private Boolean isMatched;
	private Boolean isConfirmed;
	
	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getEurolandCode() {
		return eurolandCode;
	}
	
	public void setEurolandCode(String eurolandCode) {
		this.eurolandCode = eurolandCode;
	}
	
	public String getCrawledName() {
		return crawledName;
	}
	
	public void setCrawledName(String crawledName) {
		this.crawledName = crawledName;
	}
	
	public String getEurolandName() {
		return eurolandName;
	}

	public void setEurolandName(String eurolandName) {
		this.eurolandName = eurolandName;
	}

	public String getCrawledSymbol() {
		return crawledSymbol;
	}

	public void setCrawledSymbol(String crawledSymbol) {
		this.crawledSymbol = crawledSymbol;
	}

	public String getEurolandSymbol() {
		return eurolandSymbol;
	}

	public void setEurolandSymbol(String eurolandSymbol) {
		this.eurolandSymbol = eurolandSymbol;
	}

	public Boolean getIsMatched() {
		return isMatched;
	}
	
	public void setIsMatched(Boolean isMatched) {
		this.isMatched = isMatched;
	}
	
	public Boolean getIsConfirmed() {
		return isConfirmed;
	}
	
	public void setIsConfirmed(Boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}
	
	public String getCrawledIsin() {
		return crawledIsin;
	}

	public void setCrawledIsin(String crawledIsin) {
		this.crawledIsin = crawledIsin;
	}

	public String getEurolandIsin() {
		return eurolandIsin;
	}

	public void setEurolandIsin(String eurolandIsin) {
		this.eurolandIsin = eurolandIsin;
	}
	
	public MatchData(String sourceName, String eurolandCode, String crawledName, String eurolandName,
			String crawledSymbol, String eurolandSymbol, String crawledIsin, String eurolandIsin, Boolean isMatched,
			Boolean isConfirmed) {
		super();
		this.sourceName = sourceName;
		this.eurolandCode = eurolandCode;
		this.crawledName = crawledName;
		this.eurolandName = eurolandName;
		this.crawledSymbol = crawledSymbol;
		this.eurolandSymbol = eurolandSymbol;
		this.crawledIsin = crawledIsin;
		this.eurolandIsin = eurolandIsin;
		this.isMatched = isMatched;
		this.isConfirmed = isConfirmed;
	}

	public MatchData() {
		super();
	}

	@Override
	public String toString() {
		return "EurolandMatchData [sourceName=" + sourceName + ", eurolandCode=" + eurolandCode + ", crawledName="
				+ crawledName + ", eurolandName=" + eurolandName + ", crawledSymbol=" + crawledSymbol
				+ ", eurolandSymbol=" + eurolandSymbol + ", crawledIsin=" + crawledIsin + ", eurolandIsin="
				+ eurolandIsin + ", isMatched=" + isMatched + ", isConfirmed=" + isConfirmed + "]";
	}
}
