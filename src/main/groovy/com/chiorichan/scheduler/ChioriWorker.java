/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * 
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.scheduler;


/**
 * Represents a worker thread for the scheduler. This gives information about the Thread object for the task, owner of
 * the task and the taskId. </p> Workers are used to execute async tasks.
 */

public interface ChioriWorker
{
	
	/**
	 * Returns the taskId for the task being executed by this worker.
	 * 
	 * @return Task id number
	 */
	int getTaskId();
	
	/**
	 * Returns the TaskCreator that owns this task.
	 * 
	 * @return The TaskCreator that owns the task
	 */
	TaskCreator getOwner();
	
	/**
	 * Returns the thread for the worker.
	 * 
	 * @return The Thread object for the worker
	 */
	Thread getThread();
	
}
