/**
 * 
 */
package org.rifidi.edge.notification;

import java.math.BigInteger;

/**
 * 
 * 
 * @author rifidi
 */
public class RSSITagReadEvent {

	private String tagID;
	private String readerID;
	private Double avgRSSI;
	private Double tagCount;

	/**
	 * 
	 */
	public RSSITagReadEvent(String tagID, String readerID, Double avgRSSI,
			Double tagCount) {
		this.tagID = tagID;
		this.readerID = readerID;
		this.avgRSSI = avgRSSI;
		this.tagCount = tagCount;
	}

	public String getTagID() {
		return tagID;
	}

	public void setTagID(String tagID) {
		this.tagID = tagID;
	}

	public String getReaderID() {
		return readerID;
	}

	public void setReaderID(String readerID) {
		this.readerID = readerID;
	}

	public Double getAvgRSSI() {
		return avgRSSI;
	}

	public void setAvgRSSI(Double avgRSSI) {
		this.avgRSSI = avgRSSI;
	}

	public void setTagCount(Double tagCount) {
		this.tagCount = tagCount;
	}

	public Double getTagCount() {
		return this.tagCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RSSITagReadEvent tag: " + this.tagID);
		builder.append(", reader: " + this.readerID);
		builder.append(", avg: " + this.avgRSSI);
		builder.append(", sum: " + this.tagCount);
		return builder.toString();
	}

	public String getCombinedReaderTagID() {
		return tagID + "" + readerID;
	}
}