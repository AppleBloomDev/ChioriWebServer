package com.chiorichan.updater;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.chiorichan.ChatColor;
import com.chiorichan.updater.BuildArtifact.ChangeSet.ChangeSetDetails;

public class BuildArtifact
{
	public List<BuildAction> actions;
	public List<ResultingArtifact> artifacts;
	public boolean building;
	public String description;
	public int duration;
	public int estimatedDuration;
	public String fullDisplayName;
	public String id;
	public boolean keepLog;
	public int number;
	public String result;
	public long timestamp;
	public String url;
	public String builtOn;
	public ChangeSet changeSet;
	public List<Culprits> culprits;
	
	private Properties buildProperties;
	
	public class BuildAction
	{
		public List<Causes> causes;
		public Map<String, BuildsByBranchName> buildsByBranchName;
		public Revision lastBuiltRevision;
		public List<String> remoteUrls;
		public String scmName;
		
		public class Causes
		{
			String shortDescription;
		}
		
		public class BuildsByBranchName
		{
			public int buildNumber;
			public String buildResult;
			public Revision marked;
			public Revision revision;
		}
		
		public class Revision
		{
			public String SHA1;
			public List<Branch> branch;
			
			public class Branch
			{
				public String SHA1;
				public String name;
			}
		}
	}
	
	public class ResultingArtifact
	{
		public String displayPath;
		public String fileName;
		public String relativePath;
	}
	
	public class ChangeSet
	{
		public List<ChangeSetDetails> items;
		public String kind;
		
		public class ChangeSetDetails
		{
			public List<String> affectedPaths;
			public String commitId;
			public int timestamp;
			public Author author;
			public String comment;
			public String date;
			public String id;
			public String msg;
			public List<Paths> paths;
			
			public class Author
			{
				public String absoluteUrl;
				public String fullName;
			}
			
			public class Paths
			{
				public String editType;
				public String file;
			}
			
			public String toString()
			{
				StringBuilder sb = new StringBuilder();
				
				for ( Paths p : paths )
				{
					sb.append( "\n\t" + ChatColor.GREEN + "[" + p.editType + "] " + p.file );
				}
				
				return msg + " (Commit: " + id + ")" + sb.toString();
			}
		}
	}
	
	public class Culprits
	{
		public String absoluteUrl;
		public String fullName;
	}

	public boolean isBroken()
	{
		// TODO Parse results to determine if this build was reported as broken (or had bugs). Description parse maybe.
		return false;
	}

	public String getBrokenReason()
	{
		// TODO Find out why this build was reported broken? Again, Description maybe?
		return "";
	}

	public int getBuildNumber()
	{
		return number;
	}

	public String getCreated()
	{
		Date date = new Date( timestamp * 1000 );
		return new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z" ).format( date );
	}

	public String getVersion()
	{
		return getBuildProperties().getProperty( "project.version", "{Internal Error}" );
	}

	public String getFile()
	{
		String mainJar = null;
		
		for ( ResultingArtifact ra : artifacts )
		{
			if ( ra.fileName.startsWith( "ChioriWebServer" ) && ra.fileName.endsWith( ".jar" ) )
			{
				mainJar = url + "artifact/" + ra.relativePath;
				break;
			}
		}
		
		return mainJar;
	}
	
	public Properties getBuildProperties()
	{
		if ( buildProperties == null )
			buildProperties = AutoUpdater.getService().getBuildProperties( number + "", "build properties about this Chiori Web Server version; perhaps you are running a custom one?" );
		
		return buildProperties;
	}

	public String getHtmlUrl()
	{
		return url;
	}

	public List<ChangeSetDetails> getChanges()
	{
		return changeSet.items;
	}
}
