/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.scheduler;

public interface TaskCreator
{
	/**
	 * Returns a value indicating whether or not this creator is currently enabled
	 * 
	 * @return true if this creator is enabled, otherwise false
	 */
	public boolean isEnabled();
	
	/**
	 * Returns the name of the creator.
	 * <p>
	 * This should return the bare name of the creator and should be used for comparison.
	 * 
	 * @return name of the creator
	 */
	public String getName();
}
