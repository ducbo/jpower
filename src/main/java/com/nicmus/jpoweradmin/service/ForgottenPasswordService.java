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
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.nicmus.jpoweradmin.model.orm.ForgottenPasswordMailEvent;
import com.nicmus.jpoweradmin.model.orm.NoEmailException;
import com.nicmus.jpoweradmin.model.orm.PasswordResets;
import com.nicmus.jpoweradmin.model.orm.User;

public class ForgottenPasswordService implements Serializable{
	private static final long serialVersionUID = 9047194942992667510L;
	
	@Inject
	private AccountServiceDAO accountsDAO;
	
	@Inject
	private GeneralDAO generalDAO;
	
	@Inject
	private Event<ForgottenPasswordMailEvent> forgottenPasswordMail;
	
	/**
	 * Request a password reset token for the given username
	 * @param userName
	 * @throws NoEmailException 
	 */
	public void requestResetToken(String userName) throws NoEmailException {
		User user = this.accountsDAO.getUser(userName);
		if(user.getEmail() == null || user.getEmail().isEmpty()) {
			throw new NoEmailException(userName + " does not have an email address associated with account"); 
		}
		
		
		//generate and save the GUID
		UUID randomUUID = UUID.randomUUID();
		String guid = randomUUID.toString();
		
		PasswordResets resetGUID = new PasswordResets();
		resetGUID.setGuid(guid);
		resetGUID.setUserName(userName);
		this.generalDAO.save(resetGUID);
		
		String email = user.getEmail();
		this.forgottenPasswordMail.fire(new ForgottenPasswordMailEvent(user.getUserName(), email, guid));
	}

	/**
	 * 
	 * @param guid
	 * @return
	 */
	public User getUser(String guid) {
		String userName = this.accountsDAO.getUserName(guid);
		return this.accountsDAO.getUser(userName);
	}
}
