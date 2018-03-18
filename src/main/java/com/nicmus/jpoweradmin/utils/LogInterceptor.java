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

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jsabev
 *
 */
@Loggable
@Interceptor
public class LogInterceptor implements Serializable {
	private static final long serialVersionUID = -5223587460267208794L;

	@AroundInvoke
	public Object logMethodEntry(InvocationContext invocationContext) throws Exception{
		Method method = invocationContext.getMethod();
		Class<?> declaringClass = method.getDeclaringClass();
		Logger logger = LoggerFactory.getLogger(declaringClass);
		
		Object[] parameters = invocationContext.getParameters();
		logger.info("Entering {} {}", method, parameters);
		logger.info("Leaving {} {}", method, parameters);
		return invocationContext.proceed();
	}

}
