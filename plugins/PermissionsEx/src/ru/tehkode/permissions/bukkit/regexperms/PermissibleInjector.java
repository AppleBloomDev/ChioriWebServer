package ru.tehkode.permissions.bukkit.regexperms;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

import com.chiorichan.Loader;
import com.chiorichan.account.bases.Account;
import com.chiorichan.permissions.Permissible;
import com.chiorichan.permissions.PermissibleBase;

/**
 * This class handles injection of {@link Permissible}s into {@link Account}s for various server implementations.
 */
public abstract class PermissibleInjector
{
	protected final String clazzName, fieldName;
	protected final boolean copyValues;
	
	public PermissibleInjector(String clazzName, String fieldName, boolean copyValues)
	{
		this.clazzName = clazzName;
		this.fieldName = fieldName;
		this.copyValues = copyValues;
	}
	
	/**
	 * Attempts to inject {@code permissible} into {@code user},
	 * 
	 * @param user The user to have {@code permissible} injected into
	 * @param permissible The permissible to inject into {@code user}
	 * @return the old permissible if the injection was successful, otherwise null
	 * @throws NoSuchFieldException when the permissions field could not be found in the Permissible
	 * @throws IllegalAccessException when things go very wrong
	 */
	public Permissible inject( Account user, Permissible permissible ) throws NoSuchFieldException, IllegalAccessException
	{
		Field permField = getPermissibleField( user );
		if ( permField == null )
		{
			return null;
		}
		Permissible oldPerm = (Permissible) permField.get( user );
		if ( copyValues && permissible instanceof PermissibleBase )
		{
			PermissibleBase newBase = (PermissibleBase) permissible;
			PermissibleBase oldBase = (PermissibleBase) oldPerm;
			copyValues( oldBase, newBase );
		}
		
		// Inject permissible
		permField.set( user, permissible );
		return oldPerm;
	}
	
	public Permissible getPermissible( Account user ) throws NoSuchFieldException, IllegalAccessException
	{
		return (Permissible) getPermissibleField( user ).get( user );
	}
	
	private Field getPermissibleField( Account user ) throws NoSuchFieldException
	{
		Class<?> humanEntity;
		try
		{
			humanEntity = Class.forName( clazzName );
		}
		catch ( ClassNotFoundException e )
		{
			Logger.getLogger( "PermissionsEx" ).warning( "[PermissionsEx] Unknown server implementation being used!" );
			return null;
		}
		
		if ( !humanEntity.isAssignableFrom( user.getClass() ) )
		{
			Logger.getLogger( "PermissionsEx" ).warning( "[PermissionsEx] Strange error while injecting permissible!" );
			return null;
		}
		
		Field permField = humanEntity.getDeclaredField( fieldName );
		// Make it public for reflection
		permField.setAccessible( true );
		return permField;
	}
	
	private void copyValues( PermissibleBase old, PermissibleBase newPerm ) throws NoSuchFieldException, IllegalAccessException
	{
		// Attachments
		Field attachmentField = PermissibleBase.class.getDeclaredField( "attachments" );
		attachmentField.setAccessible( true );
		( (List) attachmentField.get( newPerm ) ).addAll( (List) attachmentField.get( old ) );
		newPerm.recalculatePermissions();
	}
	
	public abstract boolean isApplicable( Account user );
	
	public static class ServerNamePermissibleInjector extends PermissibleInjector
	{
		protected final String serverName;
		
		public ServerNamePermissibleInjector(String clazz, String field, boolean copyValues, String serverName)
		{
			super( clazz, field, copyValues );
			this.serverName = serverName;
		}
		
		@Override
		public boolean isApplicable( Account user )
		{
			return Loader.getInstance().getServerName().equalsIgnoreCase( serverName );
		}
	}
	
	public static class ClassPresencePermissibleInjector extends PermissibleInjector
	{
		
		public ClassPresencePermissibleInjector(String clazzName, String fieldName, boolean copyValues)
		{
			super( clazzName, fieldName, copyValues );
		}
		
		@Override
		public boolean isApplicable( Account user )
		{
			try
			{
				return Class.forName( clazzName ).isInstance( user );
			}
			catch ( ClassNotFoundException e )
			{
				return false;
			}
		}
	}
	
	public static class ClassNameRegexPermissibleInjector extends PermissibleInjector
	{
		private final String regex;
		
		public ClassNameRegexPermissibleInjector(String clazz, String field, boolean copyValues, String regex)
		{
			super( clazz, field, copyValues );
			this.regex = regex;
		}
		
		@Override
		public boolean isApplicable( Account user )
		{
			return user.getClass().getName().matches( regex );
		}
	}
}
