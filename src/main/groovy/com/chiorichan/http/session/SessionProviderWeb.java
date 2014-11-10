package com.chiorichan.http.session;

import groovy.lang.Binding;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.chiorichan.ChatColor;
import com.chiorichan.Loader;
import com.chiorichan.account.bases.Account;
import com.chiorichan.account.helpers.LoginException;
import com.chiorichan.factory.CodeEvalFactory;
import com.chiorichan.framework.ConfigurationManagerWrapper;
import com.chiorichan.framework.Site;
import com.chiorichan.http.Candy;
import com.chiorichan.http.HttpCode;
import com.chiorichan.http.HttpRequest;
import com.chiorichan.http.HttpResponse;

public class SessionProviderWeb implements SessionProvider
{
	protected final Binding binding = new Binding();
	protected CodeEvalFactory factory = null;
	protected HttpRequest request;
	protected Session parentSession;
	
	protected SessionProviderWeb(HttpRequest _request)
	{
		parentSession = SessionManager.createSession();
		parentSession.sessionProviders.add( this );
		setRequest( _request, false );
	}
	
	protected SessionProviderWeb(Session session, HttpRequest _request)
	{
		parentSession = session;
		parentSession.sessionProviders.add( this );
		for ( Entry<String, Object> e : session.bindingMap.entrySet() )
			binding.setVariable( e.getKey(), e.getValue() );
		
		setRequest( _request, true );
	}
	
	@Override
	public Session getParentSession()
	{
		return parentSession;
	}
	
	protected void setRequest( HttpRequest _request, Boolean _stale )
	{
		request = _request;
		parentSession.stale = _stale;
		
		parentSession.setSite( request.getSite() );
		parentSession.ipAddr = request.getRemoteAddr();
		
		Map<String, Candy> pulledCandies = SessionUtils.poleCandies( _request );
		pulledCandies.putAll( parentSession.candies );
		parentSession.candies = pulledCandies;
		
		if ( request.getSite().getYaml() != null )
		{
			String _candyName = request.getSite().getYaml().getString( "sessions.cookie-name", parentSession.candyName );
			
			if ( !_candyName.equals( parentSession.candyName ) )
				if ( parentSession.candies.containsKey( parentSession.candyName ) )
				{
					parentSession.candies.put( _candyName, parentSession.candies.get( parentSession.candyName ) );
					parentSession.candies.remove( parentSession.candyName );
					parentSession.candyName = _candyName;
				}
				else
				{
					parentSession.candyName = _candyName;
				}
		}
		
		parentSession.sessionCandy = parentSession.candies.get( parentSession.candyName );
		
		try
		{
			parentSession.initSession( request.getParentDomain() );
		}
		catch ( SessionException e )
		{
			e.printStackTrace();
		}
		
		if ( request != null )
		{
			binding.setVariable( "request", request );
			binding.setVariable( "response", request.getResponse() );
		}
		binding.setVariable( "__FILE__", new File( "" ) );
	}
	
	@Override
	public void handleUserProtocols()
	{
		if ( !parentSession.pendingMessages.isEmpty() )
		{
			// request.getResponse().sendMessage( pendingMessages.toArray( new String[0] ) );
		}
		
		if ( request == null )
		{
			Loader.getLogger().warning( "PersistentSession: Request was misteriously empty for an unknown reason." );
			return;
		}
		
		String username = request.getArgument( "user" );
		String password = request.getArgument( "pass" );
		String remember = request.getArgumentBoolean( "remember" ) ? "true" : "false";
		String target = request.getArgument( "target" );
		
		if ( request.getArgument( "logout", "", true ) != null )
		{
			parentSession.logoutAccount();
			
			if ( target.isEmpty() )
				target = request.getSite().getYaml().getString( "scripts.login-form", "/login" );
			
			request.getResponse().sendRedirect( target + "?ok=You have been successfully logged out." );
			return;
		}
		
		if ( !username.isEmpty() && !password.isEmpty() )
		{
			try
			{
				Account user = Loader.getAccountsManager().attemptLogin( parentSession, username, password );
				
				parentSession.currentAccount = user;
				
				String loginPost = ( target.isEmpty() ) ? request.getSite().getYaml().getString( "scripts.login-post", "/panel" ) : target;
				
				parentSession.setVariable( "remember", remember );
				
				Loader.getLogger().info( ChatColor.GREEN + "Login Success `Username \"" + username + "\", Password \"" + password + "\", UserId \"" + user.getAccountId() + "\", Display Name \"" + user.getDisplayName() + "\"`" );
				request.getResponse().sendRedirect( loginPost );
				
			}
			catch ( LoginException l )
			{
				// l.printStackTrace();
				
				String loginForm = request.getSite().getYaml().getString( "scripts.login-form", "/login" );
				
				if ( l.getAccount() != null )
					Loader.getLogger().warning( "Login Failed `Username \"" + username + "\", Password \"" + password + "\", UserId \"" + l.getAccount().getAccountId() + "\", Display Name \"" + l.getAccount().getDisplayName() + "\", Reason \"" + l.getMessage() + "\"`" );
				
				request.getResponse().sendRedirect( loginForm + "?ok=" + l.getMessage() + "&target=" + target );
			}
		}
		else if ( parentSession.currentAccount == null )
		{
			username = parentSession.getVariable( "user" );
			password = parentSession.getVariable( "pass" );
			
			if ( username != null && !username.isEmpty() && password != null && !password.isEmpty() )
			{
				try
				{
					Account user = Loader.getAccountsManager().attemptLogin( parentSession, username, password );
					
					parentSession.currentAccount = user;
					
					Loader.getLogger().info( ChatColor.GREEN + "Login Success `Username \"" + username + "\", Password \"" + password + "\", UserId \"" + user.getAccountId() + "\", Display Name \"" + user.getDisplayName() + "\"`" );
				}
				catch ( LoginException l )
				{
					Loader.getLogger().warning( ChatColor.GREEN + "Login Failed `No Valid Login Present`" );
				}
			}
			else
				parentSession.currentAccount = null;
		}
		else
		{
			try
			{
				parentSession.currentAccount.reloadAndValidate(); // <- Is this being overly redundant?
				Loader.getLogger().info( ChatColor.GREEN + "Current Login `Username \"" + parentSession.currentAccount.getName() + "\", Password \"" + parentSession.currentAccount.getMetaData().getPassword() + "\", UserId \"" + parentSession.currentAccount.getAccountId() + "\", Display Name \"" + parentSession.currentAccount.getDisplayName() + "\"`" );
			}
			catch ( LoginException e )
			{
				parentSession.currentAccount = null;
				Loader.getLogger().warning( ChatColor.GREEN + "Login Failed `There was a login present but it failed validation with error: " + e.getMessage() + "`" );
			}
		}
		
		if ( parentSession.currentAccount != null )
			parentSession.currentAccount.putHandler( parentSession );
		
		if ( !parentSession.stale || Loader.getConfig().getBoolean( "sessions.rearmTimeoutWithEachRequest" ) )
			parentSession.rearmTimeout();
	}
	
