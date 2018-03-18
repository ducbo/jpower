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
/**
 * 
 */
package com.nicmus.jpoweradmin.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.Password;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.service.RegistrationService;

/**
 * Registers a new user
 * @author jsabev
 *
 */
@Model
public class RegistrationController implements Serializable {
	private static final long serialVersionUID = 2830024271743518745L;

	@Inject
	private Logger logger;
		
	@Produces
	@Named
	private User newUser;
	
	@Inject
	private Password password;
	
	@Inject
	private RegistrationService registrationService;
	
	@Inject
	private FacesContext facesContext;
	
	private boolean tosAccepted;
	
	@PostConstruct
	public void init(){
		this.newUser = new User();
	}
	
	/**
	 * 
	 * @return
	 */
	public String register(){
		this.logger.debug("Trying to register {} ({},{})", this.newUser.getUserName(), this.newUser.getLastName(), this.newUser.getFirstName());
		//verify the Terms of Service are accepted
		if(!this.isTosAccepted()) {
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Terms of Service must be accepted by checking the appropiate checkbox", "");
			this.facesContext.addMessage(null, facesMessage);
			return null;
		}
		
		//set the password of the user
		this.newUser.setPassword(this.password.getPassword());
		//new users are not root users by default.
		
		try {
			this.registrationService.register(this.newUser);
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO,"Registered", "Registration Successfull");
			Flash flash = this.facesContext.getExternalContext().getFlash();
			flash.setKeepMessages(true);
			this.facesContext.addMessage(null, facesMessage);
			this.init();
			return "login?faces-redirect=true";
		} catch (Exception e){
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,"ERROR", e.getCause().getMessage());
			this.facesContext.addMessage(null, facesMessage);
		}
		return null;
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
	 * @return the tosAccepted
	 */
	public boolean isTosAccepted() {
		return tosAccepted;
	}

	/**
	 * @param tosAccepted the tosAccepted to set
	 */
	public void setTosAccepted(boolean tosAccepted) {
		this.tosAccepted = tosAccepted;
	}
	
	
	
	
}
