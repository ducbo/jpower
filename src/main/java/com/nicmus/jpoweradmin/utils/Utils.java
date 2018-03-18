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
package com.nicmus.jpoweradmin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nicmus.jpoweradmin.model.orm.Record;
import com.nicmus.jpoweradmin.model.orm.Record.TYPE;

@ApplicationScoped
public class Utils implements Serializable{
	
	private static final long serialVersionUID = -8564930390779165955L;

	@Produces
	@JPowerAdmin
	@PersistenceContext(unitName="JPowerAdmin")
	private EntityManager entityManager;
	
	@Produces
	@PowerDNS
	@PersistenceContext(unitName="pdns") 
	private EntityManager powerDNSEntityManager;

	@Resource(name="java:jboss/mail/Default")
	private Session mailSession;
	
	@Produces
	@RequestScoped
	public FacesContext getFacesContext(){
		return FacesContext.getCurrentInstance();
	}
	
	/**
	 * Get a logger for the given class
	 * @param injectionPoint
	 * @return
	 */
	@Produces
	public Logger getLogger(InjectionPoint injectionPoint){
		return LoggerFactory.getLogger(injectionPoint.getClass());
	}

	@Produces
	public MessageDigest getMessageDigest() throws NoSuchAlgorithmException, IOException{
		return MessageDigest.getInstance(this.getProperties().getProperty("message.digest"));
	}
	
	@Named
	@Produces
	public TYPE[] getRecordTypes(){
		return Record.TYPE.values();
	}
	
	@Named
	@Produces
	public String[] getTimeZones() {
		return TimeZone.getAvailableIDs();
	}
	
	@Named
	@Produces
	public List<Locale> getLocales() {
		Locale[] availableLocales = Locale.getAvailableLocales();
		List<Locale> englishLocales = Arrays.asList(availableLocales).stream().filter(l -> (l.getLanguage().equals("en") && !l.getDisplayCountry().isEmpty())).collect(Collectors.toList());
		return englishLocales;
	}
	
	/**
	 * 
	 * @return
	 * @throws MessagingException
	 */
	@Produces
	@ForgottenPasswordEmail
	public MimeMessage getForgottenPasswordMessage() throws MessagingException {
		MimeMessage mimeMessage = new MimeMessage(this.mailSession);
		mimeMessage.setFrom("info@nicmus.com");
		mimeMessage.setSubject("Password reset for");
		return mimeMessage;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Produces
	public String getForgottenPasswordBody() throws IOException {
		StringBuffer buffer = new StringBuffer();
		try(InputStream resourceAsStream = this.getClass().getResourceAsStream("/password-email.txt")){
			BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream));
			String curLine = null;
			while((curLine = in.readLine())!=null) {
				buffer.append(curLine);
				buffer.append(System.lineSeparator());
			}
		}
		
		return buffer.toString();
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Produces
	public Properties getProperties() throws IOException {
		Properties properties = new Properties();
		try(InputStream in = this.getClass().getResourceAsStream("/jpoweradmin.properties")){
			properties.load(in);
		}
		return properties;
	}
}
