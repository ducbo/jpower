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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.primefaces.model.SortOrder;

import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.model.orm.PasswordResets;
import com.nicmus.jpoweradmin.model.orm.PasswordResets_;
import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Role;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.User_;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;
import com.nicmus.jpoweradmin.utils.Loggable;
import com.nicmus.powerdns.service.PowerDNSService;

@Stateless
@RolesAllowed({"ROOT","USER"})
public class AccountServiceDAO {

	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	@Inject
	private PowerDNSService powerDNSService;
	
	/**
	 * 
	 * @param userName
	 * @param timeZoneId
	 */
	public void updateTimeZonePreference(String userName, String timeZoneId) {
		User user = this.getUser(userName);
		user.setTimeZoneId(timeZoneId);
		this.entityManager.persist(user);
	}
	
	/**
	 * 
	 * @param userName
	 * @param localeCode
	 */
	public void updateLocalePreferences(String userName, String localeCode) {
		User user = this.getUser(userName);
		user.setLocaleName(localeCode);
		this.entityManager.persist(user);
	}
	
	
	/**
	 * 
	 * @param userName
	 * @return
	 */
	@PermitAll
	public User getUser(String userName) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<User> userCriteriaQuery = criteriaBuilder.createQuery(User.class);
		Root<User> from = userCriteriaQuery.from(User.class);
		Predicate predicate = criteriaBuilder.equal(from.get(User_.userName), userName);		
		return this.entityManager.createQuery(userCriteriaQuery.select(from).where(predicate)).getSingleResult();
	}
	
	/**
	 * Get the user id based on the GUID
	 * @param guid
	 * @return userId matching GUID
	 */
	public String getUserName(String guid) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<PasswordResets> userNameQuery = criteriaBuilder.createQuery(PasswordResets.class);
		Root<PasswordResets> from = userNameQuery.from(PasswordResets.class);
		Predicate predicate = criteriaBuilder.equal(from.get(PasswordResets_.guid), guid);
		PasswordResets passwordResets = this.entityManager.createQuery(userNameQuery.select(from).where(predicate)).getSingleResult();
		return passwordResets.getUserName();
	}
	
	/**
	 * Mark the user account as deleted and delete all user zones from the pdns DB
	 */
	public void deleteUserAccount(String userName) {
		User user = this.getUser(userName);
		//delete all user zones
		Set<Domain> domains = user.getDomains();
		domains.forEach(d -> {
			this.powerDNSService.deleteZone(d.getZoneId());
			Set<User> users = d.getUsers();
			users.forEach(u -> u.getDomains().remove(d));
			this.entityManager.remove(d);
		});
		Set<Role> roles = user.getRoles();
		roles.forEach(r -> this.entityManager.remove(r));
		this.entityManager.remove(user);
		
	}
	
	/**
	 * Disable the given user account.
	 * Account's zones are deleted and the user is set to deleted
	 * @param userName
	 */
	@RolesAllowed({"ROOT"})
	public void disableUserAcount(String userName) {
		User user = this.getUser(userName);
		user.setDeleted(true);
		this.entityManager.persist(user);
		//delete all the domains from PowerDNS db for the given user
		Set<Domain> domains = user.getDomains();
		domains.forEach(d -> this.powerDNSService.deleteZone(d.getZoneId()));
	}
	
	/**
	 * Re-enable the given user account
	 * @param userName
	 */
	@RolesAllowed({"ROOT"})
	public void reenableUserAccount(String userName) {
		User user = this.getUser(userName);
		user.setDeleted(false);
		this.entityManager.persist(user);
		
		//re-add the domains to power dns.
		Set<Domain> domains = user.getDomains();
		for(Domain d : domains) {
			int zoneId = this.powerDNSService.createZone(d.getName(), d.getType().name());
			d.setZoneId(zoneId);
			this.entityManager.persist(d);
			//add the zone records
			Set<Record> records = d.getRecords();
			for(Record r : records) {
				int zoneRecordId = this.powerDNSService.createZoneRecord(zoneId, r);
				r.setZoneRecordId(zoneRecordId);
				this.entityManager.persist(r);
			}
		}
	}
	

	/**
	 * 
	 * @param startIndex
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return
	 */
	@RolesAllowed({"ROOT"})
	@Loggable
	public List<User> getUsers(int startIndex, int size, String sortField, SortOrder sortOrder, Map<String, Object> filters){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<User> userQueryCriteria = criteriaBuilder.createQuery(User.class);
		Root<User> from = userQueryCriteria.from(User.class);
		
		//fetch the roles
		from.fetch(User_.roles);
		
		//filtering - only by userName
		Predicate like = null;
		String userName = User_.userName.getName();
		if(filters.containsKey(userName)) {
			String nameValue = (String) filters.get(userName);
			if(nameValue != null && !nameValue.isEmpty()) {
				like = criteriaBuilder.like(from.get(User_.userName), "%" + nameValue + "%");
				
			}
		}
		
		Path<?> path = null;
		if(sortField != null && !sortField.isEmpty()) {
			switch (sortField) {
			case "userName":
				path = from.get(User_.userName);
				break;
			case "dateCreated":
				path = from.get(User_.dateCreated);
				break;
			case "dateModified":
				path = from.get(User_.dateModified);
				break;
			default:
				path = from.get(User_.userName);
				break;
			}
		}
		
		//set the sorting order
		if(sortField != null && sortOrder != null) {
			switch(sortOrder) {
			case ASCENDING:
				userQueryCriteria.orderBy(criteriaBuilder.asc(path));
				break;
			case DESCENDING:
				userQueryCriteria.orderBy(criteriaBuilder.desc(path));
				break;
			case UNSORTED:
				break;
			default:
				break;
			}
		}
		CriteriaQuery<User> query = userQueryCriteria.select(from);
		if(like != null) {
			query.where(like);
		}
		
		TypedQuery<User> userQuery = this.entityManager.createQuery(query);
		userQuery.setFirstResult(startIndex);
		userQuery.setMaxResults(size);
		return userQuery.getResultList();
	}
	
	/**
	 * 
	 * @param filters
	 * @return
	 */
	@RolesAllowed({"ROOT"})
	public long getUserCount(Map<String, Object> filters) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> userCountQuery = criteriaBuilder.createQuery(Long.class);
		Root<User> from = userCountQuery.from(User.class);
		
		String userNameKey = User_.userName.getName();
		Predicate like = null;
		if(filters.containsKey(userNameKey)) {
			String userNameValue = (String) filters.get(userNameKey);
			if(userNameValue != null && !userNameValue.isEmpty()) {
				like = criteriaBuilder.like(from.get(User_.userName), "%" + userNameValue + "%");
			}
		}
		CriteriaQuery<Long> countSelect = userCountQuery.select(criteriaBuilder.count(from));
		if(like != null) {
			countSelect.where(like);
		}
			
		return this.entityManager.createQuery(countSelect).getSingleResult();
		
	}

}
