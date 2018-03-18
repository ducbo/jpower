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

import java.security.MessageDigest;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import com.nicmus.jpoweradmin.model.orm.Role;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.UserPreferences;
import com.nicmus.jpoweradmin.utils.Loggable;

public class RegistrationService {
	
	@Inject
	private UserPreferences userPreferences;
	
	@Inject
	private RegistrationDAO registrationDAO;
	
	@Inject
	private MessageDigest messageDigest;
		
	@Loggable
	public void register(User user){
		
		this.messageDigest.update(user.getPassword().getBytes());
		byte[] messageDigest = this.messageDigest.digest();
		String passwordHashBase64 = DatatypeConverter.printBase64Binary(messageDigest);
		user.setPassword(passwordHashBase64);
		Role role = new Role();
		//only the root user has the ROOT privilege by default
		if(user.getUserName().equals("root")) {
			role.setRole(Role.ROLE.ROOT);
		} else {
			role.setRole(Role.ROLE.USER);
		}
		user.getRoles().add(role);
		role.setUser(user);

		//set the default locale preferences
		user.setLocaleName(this.userPreferences.getLocaleName());
		user.setTimeZoneId(this.userPreferences.getTimeZone().getID());
		//save the user
		this.registrationDAO.register(user);
	}
	
}


