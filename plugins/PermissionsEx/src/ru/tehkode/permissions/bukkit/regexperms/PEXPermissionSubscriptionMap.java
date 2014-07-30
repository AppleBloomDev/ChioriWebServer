package ru.tehkode.permissions.bukkit.regexperms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.utils.FieldReplacer;

import com.chiorichan.Loader;
import com.chiorichan.account.bases.Account;
import com.chiorichan.permissions.Permissible;
import com.chiorichan.plugin.PluginManager;
import com.google.common.collect.Sets;

/**
 * PermissibleMap for the permissions subscriptions data in Loader's {@link PluginManager} so we can put in our own data too.
 */
public class PEXPermissionSubscriptionMap extends HashMap<String, Map<Permissible, Boolean>>
{
	private static FieldReplacer<PluginManager, Map> INJECTOR;
	private static final AtomicReference<PEXPermissionSubscriptionMap> INSTANCE = new AtomicReference<PEXPermissionSubscriptionMap>();
	private final PermissionsEx plugin;
	private final PluginManager manager;
	
	private PEXPermissionSubscriptionMap(PermissionsEx plugin, PluginManager manager, Map<String, Map<Permissible, Boolean>> backing)
	{
		super( backing );
		this.plugin = plugin;
		this.manager = manager;
	}
	
	/**
	 * Inject a PEX permission subscription map into the provided plugin manager.
	 * This allows some PEX functions to work with the plugin manager.
	 * 
	 * @param manager The manager to inject into
	 */
	@SuppressWarnings( "unchecked" )
	public static PEXPermissionSubscriptionMap inject( PermissionsEx plugin, PluginManager manager )
	{
		PEXPermissionSubscriptionMap map = INSTANCE.get();
		if ( map != null )
		{
			return map;
		}
		
		if ( INJECTOR == null )
		{
			INJECTOR = new FieldReplacer<PluginManager, Map>( manager.getClass(), "permSubs", Map.class );
		}
		
		Map backing = INJECTOR.get( manager );
		if ( backing instanceof PEXPermissionSubscriptionMap )
		{
			return (PEXPermissionSubscriptionMap) backing;
		}
		PEXPermissionSubscriptionMap wrappedMap = new PEXPermissionSubscriptionMap( plugin, manager, backing );
		if ( INSTANCE.compareAndSet( null, wrappedMap ) )
		{
			INJECTOR.set( manager, wrappedMap );
			return wrappedMap;
		}
		else
		{
			return INSTANCE.get();
		}
	}
	
	/**
	 * Uninject this PEX map from its plugin manager
	 */
	public void uninject()
	{
		if ( INSTANCE.compareAndSet( this, null ) )
		{
			Map<String, Map<Permissible, Boolean>> unwrappedMap = new HashMap<String, Map<Permissible, Boolean>>( this.size() );
			for ( Map.Entry<String, Map<Permissible, Boolean>> entry : this.entrySet() )
			{
				if ( entry.getValue() instanceof PEXSubscriptionValueMap )
				{
					unwrappedMap.put( entry.getKey(), ( (PEXSubscriptionValueMap) entry.getValue() ).backing );
				}
			}
			INJECTOR.set( manager, unwrappedMap );
		}
	}
	
	@Override
	public Map<Permissible, Boolean> get( Object key )
	{
		if ( key == null )
		{
			return null;
		}
		
		Map<Permissible, Boolean> result = super.get( key );
		if ( result == null )
		{
			result = new PEXSubscriptionValueMap( (String) key, new WeakHashMap<Permissible, Boolean>() );
			super.put( (String) key, result );
		}
		else if ( !( result instanceof PEXSubscriptionValueMap ) )
		{
			result = new PEXSubscriptionValueMap( (String) key, result );
			super.put( (String) key, result );
		}
		return result;
	}
	
	@Override
	public Map<Permissible, Boolean> put( String key, Map<Permissible, Boolean> value )
	{
		if ( !( value instanceof PEXSubscriptionValueMap ) )
		{
			value = new PEXSubscriptionValueMap( key, value );
		}
		return super.put( key, value );
	}
	
	public class PEXSubscriptionValueMap implements Map<Permissible, Boolean>
	{
		private final String permission;
		private final Map<Permissible, Boolean> backing;
		
		public PEXSubscriptionValueMap(String permission, Map<Permissible, Boolean> backing)
		{
			this.permission = permission;
			this.backing = backing;
		}
		
		@Override
		public int size()
		{
			return backing.size();
		}
		
		@Override
		public boolean isEmpty()
		{
			return backing.isEmpty();
		}
		
		@Override
		public boolean containsKey( Object key )
		{
			return backing.containsKey( key );
		}
		
		@Override
		public boolean containsValue( Object value )
		{
			return backing.containsValue( value );
		}
		
		@Override
		public Boolean put( Permissible key, Boolean value )
		{
			return backing.put( key, value );
		}
		
		@Override
		public Boolean remove( Object key )
		{
			return backing.remove( key );
		}
		
		@Override
		public void putAll( Map<? extends Permissible, ? extends Boolean> m )
		{
			backing.putAll( m );
		}
		
		@Override
		public void clear()
		{
			backing.clear();
		}
		
		@Override
		public Boolean get( Object key )
		{
			if ( key instanceof Permissible )
			{
				Permissible p = (Permissible) key;
				if ( p.isPermissionSet( permission ) )
				{
					return p.hasPermission( permission );
				}
			}
			return backing.get( key );
		}
		
		@Override
		public Set<Permissible> keySet()
		{
			List<Account> users = Loader.getAccountsManager().getOnlineAccounts();
			Set<Permissible> pexMatches = new HashSet<Permissible>( users.size() );
			for ( Account user : users )
			{
				if ( user.hasPermission( permission ) )
				{
					pexMatches.add( user );
				}
			}
			return Sets.union( pexMatches, backing.keySet() );
		}
		
		@Override
		public Collection<Boolean> values()
		{
			return backing.values();
		}
		
		@Override
		public Set<Entry<Permissible, Boolean>> entrySet()
		{
			return backing.entrySet();
		}
	}
}
