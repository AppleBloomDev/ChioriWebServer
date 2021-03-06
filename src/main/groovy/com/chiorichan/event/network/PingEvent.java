/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.event.network;

import java.net.InetAddress;

/**
 * Called when a server list ping is coming in.
 */
public class PingEvent extends NetworkEvent
{
	private final InetAddress address;
	private String motd;
	private final int numPlayers;
	private int maxPlayers;

	public PingEvent( final InetAddress address, final String motd, final int numPlayers, final int maxPlayers )
	{
		this.address = address;
		this.motd = motd;
		this.numPlayers = numPlayers;
		this.maxPlayers = maxPlayers;
	}

	/**
	 * Get the address the ping is coming from.
	 *
	 * @return the address
	 */
	public InetAddress getAddress()
	{
		return address;
	}

	/**
	 * Get the maximum number of players sent.
	 *
	 * @return the maximum number of players
	 */
	public int getMaxPlayers()
	{
		return maxPlayers;
	}

	/**
	 * Get the message of the day message.
	 *
	 * @return the message of the day
	 */
	public String getMotd()
	{
		return motd;
	}

	/**
	 * Get the number of players sent.
	 *
	 * @return the number of players
	 */
	public int getNumPlayers()
	{
		return numPlayers;
	}

	/**
	 * Set the maximum number of players sent.
	 *
	 * @param maxPlayers
	 *            the maximum number of player
	 */
	public void setMaxPlayers( int maxPlayers )
	{
		this.maxPlayers = maxPlayers;
	}

	/**
	 * Change the message of the day message.
	 *
	 * @param motd
	 *            the message of the day
	 */
	public void setMotd( String motd )
	{
		this.motd = motd;
	}
}
