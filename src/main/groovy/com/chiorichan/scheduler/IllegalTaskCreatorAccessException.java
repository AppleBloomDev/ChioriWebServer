/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2014 Chiori-chan. All Right Reserved.
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.scheduler;

/**
 * Thrown when a creator attempts to interact with the server when it is not enabled
 */
@SuppressWarnings( "serial" )
public class IllegalTaskCreatorAccessException extends RuntimeException
{
	/**
	 * Creates a new instance of <code>IllegalPluginAccessException</code> without detail message.
	 */
	public IllegalTaskCreatorAccessException()
	{
	}
	
	/**
	 * Constructs an instance of <code>IllegalPluginAccessException</code> with the specified detail message.
	 * 
	 * @param msg
	 *             the detail message.
	 */
	public IllegalTaskCreatorAccessException(String msg)
	{
		super( msg );
	}
}
