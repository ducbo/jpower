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
package com.nicmus.powerdns.service;

import java.net.IDN;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.utils.PowerDNS;
import com.nicmus.pdns.model.orm.Zone;
import com.nicmus.pdns.model.orm.ZoneRecord;
import com.nicmus.pdns.model.orm.ZoneRecord_;
import com.nicmus.pdns.model.orm.Zone_;

@Stateless
public class PowerDNSService {

	@Inject
	@PowerDNS
	private EntityManager entityManager;
	
	/**
	 * Add the given zone in powerdns table
	 * @param zoneName
	 * @return
	 */
	public int createZone(String zoneName, String type) {
		Zone zone = new Zone();
		zone.setName(zoneName);;
		zone.setType(type);
		this.entityManager.persist(zone);
		return zone.getId();
	}

	/**
	 * Create a record to the specified zone
	 * @param zoneId
	 * @param record
	 * @return the primary key of the record created
	 */
	public int createZoneRecord(int zoneId, Record record) {
		Zone zone  = this.entityManager.find(Zone.class, zoneId);
		ZoneRecord zoneRecord = new ZoneRecord();
		zoneRecord.setName(IDN.toASCII(record.getName()));
		zoneRecord.setChangeDate(System.currentTimeMillis()/1000);
		zoneRecord.setContent(IDN.toASCII(record.getContent()));
		zoneRecord.setPrio(record.getPrio());
		zoneRecord.setTtl(record.getTtl());
		zoneRecord.setType(record.getType().name());
		zoneRecord.setZone(zone);
		this.entityManager.persist(zoneRecord);
		return zoneRecord.getId();
	}
	
	/**
	 * delete the zone referenced by id
	 *
	 * @param id
	 */
	public void deleteZone(int id) {
		this.deleteAllZoneRecords(id);
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaDelete<Zone> deleteCriteriaQuery = criteriaBuilder.createCriteriaDelete(Zone.class);
		Root<Zone> from = deleteCriteriaQuery.from(Zone.class);
		Predicate predicate = criteriaBuilder.equal(from.get(Zone_.id), id);
		this.entityManager.createQuery(deleteCriteriaQuery.where(predicate)).executeUpdate();
	}
	
	/**
	 * delete all zone records for the given Zone
	 * @param zone
	 */
	public void deleteAllZoneRecords(int zoneId) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaDelete<ZoneRecord> deleteZoneRecordsCriteria = criteriaBuilder.createCriteriaDelete(ZoneRecord.class);
		Root<ZoneRecord> from = deleteZoneRecordsCriteria.from(ZoneRecord.class);
		Expression<Boolean> predicate = criteriaBuilder.equal(from.get(ZoneRecord_.zone), this.entityManager.find(Zone.class, zoneId));
		this.entityManager.createQuery(deleteZoneRecordsCriteria.where(predicate)).executeUpdate();	
	}
	
	/**
	 * Delete the zone record identified by the id
	 * @param id
	 */
	public void deleteZoneRecord(int id) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaDelete<ZoneRecord> recordCriteriaDelete = criteriaBuilder.createCriteriaDelete(ZoneRecord.class);
		Root<ZoneRecord> from = recordCriteriaDelete.from(ZoneRecord.class);
		Predicate predicate = criteriaBuilder.equal(from.get(ZoneRecord_.id), id);
		this.entityManager.createQuery(recordCriteriaDelete.where(predicate)).executeUpdate();
	}
	
	
	/**
	 * 
	 * @param id
	 * @param active
	 */
	public void setRecordStatus(int id, boolean active) {
		ZoneRecord zoneRecord = this.entityManager.find(ZoneRecord.class, id);
		zoneRecord.setDisabled(active);
		this.entityManager.persist(zoneRecord);
	}
	
	/**
	 * Get a listing of all zones 
	 * @return
	 */
	public List<Zone> getZones(){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Zone> zoneQuery = criteriaBuilder.createQuery(Zone.class);
		Root<Zone> from = zoneQuery.from(Zone.class);
		zoneQuery.select(from);
		return this.entityManager.createQuery(zoneQuery).getResultList();
	}
	
	/**
	 * Get all zone records 
	 * @param zoneName
	 * @return the zone records for the given zone
	 */
	public List<ZoneRecord> getRecords(int zoneId){
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<ZoneRecord> zoneRecordQuery = criteriaBuilder.createQuery(ZoneRecord.class);
		Root<ZoneRecord> from = zoneRecordQuery.from(ZoneRecord.class);
		
		Join<ZoneRecord, Zone> zoneJoin = from.join(ZoneRecord_.zone);
		Predicate predicate = criteriaBuilder.equal(zoneJoin.get(Zone_.id), zoneId);
		zoneRecordQuery.select(from).where(predicate);
		return this.entityManager.createQuery(zoneRecordQuery).getResultList();
	}
	
}
