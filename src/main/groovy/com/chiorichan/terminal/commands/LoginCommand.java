/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2015 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.terminal.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.chiorichan.ConsoleColor;
import com.chiorichan.account.AccountAttachment;
import com.chiorichan.account.AccountManager;
import com.chiorichan.account.AccountType;
import com.chiorichan.account.auth.AccountAuthenticator;
import com.chiorichan.account.lang.AccountException;
import com.chiorichan.account.lang.AccountResult;
import com.chiorichan.terminal.CommandDispatch;
import com.chiorichan.terminal.Terminal;
import com.chiorichan.terminal.TerminalInterviewer;

/**
 * Used to login an account to the console
 */
class LoginCommand extends BuiltinCommand
{
	class LoginInterviewerPass implements TerminalInterviewer
	{
		private AccountAttachment sender;
		
		public LoginInterviewerPass( AccountAttachment sender )
		{
			this.sender = sender;
		}
		
		@Override
		public String getPrompt()
		{
			return "Password for " + sender.getVariable( "user" ) + ": ";
		}
		
		@Override
		public boolean handleInput( String input )
		{
			String user = sender.getVariable( "user" );
			String pass = input;
			
			try
			{
				if ( user != null && pass != null )
				{
					AccountResult result = sender.getPermissible().login( AccountAuthenticator.PASSWORD, user, pass );
					
					if ( result != AccountResult.LOGIN_SUCCESS )
						if ( result == AccountResult.INTERNAL_ERROR )
						{
							result.getThrowable().printStackTrace();
							throw new AccountException( result );
						}
						else
							throw new AccountException( result );
					
					// if ( !handler.getPersistence().checkPermission( "sys.query" ).isTrue() )
					// throw new LoginException( LoginExceptionReason.notAuthorized, acct );
					
					AccountManager.getLogger().info( ConsoleColor.GREEN + "Successful Console Login [username='" + user + "',password='" + pass + "',userId='" + result.getAccount().getId() + "',displayName='" + result.getAccount().getDisplayName() + "']" );
					
					sender.sendMessage( ConsoleColor.GREEN + "Welcome " + user + ", you have been successfully logged in." );
				}
			}
			catch ( AccountException l )
			{
				if ( l.getAccount() != null )
					AccountManager.getLogger().warning( ConsoleColor.GREEN + "Failed Console Login [username='" + user + "',password='" + pass + "',userId='" + l.getAccount().getId() + "',displayName='" + l.getAccount().getDisplayName() + "',reason='" + l.getMessage() + "']" );
				
				sender.sendMessage( ConsoleColor.YELLOW + l.getMessage() );
				
				if ( !AccountType.isNoneAccount( sender ) )
					sender.getPermissible().login( AccountAuthenticator.NULL, AccountType.ACCOUNT_NONE.getId() );
				
				return true;
			}
			
			sender.setVariable( "user", null );
			return true;
		}
	}
	
	class LoginInterviewerUser implements TerminalInterviewer
	{
		private AccountAttachment sender;
		
		public LoginInterviewerUser( AccountAttachment sender )
		{
			this.sender = sender;
		}
		
		@Override
		public String getPrompt()
		{
			try
			{
				return InetAddress.getLocalHost().getHostName() + " login: ";
			}
			catch ( UnknownHostException e )
			{
				return "login: ";
			}
		}
		
		@Override
		public boolean handleInput( String input )
		{
			if ( input == null || input.isEmpty() )
			{
				sender.sendMessage( "Username can't be empty!" );
				return true;
			}
			
			sender.setVariable( "user", input );
			return true;
		}
	}
	
	public LoginCommand()
	{
		super( "login" );
	}
	
	@Override
	public boolean execute( AccountAttachment sender, String command, String[] args )
	{
		if ( sender instanceof Terminal )
		{
			CommandDispatch.addInterviewer( ( Terminal ) sender, new LoginInterviewerUser( sender ) );
			CommandDispatch.addInterviewer( ( Terminal ) sender, new LoginInterviewerPass( sender ) );
		}
		
		return true;
	}
}