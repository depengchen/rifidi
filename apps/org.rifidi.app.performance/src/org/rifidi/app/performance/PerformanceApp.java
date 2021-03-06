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
package org.rifidi.app.performance;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.rifidi.edge.api.AbstractRifidiApp;
import org.rifidi.edge.api.service.tagmonitor.RawTagMonitoringService;
import org.rifidi.edge.api.service.tagmonitor.RawTagSubscriber;
import org.rifidi.edge.api.service.tagmonitor.ReadZone;

/**
 * A simple application which prints out arriving and departing tag for all readers.
 * 
 * @author Matthew Dean - matt@transcends.co
 */
public class PerformanceApp extends AbstractRifidiApp {

	/** The service for monitoring arrival and departure events */
	private RawTagMonitoringService rawTagMonitoringService;
	private List<RawTagSubscriber> subscriberList;

	public PerformanceApp(String group, String name) {
		super(group, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.api.AbstractRifidiApp#_start()
	 */
	@Override
	public void _start() {
		super._start();

		PerformanceSubscriber sub = new PerformanceSubscriber();
		this.subscriberList = new LinkedList<RawTagSubscriber>();
		this.subscriberList.add(sub);
		this.rawTagMonitoringService.subscribe(sub, new LinkedList<ReadZone>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.api.AbstractRifidiApp#_stop()
	 */
	@Override
	public void _stop() {
		for (RawTagSubscriber s : this.subscriberList) {
			this.rawTagMonitoringService.unsubscribe(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.api.AbstractRifidiApp#initialize()
	 */
	@Override
	public void initialize() {
	}

	/**
	 * Called by spring. This method injects the ReadZoneMonitoringService into
	 * the application.
	 * 
	 * @param rzms
	 */
	public void setRawTagMonitoringService(RawTagMonitoringService rzms) {
		this.rawTagMonitoringService = rzms;
	}
}
