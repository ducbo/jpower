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
public class Role extends PersistentObject {
	private static final long serialVersionUID = -3737505336237434091L;

	public enum ROLE{ROOT,USER}
	
	@Enumerated(EnumType.STRING)
	private ROLE role;
	
	@ManyToOne
	private User user; //mapping back to user
	
	
	/**
	 * @return the role
	 */
	public ROLE getRole() {
		return role;
	}
	
	/**
	 * @param role the role to set
	 */
	public void setRole(ROLE role) {
		this.role = role;
	}
	
	/**
	 * @return the user
	 */
	
	public User getUser() {
		return user;
	}
	
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	
}
