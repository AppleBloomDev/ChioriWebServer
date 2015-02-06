/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * 
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.event.account;

import com.chiorichan.account.Account;
import com.chiorichan.event.HandlerList;

/**
 * This event is called after a User registers or unregisters a new plugin channel.
 */
public abstract class AccountChannelEvent extends AccountEvent
{
	private static final HandlerList handlers = new HandlerList();
	private final String channel;
	
	public AccountChannelEvent( final Account<?> user, final String channel )
	{
		super( user );
		this.channel = channel;
	}
	
	public final String getChannel()
	{
		return channel;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
