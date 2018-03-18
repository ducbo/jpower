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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.nicmus.jpoweradmin.model.orm.CurrentUser;
import com.nicmus.jpoweradmin.model.orm.Role;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.service.AccountServiceDAO;
import com.nicmus.jpoweradmin.service.ZoneImportService;

@Named
@ViewScoped
@RolesAllowed({"ROOT"})
public class AdminController implements Serializable {
	private static final long serialVersionUID = -1405453812063525934L;
	
	@Inject
	private AccountServiceDAO accountSericeDAO;
	
	@Inject
	private ZoneImportService zoneImportService;
	
	private LazyDataModel<User> userModel;
	
	@Inject
	private FacesContext facesContext;
	
	@Inject
	private CurrentUser currentUser;
	
	@PostConstruct
	public void init() {
		this.userModel = new LazyDataModel<User>() {
			private static final long serialVersionUID = -3991964307829678282L;

			/* (non-Javadoc)
			 * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
			 */
			@Override
			public List<User> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
				List<User> users = accountSericeDAO.getUsers(first, pageSize, sortField, sortOrder, filters);
				long userCount = accountSericeDAO.getUserCount(filters);
				this.setRowCount(Math.toIntExact(userCount));
				this.setWrappedData(users);
				return users;
			}
			
			
			
			/* (non-Javadoc)
			 * @see org.primefaces.model.LazyDataModel#getRowData(java.lang.String)
			 */
			@Override
			public User getRowData(String rowKey) {
				@SuppressWarnings("unchecked")
				List<User> wrappedData = (List<User>) this.getWrappedData();
				for(User u : wrappedData) {
					if(u.getId() == Integer.parseInt(rowKey)) {
						return u;
					}
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see org.primefaces.model.LazyDataModel#getRowKey(java.lang.Object)
			 */
			@Override
			public Object getRowKey(User object) {
				return object.getId();
			}
		};
	}

	/**
	 * @return the userModel
	 */
	public LazyDataModel<User> getUserModel() {
		return userModel;
	}

	/**
	 * @param userModel the userModel to set
	 */
	public void setUserModel(LazyDataModel<User> userModel) {
		this.userModel = userModel;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Role> getRoles(){
		return new ArrayList<>(this.userModel.getRowData().getRoles());
	}

	/**
	 * toggle the status of the user
	 */
	public void toggle() {
		User user = this.userModel.getRowData();
		String userName = user.getUserName();
		if(user.isDeleted()) {
			this.accountSericeDAO.disableUserAcount(userName);
			this.facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, userName + " marked for deletion",""));
		} else {
			this.accountSericeDAO.reenableUserAccount(userName);
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, userName + " re-enabled",""));
		}
	}
	
	/**
	 * View the user account
	 * @return
	 */
	public String viewZone() {
		User rowData = this.userModel.getRowData();
		this.currentUser.setUserName(rowData.getUserName());
		return "zones";
		
	}
	
	/**
	 * 
	 */
	public void importZones() {
		int importZones = this.zoneImportService.importZones();
		this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, importZones + " zones imported into JPowerAdmin",""));
	}
}
