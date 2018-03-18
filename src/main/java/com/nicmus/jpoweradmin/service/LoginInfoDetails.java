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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.nicmus.jpoweradmin.model.orm.LoginHistory;
import com.nicmus.jpoweradmin.model.orm.LoginHistory_;
import com.nicmus.jpoweradmin.utils.JPowerAdmin;

@Named
@SessionScoped
public class LoginInfoDetails implements Serializable {
	private static final long serialVersionUID = 7057657084163593743L;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Inject @JPowerAdmin
	private EntityManager entityManager;
	
	@Inject
	private FacesContext facesContext;
	private boolean messageShown = false;

	public void showLoginInfo(ComponentSystemEvent event){
		if(this.facesContext.getExternalContext().getUserPrincipal() != null && !this.messageShown){
			
			CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
			CriteriaQuery<LoginHistory> loginCriteriaQuery = criteriaBuilder.createQuery(LoginHistory.class);
			Root<LoginHistory> from = loginCriteriaQuery.from(LoginHistory.class);
			
			String userName = this.facesContext.getExternalContext().getUserPrincipal().getName();
			Predicate userNamePredicate = criteriaBuilder.equal(from.get(LoginHistory_.username), userName);
			loginCriteriaQuery.select(from).where(userNamePredicate);
			loginCriteriaQuery.orderBy(criteriaBuilder.desc(from.get(LoginHistory_.dateCreated)));
			
			TypedQuery<LoginHistory> query = this.entityManager.createQuery(loginCriteriaQuery);
			query.setMaxResults(2);
			
			List<LoginHistory> resultList = query.getResultList();
			//we have a login - the second entry in the login history is the last login
			if(resultList.size() == 2){
				LoginHistory loginHistory = resultList.get(1);
				String message = "Last login on " + DATE_FORMAT.format(loginHistory.getDateCreated()) + " from " + loginHistory.getHostName();
				FacesMessage facesMessage = new FacesMessage("", message);
				this.facesContext.addMessage(null, facesMessage);
			} 
			this.messageShown = true;
		}
		
	}
	
}
