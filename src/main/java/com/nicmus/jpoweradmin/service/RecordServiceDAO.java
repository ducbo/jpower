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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.primefaces.model.SortOrder;

import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.model.orm.Domain_;
import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Record.TYPE;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;
import com.nicmus.powerdns.service.PowerDNSService;
import com.nicmus.jpoweradmin.model.orm.Record_;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.User_;

@Stateless
public class RecordServiceDAO {

	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	@Inject
	private PowerDNSService powerDNSService;
	
	/**
	 * 
	 * @param record
	 */
	public void addRecord(Record record){
		int id = record.getDomain().getId();
		Domain domain = this.entityManager.find(Domain.class, id);
		domain.getRecords().add(record);
		record.setDomain(domain);
		
		int recordId = this.powerDNSService.createZoneRecord(domain.getZoneId(), record);
		record.setZoneRecordId(recordId);
		
		this.entityManager.persist(domain);
		this.entityManager.persist(record);
		
	}
		
	/**
	 * 
	 * @param userName
	 * @param domainName
	 * @param startIndex
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return
	 */
	public List<Record> getRecords(String userName, String domainName, int startIndex, int size, String sortField, SortOrder sortOrder, Map<String, Object> filters){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Record> recordCriteriaQuery = criteriaBuilder.createQuery(Record.class);
		
		Root<Record> from = recordCriteriaQuery.from(Record.class);
		
		Join<Record, Domain> domainJoin = from.join(Record_.domain);
		
		SetJoin<Domain, User> usersJoin = domainJoin.join(Domain_.users);
		
		Predicate domainNamePredicate = criteriaBuilder.equal(domainJoin.get(Domain_.name), domainName);
		Predicate userNamePredicate = criteriaBuilder.equal(usersJoin.get(User_.userName), userName);
		
		Predicate whereCondition = criteriaBuilder.and(domainNamePredicate,userNamePredicate);
		
		//filtering predicates
		//filter by name
		String nameKey = Record_.name.getName();
		if(filters.containsKey(nameKey)){
			String filterValue = (String) filters.get(nameKey);
			if(filterValue != null && !filterValue.isEmpty()){
				Predicate recordName = criteriaBuilder.and(criteriaBuilder.like(from.get(Record_.name), "%" + filterValue + "%"));
				whereCondition = criteriaBuilder.and(whereCondition,recordName);
			}
		}
		
		recordCriteriaQuery.select(from).where(whereCondition);
		
		//sorting
		Path<?> sortPath = null;
		if(sortField != null && !sortField.isEmpty()){
			switch(sortField){
			case "name":
				sortPath = from.get(Record_.name);
				break;
			case "active":
				sortPath = from.get(Record_.active);
				break;
			case "type":
				sortPath = from.get(Record_.type);
				break;
			case "content":
				sortPath = from.get(Record_.content);
				break;
			case "dateCreated":
				sortPath = from.get(Record_.dateCreated);
				break;
			case "dateModified":
				sortPath = from.get(Record_.dateModified);
				break;
			default:
				sortPath = from.get(Record_.dateCreated);
				break;
			}
		}
		
		//set the sorting order
		if(sortField != null && sortOrder  != null){
			switch(sortOrder){
			case ASCENDING:
				recordCriteriaQuery.orderBy(criteriaBuilder.asc(sortPath));
				break;
			case DESCENDING:
				recordCriteriaQuery.orderBy(criteriaBuilder.desc(sortPath));
				break;
			case UNSORTED:
				break;
			default:
				break;
			
			}
		}
		
		TypedQuery<Record> recordQuery = this.entityManager.createQuery(recordCriteriaQuery);
		recordQuery.setFirstResult(startIndex);
		recordQuery.setMaxResults(size);
		return recordQuery.getResultList();
	}
	
	/**
	 * 
	 * @param domainName
	 * @param filters
	 * @return
	 */
	public Long getRecordCount(String domainName, Map<String, Object> filters){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		Root<Record> from = countQuery.from(Record.class);
		Join<Record, Domain> domainJoin = from.join(Record_.domain);
		
		Predicate domainPredicate = criteriaBuilder.equal(domainJoin.get(Domain_.name), domainName);
		
		Predicate wherePredicate = criteriaBuilder.and(domainPredicate);
		
		//filtering 
		//filter by name at the momen
		//TODO: Add additional filters if necessary
		String keyName = Record_.name.getName();
		if(filters.containsKey(keyName)){
			String filterValue = (String) filters.get(keyName);
			if(filterValue != null && !filterValue.isEmpty()){
				wherePredicate = criteriaBuilder.and(wherePredicate, criteriaBuilder.like(from.get(Record_.name), "%" + filterValue + "%"));
			}
		}
		
		CriteriaQuery<Long> query = countQuery.select(criteriaBuilder.count(from)).where(wherePredicate);
		return this.entityManager.createQuery(query).getSingleResult();
	}
	
