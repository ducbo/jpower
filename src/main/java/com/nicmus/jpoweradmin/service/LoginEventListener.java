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

import java.util.Locale;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.nicmus.jpoweradmin.model.orm.LoginHistory;
import com.nicmus.jpoweradmin.model.orm.User;
import com.nicmus.jpoweradmin.model.orm.UserPreferences;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;

/**
 * The login event saves the user/ip data on successful login to the log
 * @author jsabev
 *
 */
@Stateless
public class LoginEventListener {
	
	@Inject
	private AccountServiceDAO accountServiceDAO;
	
	
	@Inject
	private UserPreferences userPreferences;
	
	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	/**
	 * Save the last login info to the log
	 * @param loginEvent
	 */
	public void loginEventHandler(@Observes LoginHistory loginEvent){
		this.entityManager.persist(loginEvent);
		this.setUserPreferences(loginEvent.getUsername());
	}
	
	/**
	 * 
	 * @param userName
	 */
	public void setUserPreferences(String userName) {
		User user = this.accountServiceDAO.getUser(userName);
		userPreferences.setTimeZone(TimeZone.getTimeZone(user.getTimeZoneId()));
		String localeName = user.getLocaleName();
		Locale newLocale = Locale.forLanguageTag(localeName);
		userPreferences.setLocale(newLocale);
		userPreferences.setLocaleName(localeName);
	}
	
}
