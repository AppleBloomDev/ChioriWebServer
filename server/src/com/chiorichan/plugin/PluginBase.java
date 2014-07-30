package com.chiorichan.plugin;


/**
 * Represents a base {@link Plugin}
 * <p>
 * Extend this class if your plugin is not a {@link org.bukkit.plugin.java.JavaPlugin}
 */
public abstract class PluginBase implements Plugin
{
	@Override
	public final int hashCode()
	{
		return getName().hashCode();
	}
	
	@Override
	public final boolean equals( Object obj )
	{
		if ( this == obj )
		{
			return true;
		}
		if ( obj == null )
		{
			return false;
		}
		if ( !( obj instanceof Plugin ) )
		{
			return false;
		}
		try
		{
			return getName().equals( ( (Plugin) obj ).getName() );
		}
		catch ( NullPointerException e )
		{
			return false;
		}
	}
	
	public final String getName()
	{
		return getDescription().getName();
	}
}
