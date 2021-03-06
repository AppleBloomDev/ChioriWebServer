/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.factory.event;

import com.chiorichan.event.AbstractEvent;
import com.chiorichan.event.Cancellable;
import com.chiorichan.factory.ScriptingContext;

public class PostEvalEvent extends AbstractEvent implements Cancellable
{
	private ScriptingContext context;
	private boolean cancelled;
	
	public PostEvalEvent( ScriptingContext context )
	{
		this.context = context;
	}
	
	public ScriptingContext context()
	{
		return context;
	}
	
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}
	
	@Override
	public void setCancelled( boolean cancelled )
	{
		this.cancelled = cancelled;
	}
}