	@Override
	public CodeEvalFactory getCodeFactory()
	{
		if ( factory == null )
			factory = CodeEvalFactory.create( binding );
		
		return factory;
	}
	
	@Override
	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}
	
	@Override
	public Object getGlobal( String key )
	{
		return binding.getVariable( key );
	}
	
	public void setVariable( String key, String value )
	{
		parentSession.setVariable( key, value );
	}
	
	public String getVariable( String key )
	{
		return parentSession.getVariable( key );
	}
	
	protected Binding getBinding()
	{
		return binding;
	}
	
	@Override
	public HttpRequest getRequest()
	{
		return request;
	}
	
	@Override
	public HttpResponse getResponse()
	{
		return request.getResponse();
	}
	
	public void requireLogin() throws IOException
	{
		requireLogin( null );
	}
	
	/**
	 * First checks in an account is present, sends to login page if not.
	 * Second checks if the present accounts has the specified permission.
	 * 
	 * @param permission
	 * @throws IOException
	 */
	public void requireLogin( String permission ) throws IOException
	{
		if ( parentSession.currentAccount == null )
			request.getResponse().sendLoginPage();
		
		if ( permission != null )
			if ( !parentSession.currentAccount.hasPermission( permission ) )
				request.getResponse().sendError( HttpCode.HTTP_FORBIDDEN, "You must have the `" + permission + "` in order to view this page!" );
	}
	
	public ConfigurationManagerWrapper getConfigurationManager()
	{
		return new ConfigurationManagerWrapper( this );
	}
	
	@Override
	public void onFinished()
	{
		request = null;
		
		Map<String, Object> bindingMap = parentSession.bindingMap;
		
		@SuppressWarnings( "unchecked" )
		Map<String, Object> variables = binding.getVariables();
		
		if ( bindingMap != null && variables != null )
			for ( Entry<String, Object> e : variables.entrySet() )
				if ( !e.getKey().equals( "__FILE__" ) && !e.getKey().equals( "_REQUEST" ) && !e.getKey().equals( "_REWRITE" ) && !e.getKey().equals( "_GET" ) && !e.getKey().equals( "_POST" ) && !e.getKey().equals( "_SERVER" ) && !e.getKey().equals( "_FILES" ) )
					bindingMap.put( e.getKey(), e.getValue() );
		
		parentSession.sessionProviders.remove( this );
	}
	
	@Override
	public Account getAccount()
	{
		return parentSession.getAccount();
	}
	
	@Override
	public String toString()
	{
		return parentSession.toString();
	}
	
	@Override
	public Candy getCandy( String key )
	{
		return parentSession.getCandy( key );
	}
	
	@Override
	public boolean isStale()
	{
		return parentSession.isStale();
	}
	
	@Override
	public String getId()
	{
		return parentSession.getId();
	}
	
	@Override
	public boolean isSet( String key )
	{
		return parentSession.isSet( key );
	}
	
	@Override
	public void setCookieExpiry( int valid )
	{
		parentSession.setCookieExpiry( valid );
	}
	
	@Override
	public void destroy() throws SessionException
	{
		parentSession.destroy();
	}
	
	@Override
	public long getTimeout()
	{
		return parentSession.getTimeout();
	}
	
	@Override
	public void infiniTimeout()
	{
		parentSession.infiniTimeout();
	}
	
	@Override
	public boolean getUserState()
	{
		return parentSession.getUserState();
	}
	
	@Override
	public void logoutAccount()
	{
		parentSession.logoutAccount();
	}
	
	@Override
	public Site getSite()
	{
		return parentSession.getSite();
	}
	
	@Override
	public void saveSession( boolean force )
	{
		parentSession.saveSession( force );
	}
	
	// TODO: Future add of setDomain, setCookieName, setSecure (http verses https)
}