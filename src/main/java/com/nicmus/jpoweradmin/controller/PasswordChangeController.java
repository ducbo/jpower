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

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.CurrentUser;
import com.nicmus.jpoweradmin.model.orm.Password;
import com.nicmus.jpoweradmin.service.PasswordChangeService;

@Named
@RequestScoped
public class PasswordChangeController {

	@Inject
	private FacesContext facesContext;
	
	@Inject
	private PasswordChangeService passwordChangeService;
	
	
	@Inject
	private Logger logger;
	
	@Inject
	private Password password;

	@Inject
	private CurrentUser currentUser;
	
	private String currentPassword;
	
	/**
	 * @return 
	 * 
	 */
	public String changePassword() {
		this.logger.info("Password {} - {}", this.getPassword().getPassword(), this.password.getConfirmPassword());
		//check to see if the old password matches
		//String userName = this.facesContext.getExternalContext().getUserPrincipal().getName();
		String userName = this.currentUser.getUserName();
		if(!this.passwordChangeService.passwordMatches(userName, this.getCurrentPassword())) {
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"The current password does not match",""));
			return null;
		}
		//check the new passwords match
		Password password = this.getPassword();
		if(!password.getPassword().equals(password.getConfirmPassword())) {
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Passwords do not match",""));
			return null;
		}
		//update the password.
		this.passwordChangeService.changePassword(userName, password.getPassword());
		this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Password Changed",""));
		return "account";
	}

	/**
	 * @return the password
	 */
	public Password getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(Password password) {
		this.password = password;
	}

	/**
	 * @return the currentPassword
	 */
	public String getCurrentPassword() {
		return currentPassword;
	}

	/**
	 * @param currentPassword the currentPassword to set
	 */
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	
	
	
}
