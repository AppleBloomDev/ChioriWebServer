/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.permission;

public class RankingException extends Exception
{
	private static final long serialVersionUID = -328357153481259189L;
	
	protected PermissibleEntity target = null;
	protected PermissibleEntity promoter = null;
	
	public RankingException( String message, PermissibleEntity target, PermissibleEntity promoter )
	{
		super( message );
		this.target = target;
		this.promoter = promoter;
	}
	
	public PermissibleEntity getTarget()
	{
		return target;
	}
	
	public PermissibleEntity getPromoter()
	{
		return promoter;
	}
}
