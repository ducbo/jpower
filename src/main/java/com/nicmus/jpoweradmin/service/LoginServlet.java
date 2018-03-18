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

import java.io.IOException;
import java.util.Date;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.nicmus.jpoweradmin.model.orm.CurrentUser;
import com.nicmus.jpoweradmin.model.orm.FailedLogin;
import com.nicmus.jpoweradmin.model.orm.LoginHistory;

@WebServlet(name="login", urlPatterns="/checklogin")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = -8605848760125317960L;

	@Inject
	private Event<LoginHistory> loginEvent;
	
	@Inject
	private Event<FailedLogin> failedLoginEvent;
	
	@Inject
	private Logger logger;
	
	@Inject
	private CurrentUser currentUser;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.processReqest(req, resp);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.processReqest(req, resp);
	}
	
	protected void processReqest(HttpServletRequest request, HttpServletResponse response){
		String username = request.getParameter("j_username");
		String password = request.getParameter("j_password");
		this.logger.debug("Username/password {}/{}", username,password);
		try {
			request.login(username, password);
			this.logger.debug("Successfull authentication!!!");
			this.currentUser.setUserName(username);
			String remoteAddr = request.getRemoteAddr();
			String remoteHost = request.getRemoteHost();
			LoginHistory loginHistory = new LoginHistory();
			loginHistory.setUsername(username);
			loginHistory.setIp(remoteAddr);
			loginHistory.setHostName(remoteHost);
			loginEvent.fire(loginHistory);
			
			response.sendRedirect("members/zones.jsf");
		} catch (ServletException | IOException e) {
			//e.printStackTrace();
			this.failedLoginEvent.fire(new FailedLogin(username, new Date()));
			try {
				response.sendRedirect("login.jsf");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	
}
