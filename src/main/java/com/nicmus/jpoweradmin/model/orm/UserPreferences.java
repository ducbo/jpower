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
package com.nicmus.jpoweradmin.model.orm;

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class UserPreferences implements Serializable {
	private static final long serialVersionUID = -7910483303417007035L;

	private TimeZone timeZone;
	private Locale locale;
	private String localeName;
	
	@PostConstruct
	public void init() {
		this.timeZone = TimeZone.getDefault();
		this.locale = Locale.getDefault();
		this.localeName = this.locale.getLanguage()+"-"+this.locale.getCountry();
	}
	
	/**
	 * @return the timeZone
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}
	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the localeName
	 */
	public String getLocaleName() {
		return localeName;
	}

	/**
	 * @param localeName the localeName to set
	 */
	public void setLocaleName(String localeName) {
		this.localeName = localeName;
	}

	
	
}
