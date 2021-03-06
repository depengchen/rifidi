package org.rifidi.edge.adapter.generic.dtos;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */

/**
 * @author matt
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tag")
public class GenericTagDTO implements Serializable {
	
	private static final long serialVersionUID = -3035982716629339545L;

	//The ID of the tag
	@XmlElement(name = "epc")
	private String epc;
	
	//The antenna of the tag
	@XmlElement(name = "antenna")
	private Integer antenna;
	
	@XmlElement(name = "timestamp")
	private Long timestamp;
	
	@XmlElement(name = "reader")
	private String reader;
	
	@XmlElement(name = "rssi")
	private String rssi;
	
	@XmlElement(name = "extrainformation")
	private String extrainformation;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getReader() {
		return reader;
	}

	public void setReader(String reader) {
		this.reader = reader;
	}

	public String getEpc() {
		return epc;
	}

	public void setEpc(String epc) {
		this.epc = epc;
	}

	public Integer getAntenna() {
		return antenna;
	}

	public void setAntenna(Integer antenna) {
		this.antenna = antenna;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}

	public String getExtrainformation() {
		return extrainformation;
	}

	public void setExtrainformation(String extrainformation) {
		this.extrainformation = extrainformation;
	}
	
}
