/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
package org.rifidi.app.amazonsample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.rifidi.edge.api.service.tagmonitor.ReadZoneSubscriber;
import org.rifidi.edge.notification.TagReadEvent;

import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;

/**
 * 
 * 
 * @author Matthew Dean - matt@transcends.co
 */
public class DynamoDBSubscriber implements ReadZoneSubscriber {

	public AmazonDynamoDBClient dynamoDB;

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	
	public String tableName;

	/**
	 * 
	 * @param dynamoDB
	 */
	public DynamoDBSubscriber(AmazonDynamoDBClient dynamoDB, String tablename) {
		this.dynamoDB = dynamoDB;
		this.tableName = tablename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.api.service.tagmonitor.ReadZoneSubscriber#tagArrived(
	 * org.rifidi.edge.notification.TagReadEvent)
	 */
	@Override
	public void tagArrived(TagReadEvent tag) {
		Map<String, AttributeValue> item = newItem(tag.getTag()
				.getFormattedID(), tag.getAntennaID(), tag.getReaderID(),
				tag.getTimestamp());
		PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
		PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
		System.out.println("Result: " + putItemResult);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.api.service.tagmonitor.ReadZoneSubscriber#tagDeparted
	 * (org.rifidi.edge.notification.TagReadEvent)
	 */
	@Override
	public void tagDeparted(TagReadEvent tag) {

	}

	private static Map<String, AttributeValue> newItem(String epc, int antenna,
			String readerid, long timestamp) {
		Date date = new Date(timestamp);
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("epc", new AttributeValue(epc));
		item.put("antenna",
				new AttributeValue().withN(Integer.toString(antenna)));
		item.put("reader", new AttributeValue(readerid));
		item.put("timestamp",
				new AttributeValue(FORMATTER.format(date)));

		return item;
	}
}
