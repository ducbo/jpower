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

import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.NoEmailException;
import com.nicmus.jpoweradmin.model.orm.Password;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.service.ForgottenPasswordService;
import com.nicmus.jpoweradmin.service.PasswordChangeService;


@Named
@ViewScoped
public class ForgottenPasswordController implements Serializable {
	private static final long serialVersionUID = -8267463686882823162L;

	@Inject
	private Logger logger;
	
	@Inject
	private Password password;

	
	private String id;
	
	//boolean indicating if password id token is valid
	private boolean validId;
	
	//username of account whose password to reset
	private String accountName;
	
	@Inject
	private ForgottenPasswordService forgottenPasswordService;
	
	@Inject
	private PasswordChangeService passwordChangeService;
	
	@Inject
	private FacesContext facesContext;
	
	/**
	 * 
	 */
	public void checkId() {
		try {
			User user = this.forgottenPasswordService.getUser(this.id);
			this.setAccountName(user.getUserName());
			this.setValidId(true);
		} catch (EJBException e) {
			Throwable cause = e.getCause();
			if(cause instanceof NoResultException) {
				this.setValidId(false);
			}
		}
	}
	
	/**
	 * 
	 */
	public void requestPasswordResetToken() {
		//1. Get the user matching the ID
		try {
			this.forgottenPasswordService.requestResetToken(this.accountName);
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Password reset instructions sent to account's email address",""));
		} catch (EJBException e){
			if(e.getCause() instanceof NoResultException) {
				this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No such user found",""));
				return;
			} else {
				throw e;
			}
		} catch (NoEmailException e) {
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"No email address for user " + this.accountName,""));
			return;
		}
		
	}
	
	
	/**
	 * 
	 */
	public String reset() {
		if(this.password.getPassword().equals(this.password.getConfirmPassword())) {
			this.logger.info("Passwords match {} = {}", this.password.getPassword(), this.password.getConfirmPassword());
			this.passwordChangeService.changePassword(this.getAccountName(), this.password.getPassword());
			this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Password changed",""));
			return "login";
		} else {
			this.logger.info("Passwords do not match");
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public Password getPassword() {
		return password;
	}

	/**
	 * 
	 * @param password
	 */
	public void setPassword(Password password) {
		this.password = password;
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isValidId() {
		return validId;
	}

	/**
	 * 
	 * @param validId
	 */
	public void setValidId(boolean validId) {
		this.validId = validId;
	}

	/**
	 * 
	 * @return
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * 
	 * @param accountName
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	
	
	
}
