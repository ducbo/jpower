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
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.nicmus.jpoweradmin.model.orm.PasswordResets;
import com.nicmus.jpoweradmin.model.orm.PasswordResets_;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;

@Singleton
@ApplicationScoped
public class PasswordTokenCleaner implements Serializable {
	private static final long serialVersionUID = -3855355934730450357L;
	private static final int HOURS = 24;
	
	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	/**
	 * Scan every hour for password reset tokens older than 24 hours and delete them. 
	 */
	@Schedule(minute="0",hour="*")
	public void cleanTokens() {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		 CriteriaDelete<PasswordResets> createCriteriaDelete = criteriaBuilder.createCriteriaDelete(PasswordResets.class);
		 Root<PasswordResets> from = createCriteriaDelete.from(PasswordResets.class);
		 
		 Calendar calendar = Calendar.getInstance();
		 calendar.add(Calendar.HOUR, -HOURS);
		 Date time = calendar.getTime();
		 Predicate predicate = criteriaBuilder.lessThan(from.get(PasswordResets_.dateCreated), time);
		 createCriteriaDelete.where(predicate);
		 this.entityManager.createQuery(createCriteriaDelete).executeUpdate();
		
	}
}
