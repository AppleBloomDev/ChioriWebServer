package com.chiorichan.command.defaults;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.chiorichan.ChatColor;
import com.chiorichan.Loader;
import com.chiorichan.command.Command;
import com.chiorichan.command.CommandSender;
import com.chiorichan.util.StringUtil;
import com.google.common.collect.ImmutableList;

public class PardonIpCommand extends VanillaCommand
{
	public PardonIpCommand()
	{
		super( "pardon-ip" );
		this.description = "Allows the specified IP address to use this server";
		this.usageMessage = "/pardon-ip <address>";
		this.setPermission( "chiori.command.unban.ip" );
	}
	
	@Override
	public boolean execute( CommandSender sender, String currentAlias, String[] args )
	{
		if ( !testPermission( sender ) )
			return true;
		if ( args.length != 1 )
		{
			sender.sendMessage( ChatColor.RED + "Usage: " + usageMessage );
			return false;
		}
		
		if ( BanIpCommand.ipValidity.matcher( args[0] ).matches() )
		{
			Loader.getInstance().unbanIP( args[0] );
			Command.broadcastCommandMessage( sender, "Pardoned ip " + args[0] );
		}
		else
		{
			sender.sendMessage( "Invalid ip" );
		}
		
		return true;
	}
}