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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.nicmus.jpoweradmin.utils.JPowerAdmin;

@Stateless
public class GeneralDAO {
	
	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	/**
	 * 
	 * @param object
	 */
	public void update(Object object) {
		Object merge = this.entityManager.merge(object);
		this.entityManager.persist(merge);
	}
	
	/**
	 * 
	 * @param object
	 */
	public void save(Object object) {
		this.entityManager.persist(object);
	}
	
	/**
	 * 
	 * @param object
	 */
	public void deleteObject(Object object) {
		Object merge = this.entityManager.merge(object);
		this.entityManager.remove(merge);
	}
}
