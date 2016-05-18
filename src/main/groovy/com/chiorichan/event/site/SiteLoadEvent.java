/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.event.site;

import com.chiorichan.event.Cancellable;
import com.chiorichan.event.application.ApplicationEvent;
import com.chiorichan.site.Site;

public class SiteLoadEvent extends ApplicationEvent implements Cancellable
{
	Site site;
	boolean cancelled;
	
	public SiteLoadEvent( Site site )
	{
		this.site = site;
	}
	
	public Site getSite()
	{
		return site;
	}
	
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}
	
	@Override
	public void setCancelled( boolean cancel )
	{
		cancelled = cancel;
	}
}