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
import java.security.MessageDigest;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import com.nicmus.jpoweradmin.model.orm.User;

public class PasswordChangeService implements Serializable {
	private static final long serialVersionUID = -1741672740288659236L;

	@Inject
	private MessageDigest messageDigest;
	
	@Inject
	private AccountServiceDAO accountServiceDAO;
	
	@Inject
	private GeneralDAO generalDAO;
	
	/**
	 * Check to the user password against the provided password.
	 * 
	 * @param username
	 * @param password
	 * @return true if the passwords match/false otherwise
	 */
	public boolean passwordMatches(String username, String password) {
		User user = this.accountServiceDAO.getUser(username);
		byte[] digest = this.messageDigest.digest(password.getBytes());
		String printBase64Binary = DatatypeConverter.printBase64Binary(digest);
		return printBase64Binary.equals(user.getPassword());
	}
	
	/**
	 * Change the password for the given user name
	 * @param username the username whose password to update
	 * @param password the plain-text password
	 */
	public void changePassword(String username, String password) {
		byte[] digest = this.messageDigest.digest(password.getBytes());
		String hashed = DatatypeConverter.printBase64Binary(digest);
		User user = this.accountServiceDAO.getUser(username);
		user.setPassword(hashed);
		this.generalDAO.update(user);
	}
}
