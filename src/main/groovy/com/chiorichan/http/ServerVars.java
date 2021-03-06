/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.http;

/**
 * Server Variable Enum
 * Used to map HTTP headers to their string variance
 */
public enum ServerVars
{
	SERVER_ADDR, SERVER_NAME, SERVER_ID, SERVER_SOFTWARE, SERVER_PROTOCOL, REQUEST_METHOD, REQUEST_TIME, REQUEST_URI, QUERY_STRING, DOCUMENT_ROOT, HTTP_VERSION, HTTP_ACCEPT, HTTP_ACCEPT_CHARSET, HTTP_ACCEPT_ENCODING, HTTP_ACCEPT_LANGUAGE, HTTP_CONNECTION, HTTP_HOST, HTTP_USER_AGENT, HTTPS, REMOTE_ADDR, REMOTE_HOST, REMOTE_PORT, REMOTE_USER, SERVER_ADMIN, SERVER_IP, SERVER_PORT, SERVER_SIGNATURE, AUTH_DIGEST, AUTH_USER, AUTH_PW, AUTH_TYPE, CONTENT_LENGTH, SESSION, PHP_SELF, HTTP_X_REQUESTED_WITH, SERVER_VERSION;

	public static ServerVars parse( String key )
	{
		for ( ServerVars sv : ServerVars.values() )
			if ( sv.name().equalsIgnoreCase( key ) || ( "PHP_" + sv.name() ).equalsIgnoreCase( key ) )
				return sv;

		return null;
	}
}