	/**
	 * 
	 * @param userName
	 * @param domainName
	 * @return
	 */
	public List<Record> getRecords(String userName, String domainName){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Record> recordCriteria = criteriaBuilder.createQuery(Record.class);
		Root<Record> from = recordCriteria.from(Record.class);
		
		Join<Record, Domain> domainJoin = from.join(Record_.domain);
		
		SetJoin<Domain, User> userJoin = domainJoin.join(Domain_.users);

		Predicate domainNamePredicate = criteriaBuilder.equal(domainJoin.get(Domain_.name), domainName);
		Predicate userNamePredicate = criteriaBuilder.equal(userJoin.get(User_.userName), userName);
		
		Predicate whereCondition = criteriaBuilder.and(domainNamePredicate, userNamePredicate);
		
		recordCriteria.select(from).where(whereCondition);
		
		return this.entityManager.createQuery(recordCriteria).getResultList();
	}
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @param content
	 * @return
	 */
	public Record getRecord(String name, TYPE type, String content){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Record> criteriaQuery = criteriaBuilder.createQuery(Record.class);
		Root<Record> from = criteriaQuery.from(Record.class);
		
		Predicate namePredicate = criteriaBuilder.equal(from.get(Record_.name), name);
		Predicate typePredicate = criteriaBuilder.and(namePredicate, criteriaBuilder.equal(from.get(Record_.type), type));
		Predicate contentPredicate = criteriaBuilder.and(typePredicate, criteriaBuilder.equal(from.get(Record_.content), content));
		
		CriteriaQuery<Record> criteriaSingleRecord = criteriaQuery.select(from).where(contentPredicate);
		TypedQuery<Record> query = this.entityManager.createQuery(criteriaSingleRecord);
		return query.getSingleResult();
		
	}
	
	/**
	 * 
	 * @param record
	 */
	public void updateRecord(Record record) {
		Record merged = this.entityManager.merge(record);
		this.entityManager.persist(merged);
		boolean active = merged.isActive();
		boolean disabled = !active;
		this.powerDNSService.setRecordStatus(merged.getZoneRecordId(), disabled);
	}
	
	/**
	 * 
	 * @param record
	 */
	public void deleteRecord(Record record) {
		Record mergedRecord = this.entityManager.merge(record);
		Domain domain = mergedRecord.getDomain();
		domain.getRecords().remove(mergedRecord);
		this.entityManager.remove(mergedRecord);
		this.powerDNSService.deleteZoneRecord(record.getZoneRecordId());
	}
	
	/**
	 * Verify if the record exists
	 * @param name
	 * @param content
	 * @param type
	 * @return
	 */
	public boolean exists(String name, String content, Record.TYPE type) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Record> recordQuery = criteriaBuilder.createQuery(Record.class);
		Root<Record> from = recordQuery.from(Record.class);
		
		Predicate namePredicate = criteriaBuilder.equal(from.get(Record_.name), name);
		Predicate contentPredicate = criteriaBuilder.and(namePredicate, criteriaBuilder.equal(from.get(Record_.content), content));
		Predicate typePredicate = criteriaBuilder.and(contentPredicate, criteriaBuilder.equal(from.get(Record_.type), type));
		CriteriaQuery<Record> query = recordQuery.select(from).where(typePredicate);
		List<Record> resultList = this.entityManager.createQuery(query).getResultList();
		return !resultList.isEmpty();
	}
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public boolean exists(String name, TYPE type) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Record> recordQuery = criteriaBuilder.createQuery(Record.class);
		Root<Record> from = recordQuery.from(Record.class);
		
		Predicate namePredicate = criteriaBuilder.equal(from.get(Record_.name), name);
		Predicate typePredicate = criteriaBuilder.and(namePredicate, criteriaBuilder.equal(from.get(Record_.type), type));
		CriteriaQuery<Record> query = recordQuery.select(from).where(typePredicate);
		List<Record> resultList = this.entityManager.createQuery(query).getResultList();
		return !resultList.isEmpty();
	}
	
	/**
	 * 
	 * @param content
	 * @param type
	 * @return
	 */
	public boolean contentExists(String content, TYPE type) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Record> createQuery = criteriaBuilder.createQuery(Record.class);
		Root<Record> from = createQuery.from(Record.class);
		
		Predicate contentPredicate = criteriaBuilder.equal(from.get(Record_.content), content);
		Predicate typePredicate = criteriaBuilder.and(contentPredicate, criteriaBuilder.equal(from.get(Record_.type), type));
		
		List<Record> resultList = this.entityManager.createQuery(createQuery.select(from).where(typePredicate)).getResultList();
		return !resultList.isEmpty();
	}
}
