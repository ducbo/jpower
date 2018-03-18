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
import java.net.IDN;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.model.orm.Domain_;
import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Role;
import com.nicmus.jpoweradmin.model.orm.Role_;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;
import com.nicmus.pdns.model.orm.Zone;
import com.nicmus.pdns.model.orm.ZoneRecord;
import com.nicmus.powerdns.service.PowerDNSService;


@RolesAllowed({"ROOT"})
@Stateless
public class ZoneImportService implements Serializable {
	private static final long serialVersionUID = -3586655522344827960L;
	
	@Inject
	private Logger logger;
	
	@Inject
	private PowerDNSService powerDNSService;
	
	@Inject
	@JPowerAdmin
	private EntityManager entityManager;
	
	/**
	 * Import all zones from PowerDNS instance into JPowerAdmin control panel.
	 * Imported zones are linked to all accounts having ROOT privileges
	 * @return number of zones imported
	 */
	public int importZones() {
		int importedCount = 0;
		List<Zone> zones = this.powerDNSService.getZones();
		for(Zone zone : zones) {
			String zoneName = IDN.toUnicode(zone.getName());
			this.logger.debug("Checking {}", zoneName);
			if(this.zoneExists(zoneName)) {
				this.logger.debug("Zone {} exists in JPowerAdmin db. Skipping", zoneName);
				continue;
			}
			
			this.logger.info("Importing zone {}", zoneName);
			//create the equivalent domain name
			Domain domain = new Domain();
			domain.setName(zoneName);
			domain.setZoneId(zone.getId());
			
			//get zone records
			List<ZoneRecord> records = this.powerDNSService.getRecords(zone.getId());
			for(ZoneRecord zoneRecord : records) {
				Record record = new Record();
				record.setActive(!zoneRecord.isDisabled());
				record.setAuth(zoneRecord.isAuth());
				record.setContent(IDN.toUnicode(zoneRecord.getContent()));
				record.setName(IDN.toUnicode(zoneRecord.getName()));
				record.setOrderName(zoneRecord.getOrderName());
				record.setPrio(zoneRecord.getPrio());
				record.setType(com.nicmus.jpoweradmin.model.orm.Record.TYPE.valueOf(zoneRecord.getType()));
				record.setZoneRecordId(zoneRecord.getId());
				
				record.setDomain(domain);
				domain.getRecords().add(record);
				this.entityManager.persist(record);
			}
			this.entityManager.persist(domain);
			//link the newly created domain to all root accounts
			//link the zone to all the root accounts
			//get all user roles
			CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
			CriteriaQuery<Role> roleCriteriaQuery = criteriaBuilder.createQuery(Role.class);
			Root<Role> roleFrom = roleCriteriaQuery.from(Role.class);
			roleCriteriaQuery.select(roleFrom).where(criteriaBuilder.equal(roleFrom.get(Role_.role), Role.ROLE.ROOT));
			
			List<Role> resultList = this.entityManager.createQuery(roleCriteriaQuery).getResultList();
			
			for(Role role : resultList){
				role.getUser().getDomains().add(domain);
				domain.getUsers().add(role.getUser());
				this.entityManager.persist(role);
				this.entityManager.persist(domain);
			}
			importedCount++;
		}
		return importedCount;
	}
	
	/**
	 * Determine if the given zone exists
	 * @param name
	 * @return true if zone exists in JPowerAdmin db, false otherwise
	 */
	private boolean zoneExists(String name) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Domain> domainQuery = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> from = domainQuery.from(Domain.class);
		Predicate predicate = criteriaBuilder.equal(from.get(Domain_.name),name);
		domainQuery.select(from).where(predicate);
		try {
			this.entityManager.createQuery(domainQuery).getSingleResult();
			return true;
		} catch(NoResultException e) {
			//expected
			return false;
		}
	}


}
