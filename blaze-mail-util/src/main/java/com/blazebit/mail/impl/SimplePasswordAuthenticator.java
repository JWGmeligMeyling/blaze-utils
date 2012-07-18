/*
 * Copyright 2011 Blazebit
 */
package com.blazebit.mail.impl;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 
 * @author Christian Beikov
 * @since 0.1.2
 */
public class SimplePasswordAuthenticator extends Authenticator {

	private final String user;
	private final String password;

	public SimplePasswordAuthenticator(String user, String password) {
		this.user = user;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}
}
