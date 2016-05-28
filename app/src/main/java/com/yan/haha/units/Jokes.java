package com.yan.haha.units;


public class Jokes {

	private String title;
	private String pubDate;
	private String body;

	public Jokes(String title, String pubDate, String body) {
		super();
		this.title = title;
		this.pubDate = pubDate;
		this.body = body;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
