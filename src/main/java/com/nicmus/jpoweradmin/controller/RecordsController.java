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
package com.nicmus.jpoweradmin.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.CurrentUser;
import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.service.DNSValidator;
import com.nicmus.jpoweradmin.service.RecordServiceDAO;
import com.nicmus.jpoweradmin.service.ZoneServiceDAO;

@Named
@ViewScoped
public class RecordsController implements Serializable {
	private static final long serialVersionUID = 1662442304616109265L;

	@Inject
	private Logger logger;

	private String zoneName;
	
	@Inject
	private CurrentUser currentUser;
	
	@Inject
	private ZoneServiceDAO zoneServiceDAO;
	
	@Inject
	private RecordServiceDAO recordServiceDAO;
	
	@Inject
	private DNSValidator dnsValidator;
	
	@Named
	@Produces
	private Record newRecord = new Record();
	
	@Named
	@Produces
	private Domain selectedDomain;
	
	private List<Record> selectedRecords;
	
	@Inject
	private FacesContext facesContext;
	
	private LazyDataModel<Record> lazyModel;
	
	@PostConstruct
	public void init(){
		//implementation of lazy model
		this.lazyModel = new LazyDataModel<Record>() {
			private static final long serialVersionUID = -2652268750057571898L;

			/* (non-Javadoc)
			 * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
			 */
			@Override
			public List<Record> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
				//String userName = facesContext.getExternalContext().getUserPrincipal().getName();
				String userName = currentUser.getUserName();
				List<Record> records = recordServiceDAO.getRecords(userName, selectedDomain.getName(), first, pageSize, sortField, sortOrder, filters);
				this.setWrappedData(records);
				//record count
				Long recordCount = recordServiceDAO.getRecordCount(selectedDomain.getName(), filters);
				this.setRowCount(Math.toIntExact(recordCount));
				return records;
			}

			/* (non-Javadoc)
			 * @see org.primefaces.model.LazyDataModel#getRowData(java.lang.String)
			 */
			@Override
			public Record getRowData(String rowKey) {
				@SuppressWarnings("unchecked")
				List<Record> records = (List<Record>) this.getWrappedData();
				for(Record r : records){
					if(r.getId() == Integer.parseInt(rowKey)){
						return r;
					}
				}
				return null;
				
			}

			/* (non-Javadoc)
			 * @see org.primefaces.model.LazyDataModel#getRowKey(java.lang.Object)
			 */
			@Override
			public Object getRowKey(Record object) {
				return object.getId();
			}
		};

	}
	
	
	/**
	 * 
	 */
	public void checkZone(){
		this.logger.info("Zone name: {}. Username: {}", this.zoneName, this.facesContext.getExternalContext().getUserPrincipal().getName());
		//String userName = this.facesContext.getExternalContext().getUserPrincipal().getName();
		String userName = this.currentUser.getUserName();
		Domain zone = this.zoneServiceDAO.getZone(userName, this.zoneName);
		this.selectedDomain = zone;
		
		
	}
	
	/**
	 * 
	 */
	public void deleteSelected() {
		for(Record r : this.selectedRecords) {
			this.recordServiceDAO.deleteRecord(r);
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
					r.getName() + " " + r.getType() + " " + r.getContent() + " deleted",""));
		}
	}
	
	/**
	 * 
	 */
	public void addRecord(){
		//TODO: add validation
		if(this.newRecord.getName() == null || this.newRecord.getName().isEmpty()) {
			this.newRecord.setName(this.selectedDomain.getName());
		}
		
		if(!this.newRecord.getName().contains(this.selectedDomain.getName())){
			this.newRecord.setName(this.newRecord.getName().concat(".").concat(this.selectedDomain.getName()));
		}
		
		if(!this.dnsValidator.isValid(this.newRecord)) {
			return;
		}
		
		this.newRecord.setDomain(this.selectedDomain);
		
		this.recordServiceDAO.addRecord(this.newRecord);
		this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
				this.newRecord.getName() + " " + this.newRecord.getType() + " " + 
				this.newRecord.getContent() + " added to " + this.selectedDomain.getName(),""));
		this.newRecord = new Record();
		
	}

	/**
	 * 
	 * @param id
	 */
	public void toggle() {
		Record selectedRecord = this.lazyModel.getRowData();
		//Record selectedRecord = this.lazyModel.getRowData("" + id);
		String message = "";
		if(selectedRecord.isActive()) {
			message = "ACTIVE";
		} else {
			message = "DISABLED";
		}
		
		this.recordServiceDAO.updateRecord(selectedRecord);
		this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,selectedRecord.getName() + " " + 
		selectedRecord.getType().name() + " " + selectedRecord.getContent() + " is " + message,""));
	}
	
	/**
	 * @return the zoneName
	 */
	public String getZoneName() {
		return zoneName;
	}

	/**
	 * @param zoneName the zoneName to set
	 */
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}


	/**
	 * @return the lazyModel
	 */
	public LazyDataModel<Record> getLazyModel() {
		return lazyModel;
	}


	/**
	 * @param lazyModel the lazyModel to set
	 */
	public void setLazyModel(LazyDataModel<Record> lazyModel) {
		this.lazyModel = lazyModel;
	}


	/**
	 * @return the selectedRecords
	 */
	public List<Record> getSelectedRecords() {
		return selectedRecords;
	}


	/**
	 * @param selectedRecords the selectedRecords to set
	 */
	public void setSelectedRecords(List<Record> selectedRecords) {
		this.selectedRecords = selectedRecords;
	}
	
	
}
