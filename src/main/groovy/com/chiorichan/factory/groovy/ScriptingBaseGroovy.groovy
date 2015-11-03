/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2015 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.factory.groovy

import com.chiorichan.account.Account
import com.chiorichan.account.AccountManager
import com.chiorichan.factory.ScriptingFactory
import com.chiorichan.factory.api.Server
import com.chiorichan.http.HttpCode
import com.chiorichan.http.HttpRequestWrapper
import com.chiorichan.http.HttpResponseWrapper
import com.chiorichan.permission.PermissibleEntity
import com.chiorichan.permission.Permission
import com.chiorichan.permission.PermissionResult
import com.chiorichan.session.Session
import com.chiorichan.site.Site
import com.chiorichan.site.SiteManager


/**
 * Used as the Groovy Scripting Base and provides scripts with custom builtin methods
 */
@Deprecated
public abstract class ScriptingBaseGroovy extends ScriptingBaseJava
{
	/**
	 * Same as {@link ScriptingBaseJava#var_export(obj)} but instead prints the result to the buffer
	 * Based on method of same name in PHP
	 * @param obj
	 *       The object you wish to dump
	 */
	void var_dump ( Object... obj )
	{
		println var_export( obj )
	}

	/**
	 * Returns the current HttpRequestWrapper instance
	 * XXX This is set inside the {@link HttpRequestWrapper#sessionStarted} and {@link SessionWrapper#startSession}, this needs looking over for other types
	 *
	 * @return
	 *      current instance
	 */
	HttpRequestWrapper getRequest()
	{
		return request
	}

	/**
	 * Returns the current HttpResponseWrapper instance
	 * XXX This is set inside the {@link HttpRequestWrapper#sessionStarted} and {@link SessionWrapper#startSession}, this needs looking over for other types
	 *
	 * @return
	 *      current instance
	 */
	HttpResponseWrapper getResponse()
	{
		return response
	}

	/**
	 * Return the current session for this request
	 *
	 * @return
	 *      current session
	 */
	Session getSession()
	{
		return request.getSession()
	}

	/**
	 * Alias for println
	 * Based on method of same name in PHP
	 * @param var
	 *       The string you wish to print
	 */
	void echo( String var )
	{
		println var
	}

	Object getPropertySafe( String name )
	{
		try
		{
			return getProperty( name );
		}
		catch ( MissingPropertyException e )
		{
			return null;
		}
	}

	PermissibleEntity getEntity()
	{
		getAccount().getEntity()
	}

	/**
	 * Get the account matching specified uid
	 * @param uid
	 *       The uid you wish to use
	 * @return
	 *       The found account, will return null if none found
	 */
	Account getAccount( String uid )
	{
		Account result = AccountManager.INSTANCE.getAccount( uid )

		if ( result == null )
			result = AccountManager.INSTANCE.getAccountPartial( uid )

		return result
	}

	Account[] getAccounts( String query )
	{
		return AccountManager.INSTANCE.getAccounts( query )
	}

	Account[] getAccounts( String query, int limit )
	{
		return AccountManager.INSTANCE.getAccounts( query, limit )
	}

	/**
	 * Returns the current logged in account
	 * @return
	 *      The current account, will return null if no one is logged in
	 */
	Account getAccount()
	{
		return request.getSession().account()
	}

	String getAcctId()
	{
		return isLoginPresent() ?: getAccount().getAcctId()
	}

	boolean isLoginPresent()
	{
		return request.getSession().isLoginPresent()
	}

	@Deprecated
	boolean getAcctState()
	{
		return request.getSession().isLoginPresent()
	}

	@Deprecated
	boolean getAccountState()
	{
		return request.getSession().isLoginPresent()
	}

	/**
	 * Returns an instance of the current site
	 * @return
	 *       The current site
	 */
	Site getSite()
	{
		return getRequest().getSite()
	}

	/**
	 * Return the unique Id for the current site
	 *
	 * @return The current site Id
	 */
	String getSiteId()
	{
		return getSite().getSiteId()
	}

	/**
	 * Converts the specified http status code to a message
	 * @param errNo
	 *       The http status code
	 * @return
	 *       The http status message
	 */
	String getStatusDescription( int errNo )
	{
		return HttpCode.msg( errNo )
	}

	/**
	 * Same as @link url_to( null )
	 */
	String url_to()
	{
		return url_to( null )
	}

	/**
	 * Returns a valid http url address
	 * Used to produce absolute uri's within scripts
	 * ex: url_to( "css" ) + "stylesheet.css"
	 * @param subdomain
	 *       The subdomain
	 * @return
	 *       A valid formatted http uri
	 */
	String url_to( String subdomain )
	{
		String url = "http://"

		if ( subdomain != null && !subdomain.isEmpty() )
			url += subdomain + "."

		if ( request.getSite() != null )
			url += request.getSite().getDomain() + "/"
		else
			url += SiteManager.INSTANCE.getDefaultSite().getDomain() + "/"

		return url
	}

	/**
	 * Returns the uri to the login page
	 * @return
	 *       The login uri
	 */
	String url_to_login()
	{
		if ( request.getSite() == null )
			return "/login"

		return request.getSite().getYaml().getString( "scripts.login-form", "/login" )
	}

	/**
	 * Returns the to log current account out
	 * @return
	 *       The logout uri
	 */
	String url_to_logout()
	{
		return url_to_login + "?logout"
	}

	Object include( String pack )
	{
		return Server.packageContext( pack ).eval()
	}

	Object require( String pack )
	{
		return Server.packageContext( pack ).require().eval()
	}

	boolean isAdmin()
	{
		getSession().isAdmin()
	}

	boolean isOp()
	{
		getSession().isOp()
	}

	PermissionResult checkPermission( String perm )
	{
		getSession().checkPermission( perm )
	}

	PermissionResult checkPermission( Permission perm )
	{
		getSession().checkPermission( perm )
	}

	PermissionResult requirePermission( String perm )
	{
		getSession().requirePermission( perm )
	}

	PermissionResult requirePermission( Permission perm )
	{
		getSession().requirePermission( perm )
	}

	ScriptingFactory getEvalFactory()
	{
		return getRequest().getEvalFactory()
	}
}
