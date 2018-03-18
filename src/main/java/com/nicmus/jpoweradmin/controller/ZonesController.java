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
import java.net.IDN;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceException;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.CurrentUser;
import com.nicmus.jpoweradmin.model.orm.Domain;
import com.nicmus.jpoweradmin.service.DNSValidator;
import com.nicmus.jpoweradmin.service.ZoneService;
import com.nicmus.jpoweradmin.service.ZoneServiceDAO;

@Named
@ViewScoped
public class ZonesController implements Serializable {
	private static final long serialVersionUID = -8136110818694167667L;

	@Inject
	private Logger logger;
	
	@Inject
	private ZoneServiceDAO zoneDAO;
			
	@Inject
	private ZoneService zoneService;
	
	@Inject
	private FacesContext facesContext;
	
	@Inject
	private CurrentUser currentUser;
	
	@Produces
	@Named
	private Domain newDomain;
	
	@Inject
	private DNSValidator dnsValidator;
	
	private List<Domain> selectedZones;
	
	private LazyDataModel<Domain> lazyModel;
		
	@PostConstruct
	public void init(){		
		
		this.newDomain = new Domain();
		 this.lazyModel = new LazyDataModel<Domain>() {
			private static final long serialVersionUID = -4275212482351964423L;

				/* (non-Javadoc)
				 * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
				 */
				@Override
				public List<Domain> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
					//String userName = facesContext.getExternalContext().getUserPrincipal().getName();
					String userName = currentUser.getUserName();
					List<Domain> zones = zoneDAO.getZones(userName, first, pageSize, sortField, sortOrder, filters);
					//Get the size of the model without the pagination - i.e first/pageSize.
					long zoneCount = zoneDAO.getZoneCount(userName, filters);
					this.setRowCount(Math.toIntExact(zoneCount));
					this.setWrappedData(zones);
					return zones;

				}
				
				/* (non-Javadoc)
				 * @see org.primefaces.model.LazyDataModel#getRowKey(java.lang.Object)
				 */
				@Override
				public Object getRowKey(Domain object) {
					return object.getId();
				}

				/* (non-Javadoc)
				 * @see org.primefaces.model.LazyDataModel#getRowData(java.lang.String)
				 */
				@Override
				public Domain getRowData(String rowKey) {
					@SuppressWarnings("unchecked")
					List<Domain> wrappedData = (List<Domain>) this.getWrappedData();
					for(Domain d : wrappedData){
						if(d.getId() == Integer.parseInt(rowKey)){
							return d;
						}
					}
					return null;
				}

			 
		};
	}
		
	/**
	 * 
	 */
	public void createZone(){
		//String userName = this.facesContext.getExternalContext().getUserPrincipal().getName();
		String userName = this.currentUser.getUserName();
		try {
			String name = this.newDomain.getName();
			String toCheck = IDN.toASCII(name);
			if(!this.dnsValidator.isValidFQDN(toCheck)) {
				return;
			}
			
			this.zoneService.addInitialZoneRecords(this.newDomain);

			this.zoneDAO.createZone(userName, this.newDomain);
			
			FacesMessage fc = new FacesMessage(FacesMessage.SEVERITY_INFO, "Zone Created",name);
			this.facesContext.addMessage(null, fc);
			this.logger.debug("Added {} to user {}", name, userName);
		} catch (Exception e){
			if(e.getCause() instanceof PersistenceException) {
				FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error processing", e.getCause().getCause().getCause().getMessage());
				this.facesContext.addMessage(null, error);
			} else {
				this.logger.debug("{}", e);
				FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error processing", e.getMessage());
				this.facesContext.addMessage(null, error);
			}
		}
		this.newDomain = new Domain();
	}
	
	

	/**
	 * @return the selectedZones
	 */
	public List<Domain> getSelectedZones() {
		return this.selectedZones;
	}

	/**
	 * @param selectedZones the selectedZones to set
	 */
	public void setSelectedZones(List<Domain> selectedZones) {
		this.selectedZones = selectedZones;
	}

	/**
	 * 
	 */
	public void deleteSelected(){
		for(Domain d : selectedZones){
			this.zoneDAO.deleteZone(d);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Zone Deleted", d.getName());
			this.facesContext.addMessage(null, message);
		}
	}

	/**
	 * @return the lazyModel
	 */
	public LazyDataModel<Domain> getLazyModel() {
		return lazyModel;
	}

	/**
	 * @param lazyModel the lazyModel to set
	 */
	public void setLazyModel(LazyDataModel<Domain> lazyModel) {
		this.lazyModel = lazyModel;
	}

	

}
