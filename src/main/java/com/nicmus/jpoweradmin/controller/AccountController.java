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

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.CurrentUser;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.UserPreferences;
import com.nicmus.jpoweradmin.service.AccountServiceDAO;
import com.nicmus.jpoweradmin.service.GeneralDAO;

@Named
@ViewScoped
public class AccountController implements Serializable {
	
	private static final long serialVersionUID = 3455754276193901320L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserPreferences userPreferences;
	
	@Inject
	private CurrentUser currentUser;
	
	@Inject
	private AccountServiceDAO accountServiceDAO;
	
	@Inject
	private GeneralDAO generalDAO;
	
	@Inject
	private FacesContext facesContext;
	
	private User user;
	
	private String confirmDelete;
	
	@PostConstruct
	public void loadSessionUser() {
		String name = this.currentUser.getUserName();
		this.user = this.accountServiceDAO.getUser(name);
	}
	
	/**
	 * Time zone
	 */
	public void timeZoneChanged() {
		String timeZoneId = this.userPreferences.getTimeZone().getID();
		String userName = this.currentUser.getUserName();
		this.userPreferences.setTimeZone(TimeZone.getTimeZone(timeZoneId));
		this.accountServiceDAO.updateTimeZonePreference(userName, timeZoneId);
		this.facesContext.addMessage(null, new FacesMessage("Time Zone Changed", "Time Zone Changed to " + timeZoneId));
	}
	
	/**
	 * Change the user locale
	 */
	public void localeChanged() {
		
		String localeName = this.userPreferences.getLocaleName();
		this.logger.info("Locale selected {}", localeName);
		String userName = this.currentUser.getUserName();
		Locale newLocale = Locale.forLanguageTag(localeName);
		this.userPreferences.setLocale(newLocale);
		this.logger.info("Locale changed to {}",newLocale.getDisplayName());
		this.facesContext.getViewRoot().setLocale(newLocale);
		this.accountServiceDAO.updateLocalePreferences(userName, localeName);
		this.facesContext.addMessage(null, new FacesMessage("Locale Changed", "Locale Changed to " + newLocale.getDisplayName()));
	}
	
	/**
	 * Save the user requested changes
	 */
	public void save() {
		String userName = this.currentUser.getUserName();
		User userToupdate = this.accountServiceDAO.getUser(userName);
		userToupdate.setFirstName(this.user.getFirstName());
		userToupdate.setLastName(this.user.getLastName());
		userToupdate.setEmail(this.user.getEmail());
		
		this.generalDAO.update(userToupdate);
		this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"User Preferences saved",""));
		this.loadSessionUser();
	}
	
	/**
	 * Delete action for the given user
	 * @return 
	 * @throws IOException 
	 */
	public String delete() throws IOException {
		if(this.confirmDelete == null || !this.confirmDelete.equalsIgnoreCase("yes")) {
			return null;
		}
		String userName = this.currentUser.getUserName();
		this.accountServiceDAO.deleteUserAccount(userName);
		Flash flash = this.facesContext.getExternalContext().getFlash();
		flash.setKeepMessages(true);
		this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "User marked for deletion",""));
		return "zones?faces-redirect=true";
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the confirmDelete
	 */
	public String getConfirmDelete() {
		return confirmDelete;
	}

	/**
	 * @param confirmDelete the confirmDelete to set
	 */
	public void setConfirmDelete(String confirmDelete) {
		this.confirmDelete = confirmDelete;
	}
	
	
}
