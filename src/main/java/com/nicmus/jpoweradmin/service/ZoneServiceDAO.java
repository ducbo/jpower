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

import java.net.IDN;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.model.orm.Domain_;
import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Role;
import com.nicmus.jpoweradmin.model.orm.Role_;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.User_;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;
import com.nicmus.powerdns.service.PowerDNSService;

@Stateless
public class ZoneServiceDAO {
	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	@Inject
	private AccountServiceDAO accountserviceDAO;
	
	@Inject
	private PowerDNSService powerDNService;
	
	@Inject
	private Logger logger;
	
	
	/**
	 * 
	 * @param userName
	 * @param domainName
	 * @return
	 */
	public Domain getZone(String userName, String domainName){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Domain> domainQuery = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> from = domainQuery.from(Domain.class);
		
		SetJoin<Domain, User> userJoin = from.join(Domain_.users);
		Predicate domainPredicate = criteriaBuilder.equal(from.get(Domain_.name), domainName);
		Predicate userPredicate = criteriaBuilder.equal(userJoin.get(User_.userName), userName);
		
		Predicate whereCondition = criteriaBuilder.and(domainPredicate, userPredicate);
		
		domainQuery.select(from).where(whereCondition);
		return this.entityManager.createQuery(domainQuery).getSingleResult();
		
	}
	
	/**
	 * 
	 * @param user
	 * @param domain
	 */
	public void createZone(String userName, Domain domain){
		//verify that the zone does not exist already
		
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		
		CriteriaQuery<Domain> domainCriteria = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> from = domainCriteria.from(Domain.class);
		domainCriteria.select(from).where(criteriaBuilder.equal(from.get(Domain_.name), domain.getName()));

		try {
			TypedQuery<Domain> domainQuery = this.entityManager.createQuery(domainCriteria);
			domainQuery.getSingleResult();
			throw new RuntimeException("Not Unique");
		} catch (NoResultException e){
			//expected behavior
			this.logger.debug("No zone found matching {}. Creating one", domain.getName());
			//create a pdns zone
			int pdnsId = this.powerDNService.createZone(IDN.toASCII(domain.getName()), domain.getType().name());
			domain.setZoneId(pdnsId);
			//load the user for the given zone
			User dbUser = this.accountserviceDAO.getUser(userName);
			
			dbUser.getDomains().add(domain);
			domain.getUsers().add(dbUser);
			this.entityManager.persist(dbUser);
			//add the zone records to powerdns
			Set<Record> records = domain.getRecords();
			for(Record r : records) {
				int zoneRecordId = this.powerDNService.createZoneRecord(pdnsId, r);
				r.setZoneRecordId(zoneRecordId);
				this.entityManager.persist(r);
				this.entityManager.persist(domain);	
			}
			
			
			this.logger.debug("Successfully added zone {}", domain.getName());
			
			//link the zone to all the root accounts
			//get all user roles
			CriteriaQuery<Role> roleCriteriaQuery = criteriaBuilder.createQuery(Role.class);
			Root<Role> roleFrom = roleCriteriaQuery.from(Role.class);
			roleCriteriaQuery.select(roleFrom).where(criteriaBuilder.equal(roleFrom.get(Role_.role), Role.ROLE.ROOT));
			
			List<Role> resultList = this.entityManager.createQuery(roleCriteriaQuery).getResultList();
			
			for(Role role : resultList){
				if(!role.getUser().getDomains().contains(domain)){
					role.getUser().getDomains().add(domain);
					domain.getUsers().add(role.getUser());
					this.entityManager.persist(role);
					this.entityManager.persist(domain);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param userName
	 * @param startIndex
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return
	 */
	public List<Domain> getZones(String userName, int startIndex, int size, String sortField, SortOrder sortOrder, Map<String, Object> filters){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		
		CriteriaQuery<Domain> domainQueryCriteria = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> from = domainQueryCriteria.from(Domain.class);

		SetJoin<Domain, User> userjoin = from.join(Domain_.users);

		Predicate userNameConjunction = criteriaBuilder.equal(userjoin.get(User_.userName), userName);
		
		Predicate wherePredicate = criteriaBuilder.and(userNameConjunction);
		
		//filtering - at the moment only the name is filtered
		String key = Domain_.name.getName();
		if(filters.containsKey(key)){
			String value = (String) filters.get(key);
			if(value != null && !value.isEmpty()){
				wherePredicate = criteriaBuilder.and(wherePredicate,criteriaBuilder.like(from.get(Domain_.name), "%" + value + "%"));
			}
		}
		
		domainQueryCriteria.select(from).where(wherePredicate);
	

		//set the sorting
		Path<?> path = null;
		if(sortField != null && !sortField.isEmpty()){
			switch (sortField) {
			case "name":
				path = from.get(Domain_.name);
				break;
			case "type":
				path = from.get(Domain_.type);
				break;
			case "dateCreated":
				path = from.get(Domain_.dateCreated);
				break;
			case "dateModified":
				path = from.get(Domain_.dateModified);
				break;
			default:
				path = from.get(Domain_.name);
				break;
			}
		}
		
		//set the sorting order
		if(sortField != null && sortOrder != null){
			switch(sortOrder){
			case ASCENDING:
				domainQueryCriteria.orderBy(criteriaBuilder.asc(path));
				break;
			case DESCENDING:
				domainQueryCriteria.orderBy(criteriaBuilder.desc(path));
				break;
			case UNSORTED:
				break;
			default:
				break;
			
			}
		}
		
		TypedQuery<Domain> domainQuery = this.entityManager.createQuery(domainQueryCriteria);
		//pagination limits
		domainQuery.setFirstResult(startIndex);
		domainQuery.setMaxResults(size);
		
		return domainQuery.getResultList();
		
	}
	
	/**
	 * 
	 * @param userName
	 * @param startIndex
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return
	 */
	public long getZoneCount(String userName,  Map<String, Object> filters){
		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<Domain> from = countQuery.from(Domain.class);
		
		SetJoin<Domain, User> userjoin = from.join(Domain_.users);

		Predicate userNameConjunction = cb.equal(userjoin.get(User_.userName), userName);
		
		Predicate wherePredicate = cb.and(userNameConjunction);
		
		//filtering - at the moment only the name is filtered
		String key = "name";
		if(filters.containsKey(key)){
			String value = (String) filters.get(key);
			if(value != null && !value.isEmpty()){
				wherePredicate = cb.and(wherePredicate,cb.like(from.get(Domain_.name), "%" + value + "%"));
			}
		}

		CriteriaQuery<Long> select = countQuery.select(cb.count(from)).where(wherePredicate);
		
		TypedQuery<Long> createQuery = this.entityManager.createQuery(select);
		return createQuery.getSingleResult();
	}
	
	/**
	 * 
	 * @param domain
	 */
	public void deleteZone(Domain domain){
		Domain mergedDomain = this.entityManager.merge(domain);
		Set<User> users = mergedDomain.getUsers();
		for(User u : users){
			u.getDomains().remove(mergedDomain);
		}
		this.entityManager.remove(mergedDomain);
		this.powerDNService.deleteZone(domain.getZoneId());
	}
	
	

}
