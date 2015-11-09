/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2015 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.http;

import java.util.Map;
import java.util.Map.Entry;

import com.chiorichan.session.SessionManager;
import com.chiorichan.tasks.TaskManager;
import com.chiorichan.tasks.Ticks;
import com.chiorichan.tasks.Timings;
import com.chiorichan.util.SecureFunc;
import com.google.common.collect.Maps;

/**
 * Stores string messages that can be referenced later.
 * Messages are removed once retrieved or server is restarted.
 * 
 * TODO I know more can be done with this class one day in the future
 */
public class MessageRepo
{
	public static class Message
	{
		private String val;
		private int level;
		private int timecode;
		
		public Message( String val, int level )
		{
			this.val = val;
			this.level = level;
			timecode = Timings.epoch();
		}
		
		public int level()
		{
			return level;
		}
		
		public String levelString()
		{
			switch ( level )
			{
				case DEFAULT:
					return "default";
				case MESSAGE:
					return "primary";
				case SUCCESS:
					return "success";
				case INFO:
					return "info";
				case WARNING:
					return "warning";
				case DANGER:
					return "danger";
				default:
					return "";
			}
		}
		
		public int timecode()
		{
			return timecode;
		}
		
		public String value()
		{
			return val;
		}
	}
	
	static
	{
		TaskManager.INSTANCE.scheduleSyncRepeatingTask( SessionManager.INSTANCE, Ticks.HOUR_5, Ticks.HOUR_3, new Runnable()
		{
			@Override
			public void run()
			{
				for ( Entry<String, Message> msg : MessageRepo.messages.entrySet() )
					if ( msg.getValue() != null && msg.getValue().timecode() < Timings.epoch() - Timings.HOUR_4 )
						messages.remove( msg.getKey() );
			}
		} );
	}
	
	public static final int DEFAULT = 0;
	public static final int MESSAGE = 1;
	public static final int SUCCESS = 2;
	public static final int INFO = 3;
	public static final int WARNING = 4;
	public static final int DANGER = 5;
	
	private static Map<String, Message> messages = Maps.newHashMap();
	
	public static Message retrive( String key )
	{
		return messages.remove( key );
	}
	
	public static String store( String val )
	{
		return store( val, DEFAULT );
	}
	
	public static String store( String val, int level )
	{
		String key = SecureFunc.randomize( "dharma1n1t1at1ve" );
		messages.put( key, new Message( val, level ) );
		return key;
	}
}