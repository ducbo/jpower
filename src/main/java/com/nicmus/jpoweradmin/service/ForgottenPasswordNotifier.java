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

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.nicmus.jpoweradmin.model.orm.ForgottenPasswordMailEvent;
import com.nicmus.jpoweradmin.utils.ForgottenPasswordEmail;

@Stateless
public class ForgottenPasswordNotifier {
	
	private static final String URL = "http://www.customdns.ca";
	
	@Inject @ForgottenPasswordEmail
	private MimeMessage emailMessage;
	
	@Inject
	private String messageBody;
	
	@Asynchronous
	public void sendEmail(@Observes ForgottenPasswordMailEvent mailEvent) {
		try {
			this.emailMessage.setRecipient(RecipientType.TO, new InternetAddress(mailEvent.getEmail()));
			this.emailMessage.setSubject(this.emailMessage.getSubject() + " " + mailEvent.getUser());
			String body = this.messageBody.replaceAll("\\{userName\\}", mailEvent.getUser());
			body = body.replaceAll("\\{newPasswordLink\\}", URL.concat("/resetpass.jsf?id=" + mailEvent.getToken()));
			this.emailMessage.setText(body);
			Transport.send(this.emailMessage);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
