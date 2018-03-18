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
package com.nicmus.jpoweradmin.model.orm;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Entity
public class Record extends PersistentObject {
	private static final long serialVersionUID = 8356362271275690792L;

	public enum TYPE{A,AAAA,CAA,CNAME,HINFO,LOC,MX,NAPTR,NS,PTR,RP,SOA,SPF,SRV,TXT,MBOXFW}
	
	private String name;
	@Enumerated(EnumType.STRING)
	private TYPE type;
	private String content;
	private int ttl = 3600; //one hour
	private int prio;
	private int zoneRecordId;
	private boolean active=true;
	private String orderName;
	private boolean auth=true;
	
	//mapping back to domain
	@ManyToOne()
	private Domain domain;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public TYPE getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(TYPE type) {
		this.type = type;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the ttl
	 */
	public int getTtl() {
		return ttl;
	}
	/**
	 * @param ttl the ttl to set
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	/**
	 * @return the prio
	 */
	public int getPrio() {
		return prio;
	}
	/**
	 * @param prio the prio to set
	 */
	public void setPrio(int prio) {
		this.prio = prio;
	}

	/**
	 * @return the zoneRecordId
	 */
	public int getZoneRecordId() {
		return zoneRecordId;
	}
	/**
	 * @param zoneRecordId the zoneRecordId to set
	 */
	public void setZoneRecordId(int zoneRecordId) {
		this.zoneRecordId = zoneRecordId;
	}
	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	/**
	 * @return the orderName
	 */
	public String getOrderName() {
		return orderName;
	}
	/**
	 * @param orderName the orderName to set
	 */
	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}
	/**
	 * @return the auth
	 */
	public boolean isAuth() {
		return auth;
	}
	/**
	 * @param auth the auth to set
	 */
	public void setAuth(boolean auth) {
		this.auth = auth;
	}
	/**
	 * @return the domain
	 */
	
	public Domain getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	
	
}
