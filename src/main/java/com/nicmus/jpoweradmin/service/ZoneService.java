/*******************************************************************************
 * Copyright (C) 2017 nicmus inc. (jivko.sabev@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.nicmus.jpoweradmin.service;

import java.io.Serializable;
import java.util.Properties;

import javax.inject.Inject;

import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Record.TYPE;

/**
 * 
 * @author jsabev
 *
 */
public class ZoneService implements Serializable {
	private static final long serialVersionUID = -9134250336362039895L;
		
	
	@Inject
	private Properties properties;
	
	/**
	 * Create the SOA and NS records for new zones
	 * @param domain
	 */
	public void addInitialZoneRecords(Domain domain) {
		String ns1 = this.properties.getProperty("ns1.record.name");
		Record soa = new Record();
		soa.setName(domain.getName());
		soa.setType(TYPE.SOA);
		String content = ns1 + " hostmaster@" + domain.getName() + " 0 10800 3600 604800 3600" ;
		soa.setContent(content);
		soa.setDomain(domain);
		soa.setDomain(domain);
		domain.getRecords().add(soa);
		
		//add ns1, ns2, ns3
		this.addNSRecord(domain, ns1);
		String ns2 = this.properties.getProperty("ns2.record.name");
		this.addNSRecord(domain, ns2);
		String ns3 = this.properties.getProperty("ns3.record.name");
		this.addNSRecord(domain, ns3);

	}
	
	/**
	 * 
	 * @param domain
	 * @param nsname
	 */
	private void addNSRecord(Domain domain, String nsname) {
		Record record = new Record();
		record.setName(domain.getName());
		record.setType(TYPE.NS);
		record.setContent(nsname);
		record.setDomain(domain);
		domain.getRecords().add(record);
	}

	
	
}
