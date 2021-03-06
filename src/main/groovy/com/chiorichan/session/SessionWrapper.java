/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.session;

import com.chiorichan.AppConfig;
import com.chiorichan.account.AccountAttachment;
import com.chiorichan.account.AccountInstance;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.factory.BindingProvider;
import com.chiorichan.factory.ScriptBinding;
import com.chiorichan.factory.ScriptingFactory;
import com.chiorichan.http.HttpCookie;
import com.chiorichan.messaging.MessageSender;
import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.site.Site;
import com.chiorichan.site.SiteManager;
import com.chiorichan.utils.UtilStrings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Acts as a bridge between a Session and the User
 * TODO If Session is nullified, we need to start a new one
 */
public abstract class SessionWrapper implements BindingProvider, AccountAttachment
{
	/**
	 * The binding specific to this request
	 */
	private ScriptBinding binding = new ScriptBinding();

	/**
	 * The EvalFactory used to process scripts of this request
	 */
	private ScriptingFactory factory;

	/**
	 * The session associated with this request
	 */
	private Session session;

	/**
	 * Used to nullify a SessionWrapper and prepare it for collection by the GC
	 * something that should happen naturally but the simpler the better.
	 * <p>
	 * Sidenote: This is only for cleaning up a Session Wrapper, cleaning up an actual parent session is a whole different story.
	 */
	public void finish()
	{
		if ( session != null )
		{
			Map<String, Object> bindings = session.globals;
			Map<String, Object> variables = binding.getVariables();
			List<String> disallow = Arrays.asList( "out", "request", "response", "context" );

			/**
			 * We transfer any global variables back into our parent session like so.
			 * We also check to make sure keys like [out, _request, _response, _FILES, _REQUEST, etc...] are excluded.
			 */
			if ( bindings != null && variables != null )
				for ( Entry<String, Object> e : variables.entrySet() )
					if ( !disallow.contains( e.getKey() ) && !( e.getKey().startsWith( "_" ) && UtilStrings.isUppercase( e.getKey() ) ) )
						bindings.put( e.getKey(), e.getValue() );

			/**
			 * Session Wrappers use a WeakReference but by doing this we are making sure we are GC'ed sooner rather than later
			 */
			session.removeWrapper( this );
		}

		/**
		 * Clearing references to these classes, again for easier GC cleanup.
		 */
		session = null;
		factory = null;
		binding = null;

		/**
		 * Active connections should be closed here
		 */
		finish0();
	}

	protected abstract void finish0();

	@Override
	public ScriptBinding getBinding()
	{
		return binding;
	}

	public abstract HttpCookie getCookie( String key );

	public abstract Set<HttpCookie> getCookies();

	@Override
	public String getDisplayName()
	{
		return getSession().getDisplayName();
	}

	@Override
	public PermissibleEntity getPermissibleEntity()
	{
		return getSession().getPermissibleEntity();
	}

	@Override
	public ScriptingFactory getScriptingFactory()
	{
		return factory;
	}

	public Object getGlobal( String key )
	{
		return binding.getVariable( key );
	}

	@Override
	public String getId()
	{
		return getSession().getId();
	}

	@Override
	public abstract Site getLocation();

	@Override
	public final AccountPermissible getPermissible()
	{
		return session;
	}

	public final HttpCookie getServerCookie( String key, String altDefault )
	{
		HttpCookie cookie = getServerCookie( key );
		return cookie == null ? getServerCookie( altDefault ) : cookie;
	}

	protected abstract HttpCookie getServerCookie( String key );

	/**
	 * Gets the Session
	 *
	 * @return The session
	 */
	public final Session getSession()
	{
		if ( session == null )
			throw new IllegalStateException( "Detected an attempt to get session before startSession() was called" );
		return session;
	}

	@Override
	public String getVariable( String key )
	{
		return getSession().getVariable( key );
	}

	@Override
	public String getVariable( String key, String def )
	{
		return getSession().getVariable( key, def );
	}

	public final boolean hasSession()
	{
		return session != null;
	}

	@Override
	public AccountInstance instance()
	{
		return session.instance();
	}

	@Override
	public boolean isInitialized()
	{
		return session.isInitialized();
	}

	@Override
	public AccountMeta meta()
	{
		return session.meta();
	}

	@Override
	public void sendMessage( MessageSender sender, Object... objs )
	{
		// Do Nothing
	}

	@Override
	public void sendMessage( Object... objs )
	{
		// Do Nothing
	}

	protected abstract void sessionStarted();

	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	@Override
	public void setVariable( String key, String value )
	{
		getSession().setVariable( key, value );
	}

	/**
	 * Starts the session
	 *
	 * @throws SessionException
	 */
	public Session startSession() throws SessionException
	{
		session = SessionManager.instance().startSession( this );
		/*
		 * Create our Binding
		 */
		binding = new ScriptBinding( new HashMap<String, Object>( session.getGlobals() ) );

		/*
		 * Create our EvalFactory
		 */
		factory = ScriptingFactory.create( this );

		/*
		 * Reference Session Variables
		 */
		binding.setVariable( "_SESSION", session.data.data );

		Site site = getLocation();

		if ( site == null )
			site = SiteManager.instance().getDefaultSite();

		session.setSite( site );

		for ( HttpCookie cookie : getCookies() )
			session.putSessionCookie( cookie.getKey(), cookie );

		// Reference Context
		binding.setVariable( "context", this );

		// Reset __FILE__ Variable
		binding.setVariable( "__FILE__", site.directoryPublic() );

		if ( AppConfig.get().getBoolean( "sessions.rearmTimeoutWithEachRequest" ) )
			session.rearmTimeout();

		sessionStarted();

		return session;
	}

	// TODO: Future add of setDomain, setCookieName, setSecure (http verses https)
}
