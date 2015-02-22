/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * 
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import com.chiorichan.ConsoleLogger;
import com.chiorichan.Loader;
import com.chiorichan.account.Account;
import com.chiorichan.configuration.file.YamlConfiguration;
import com.chiorichan.permission.backend.FileBackend;
import com.chiorichan.permission.backend.MemoryBackend;
import com.chiorichan.permission.backend.SQLBackend;
import com.chiorichan.permission.event.PermissibleEntityEvent;
import com.chiorichan.permission.event.PermissibleEvent;
import com.chiorichan.permission.event.PermissibleSystemEvent;
import com.chiorichan.permission.structure.Permission;
import com.chiorichan.scheduler.TaskCreator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PermissionManager implements TaskCreator
{
	protected Map<String, PermissibleGroup> defaultGroups = new HashMap<String, PermissibleGroup>();
	protected Map<String, PermissibleGroup> groups = new HashMap<String, PermissibleGroup>();
	protected Map<String, PermissibleEntity> entities = Maps.newHashMap();
	protected Set<Permission> roots = Sets.newConcurrentHashSet();
	protected PermissionMatcher matcher = new RegExpMatcher();
	protected PermissionBackend backend = null;
	protected YamlConfiguration config;
	
	protected static boolean debugMode = false;
	protected static boolean allowOps = true;
	
	public void init() throws PermissionBackendException
	{
		config = Loader.getConfig();
		debugMode = config.getBoolean( "permissions.debug", debugMode );
		allowOps = config.getBoolean( "permissions.allowOps", allowOps );
		
		initBackend();
	}
	
	private void initBackend() throws PermissionBackendException
	{
		PermissionBackend.registerBackendAlias( "sql", SQLBackend.class );
		PermissionBackend.registerBackendAlias( "file", FileBackend.class );
		PermissionBackend.registerBackendAlias( "memory", MemoryBackend.class );
		
		String backendName = config.getString( "permissions.backend" );
		
		if ( backendName == null || backendName.isEmpty() )
		{
			backendName = PermissionBackend.defaultBackend; // Default backend
			this.config.set( "permissions.backend", backendName );
		}
		
		setBackend( backendName );
	}
	
	/**
	 * Set backend to specified backend.
	 * This would also cause backend resetting.
	 * 
	 * @param backendName
	 *            name of backend to set to
	 */
	public void setBackend( String backendName ) throws PermissionBackendException
	{
		synchronized ( this )
		{
			clearCache();
			backend = PermissionBackend.getBackend( backendName );
			backend.initialize();
			
			loadData();
		}
		
		callEvent( PermissibleSystemEvent.Action.BACKEND_CHANGED );
	}
	
	/**
	 * Return entity's object
	 * 
	 * @param entityname
	 *            get PermissibleEntity with given name
	 * @return PermissibleEntity instance
	 */
	public PermissibleEntity getEntity( Permissible permissible )
	{
		if ( permissible == null )
			throw new IllegalArgumentException( "Null entity passed! Name must not be empty" );
		
		if ( permissible.entity == null )
		{
			if ( entities.containsKey( permissible.getEntityId() ) )
				permissible.entity = entities.get( permissible.getEntityId() );
			else
			{
				PermissibleEntity entity = backend.getEntity( permissible.getEntityId() );
				entities.put( permissible.getEntityId(), entity );
				permissible.entity = entity;
			}
		}
		
		return permissible.entity;
	}
	
	public PermissibleEntity getEntity( String permissible )
	{
		if ( permissible == null )
			throw new IllegalArgumentException( "Null entity passed! Name must not be empty" );
		
		if ( entities.containsKey( permissible ) )
			return entities.get( permissible );
		else
		{
			PermissibleEntity entity = backend.getEntity( permissible );
			entities.put( permissible, entity );
			return entity;
		}
	}
	
	/**
	 * Return all registered entity objects
	 * 
	 * @return PermissibleEntity array
	 */
	public PermissibleEntity[] getEntities()
	{
		return entities.values().toArray( new PermissibleEntity[0] );
	}
	
	/**
	 * Reset in-memory object of specified entity
	 * 
	 * @param entityName
	 *            entity's name
	 */
	public void resetEntity( Permissible entity )
	{
		entities.remove( entity.getEntityId() );
	}
	
	/**
	 * Forcefully saves groups and entities to the backend data source.
	 */
	public void saveData()
	{
		
	}
	
	/**
	 * Loads all groups and entities from the backend data source.
	 */
	public void loadData()
	{
		groups.clear();
		entities.clear();
		
		for ( PermissibleGroup group : backend.getGroups() )
			groups.put( group.getId(), group );
		for ( PermissibleEntity entity : backend.getEntities() )
			entities.put( entity.getId(), entity );
		backend.loadPermissionTree();
		
		if ( isDebug() )
		{
			getLogger().info( "DEBUGGING LOADED PERMISSIONS!! (Permission Debug is On!)" );
			for ( Permission root : Permission.getRootNodes() )
				root.debugPermissionStack( 0 );
		}
	}
	
	/**
	 * Check if specified entity has specified permission
	 * 
	 * @param perm
	 *            entity object
	 * @param permission
	 *            permission string to check against
	 * @return true on success false otherwise
	 */
	public boolean has( Permissible perm, String permission )
	{
		return has( perm.getEntityId(), permission, "" ); // perm.getRef()
	}
	
	/**
	 * Check if entity has specified permission in ref
	 * 
	 * @param entity
	 *            entity object
	 * @param permission
	 *            permission as string to check against
	 * @param ref
	 *            ref used for this perm
	 * @return true on success false otherwise
	 */
	public boolean has( Account<?> entity, String permission, String ref )
	{
		return this.has( entity.getAcctId(), permission, ref );
	}
	
	/**
	 * Check if entity with name has permission in ref
	 * 
	 * @param entityName
	 *            entity name
	 * @param permission
	 *            permission as string to check against
	 * @param ref
	 *            ref's name as string
	 * @return true on success false otherwise
	 */
	public boolean has( String entityName, String permission, String ref )
	{
		PermissibleEntity entity = getEntity( entityName );
		
		if ( entity == null )
		{
			return false;
		}
		
		return entity.has( permission, ref );
	}
	
	/**
	 * Return object for specified group
	 * 
	 * @param groupname
	 *            group's name
	 * @return PermissibleGroup object
	 */
	public PermissibleGroup getGroup( String groupname )
	{
		if ( groupname == null || groupname.isEmpty() )
		{
			return null;
		}
		
		PermissibleGroup group = groups.get( groupname.toLowerCase() );
		
		if ( group == null )
		{
			group = this.backend.getGroup( groupname );
			if ( group != null )
			{
				this.groups.put( groupname.toLowerCase(), group );
			}
			else
			{
				throw new IllegalStateException( "Group " + groupname + " is null" );
			}
		}
		
		return group;
	}
	
	/**
	 * Return all groups
	 * 
	 * @return PermissibleGroup array
	 */
	public PermissibleGroup[] getGroups()
	{
		return backend.getGroups();
	}
	
	/**
	 * Return default group object
	 * 
	 * @return default group object. null if not specified
	 */
	public PermissibleGroup getDefaultGroup( String refName )
	{
		String refIndex = refName != null ? refName : "";
		
		if ( !this.defaultGroups.containsKey( refIndex ) )
		{
			this.defaultGroups.put( refIndex, this.getDefaultGroup( refName, this.getDefaultGroup( null, null ) ) );
		}
		
		return this.defaultGroups.get( refIndex );
	}
	
	public PermissibleGroup getDefaultGroup()
	{
		return this.getDefaultGroup( null );
	}
	
	private PermissibleGroup getDefaultGroup( String refName, PermissibleGroup fallback )
	{
		PermissibleGroup defaultGroup = this.backend.getDefaultGroup( refName );
		
		if ( defaultGroup == null && refName == null )
		{
			getLogger().warning( "No default group defined. Use \"perm set default group <group> [ref]\" to define default group." );
			return fallback;
		}
		
		if ( defaultGroup != null )
		{
			return defaultGroup;
		}
		
		return fallback;
	}
	
	/**
	 * Set default group to specified group
	 * 
	 * @param group
	 *            PermissibleGroup group object
	 */
	public void setDefaultGroup( PermissibleGroup group, String refName )
	{
		if ( group == null || group.equals( this.defaultGroups ) )
		{
			return;
		}
		
		backend.setDefaultGroup( group.getId(), refName );
		
		this.defaultGroups.clear();
		
		callEvent( PermissibleSystemEvent.Action.DEFAULTGROUP_CHANGED );
		callEvent( new PermissibleEntityEvent( group, PermissibleEntityEvent.Action.DEFAULTGROUP_CHANGED ) );
	}
	
	public void setDefaultGroup( PermissibleGroup group )
	{
		this.setDefaultGroup( group, null );
	}
	
	/**
	 * Reset in-memory object for groupName
	 * 
	 * @param groupName
	 *            group's name
	 */
	public void resetGroup( String groupName )
	{
		this.groups.remove( groupName );
	}
	
	/**
	 * Set debug mode
	 * 
	 * @param debug
	 *            true enables debug mode, false disables
	 */
	public static void setDebug( boolean debug )
	{
		debugMode = debug;
		callEvent( PermissibleSystemEvent.Action.DEBUGMODE_TOGGLE );
	}
	
	/**
	 * Return current state of debug mode
	 * 
	 * @return true debug is enabled, false if disabled
	 */
	public boolean isDebug()
	{
		return debugMode;
	}
	
	/**
	 * Return current backend
	 * 
	 * @return current backend object
	 */
	public PermissionBackend getBackend()
	{
		return this.backend;
	}
	
	/**
	 * Register new timer task
	 * 
	 * @param task
	 *            TimerTask object
	 * @param delay
	 *            delay in seconds
	 */
	protected void registerTask( TimerTask task, int delay )
	{
		Loader.getScheduler().scheduleAsyncDelayedTask( this, task, delay * 50 );
	}
	
	/**
	 * Reset all in-memory groups and entities, clean up runtime stuff, reloads backend
	 */
	public void reset() throws PermissionBackendException
	{
		this.clearCache();
		
		if ( this.backend != null )
		{
			this.backend.reload();
		}
		
		callEvent( PermissibleSystemEvent.Action.RELOADED );
	}
	
	public void end()
	{
		try
		{
			reset();
		}
		catch ( PermissionBackendException ignore )
		{
			// Ignore because we're shutting down so who cares
		}
	}
	
	protected void clearCache()
	{
		this.entities.clear();
		this.groups.clear();
		this.defaultGroups.clear();
	}
	
	protected static void callEvent( PermissibleEvent event )
	{
		Loader.getEventBus().callEvent( event );
	}
	
	protected static void callEvent( PermissibleSystemEvent.Action action )
	{
		callEvent( new PermissibleSystemEvent( action ) );
	}
	
	public PermissionMatcher getPermissionMatcher()
	{
		return matcher;
	}
	
	public void setPermissionMatcher( PermissionMatcher matcher )
	{
		this.matcher = matcher;
	}
	
	public static ConsoleLogger getLogger()
	{
		return Loader.getLogger( "PermMgr" );
	}
	
	@Override
	public boolean isEnabled()
	{
		return true;
	}
	
	@Override
	public String getName()
	{
		return "PermissionsManager";
	}
}
