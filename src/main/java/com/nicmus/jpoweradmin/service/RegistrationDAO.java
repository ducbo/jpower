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
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.User_;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;

@Stateless
public class RegistrationDAO {

	@Inject
	private Logger logger;
	
	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	/**
	 * Register the given user
	 * @param user
	 */
	public void register(User user) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<User> userCriteria = criteriaBuilder.createQuery(User.class);
		Root<User> from = userCriteria.from(User.class);
		userCriteria.select(from).where(criteriaBuilder.equal(from.get(User_.userName),user.getUserName()));
		
		//verify user does not exist
		try {			
			User result = this.entityManager.createQuery(userCriteria).getSingleResult();
			throw new RuntimeException(result.getUserName() + " already registered");
		} catch (NoResultException e){
			//this is the expected result - save the user
			//save the user
			this.entityManager.persist(user);
			this.logger.debug("Successfully persisted user object {} - {}", user.getClass().getName(), user.getUserName());
		}
	
	}
	
}
