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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(name="nameIndex",columnList="name")})
public class Domain extends PersistentObject {
	private static final long serialVersionUID = -1745984405904032762L;
	public enum TYPE{NATIVE,MASTER,SLAVE}
	private String name;
	private String master;
	private int lastCheck; //determine if necessary for interface
	@Enumerated(EnumType.STRING)
	private TYPE type = TYPE.NATIVE;
	private int zoneId;//the id for the record in the pdns table
	private String account;
	
	@OneToMany(mappedBy="domain", cascade=CascadeType.ALL, orphanRemoval=true)
	private Set<Record> records = new LinkedHashSet<>();
	
	@ManyToMany(mappedBy="domains")
	private Set<User> users = new LinkedHashSet<>(); //mapping back to users

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the master
	 */
	public String getMaster() {
		return master;
	}
	/**
	 * @param master the master to set
	 */
	public void setMaster(String master) {
		this.master = master;
	}
	/**
	 * @return the lastCheck
	 */
	public int getLastCheck() {
		return lastCheck;
	}
	/**
	 * @param lastCheck the lastCheck to set
	 */
	public void setLastCheck(int lastCheck) {
		this.lastCheck = lastCheck;
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
	 * @return the notifiedSerial
	 */

	/**
	 * @return the zoneId
	 */
	public int getZoneId() {
		return zoneId;
	}
	/**
	 * @param zoneId the zoneId to set
	 */
	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
	}

		
	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}
	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	
	
	/**
	 * @return the records
	 */
	
	public Set<Record> getRecords() {
		return this.records;
	}
	/**
	 * @param records the records to set
	 */
	public void setRecords(Set<Record> records) {
		this.records = records;
	}
	/**
	 * @return the users
	 */
	public Set<User> getUsers() {
		return users;
	}
	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Domain other = (Domain) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equalsIgnoreCase(other.name))
			return false;
		return true;
	}
	
	
			
	
}
