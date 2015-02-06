/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * 
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.permission;

public abstract class PermissibleInteractive extends PermissibleBase
{
	public abstract void sendMessage( String string );
	
	public abstract boolean kick( String kickMessage );
	
	public abstract boolean isValid();
	
	public void sendMessage( String... msgs )
	{
		for ( String m : msgs )
			sendMessage( m );
	}
}
