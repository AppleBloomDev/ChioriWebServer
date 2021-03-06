/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.updater;

import com.chiorichan.utils.UtilIO;
import com.chiorichan.utils.UtilHttp;
import com.chiorichan.lang.DownloadDeniedException;
import com.chiorichan.lang.DownloadException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Download implements Runnable
{
	private class MonitorThread extends Thread
	{
		private final ReadableByteChannel rbc;
		private final Thread downloadThread;
		private long last = System.currentTimeMillis();

		MonitorThread( Thread downloadThread, ReadableByteChannel rbc )
		{
			super( "Download Monitor Thread" );
			this.setDaemon( true );
			this.rbc = rbc;
			this.downloadThread = downloadThread;
		}

		@Override
		public void run()
		{
			while ( !this.isInterrupted() )
			{
				long diff = outFile.length() - downloaded;
				downloaded = outFile.length();
				if ( diff == 0 )
				{
					if ( System.currentTimeMillis() - last > TIMEOUT )
					{
						if ( listener != null )
							listener.stateChanged( "Download Failed", getProgress() );
						try
						{
							rbc.close();
							downloadThread.interrupt();
						}
						catch ( Exception ignore )
						{
							// We catch all exceptions here, because ReadableByteChannel is AWESOME
							// and was throwing NPE's sometimes when we tried to close it after
							// the connection broke.
						}
						return;
					}
				}
				else
					last = System.currentTimeMillis();

				stateChanged();
				try
				{
					sleep( 50 );
				}
				catch ( InterruptedException ignore )
				{
					return;
				}
			}
		}
	}

	public enum Result
	{
		SUCCESS, FAILURE, PERMISSION_DENIED,
	}

	private static class StreamThread extends Thread
	{
		private final URLConnection urlconnection;
		private final AtomicReference<InputStream> is;
		public final AtomicBoolean permDenied = new AtomicBoolean( false );

		StreamThread( URLConnection urlconnection, AtomicReference<InputStream> is )
		{
			this.urlconnection = urlconnection;
			this.is = is;
		}

		@Override
		public void run()
		{
			try
			{
				is.set( urlconnection.getInputStream() );
			}
			catch ( SocketException e )
			{
				if ( e.getMessage().equalsIgnoreCase( "Permission denied: connect" ) )
					permDenied.set( true );
			}
			catch ( IOException ignore )
			{
			}
		}
	}

	private static final long TIMEOUT = 30000;
	private final URL url;
	private long size = -1;
	private long downloaded = 0;
	private final String outPath;
	private final String name;
	private DownloadListener listener;

	private Result result = Result.FAILURE;

	private File outFile = null;

	private Exception exception = null;

	public Download( URL url, String name, String outPath ) throws MalformedURLException
	{
		this.url = url;
		this.outPath = outPath;
		this.name = name;
	}

	protected InputStream getConnectionInputStream( final URLConnection urlconnection ) throws DownloadException
	{
		final AtomicReference<InputStream> is = new AtomicReference<InputStream>();

		for ( int j = 0; j < 3 && is.get() == null; j++ )
		{
			StreamThread stream = new StreamThread( urlconnection, is );
			stream.start();
			int iterationCount = 0;
			while ( is.get() == null && iterationCount++ < 5 )
				try
				{
					stream.join( 1000L );
				}
				catch ( InterruptedException ignore )
				{
				}

			if ( stream.permDenied.get() )
				throw new DownloadDeniedException( "Permission denied!" );

			if ( is.get() != null )
				break;
			try
			{
				stream.interrupt();
				stream.join();
			}
			catch ( InterruptedException ignore )
			{
			}
		}

		if ( is.get() == null )
			throw new DownloadException( "Unable to download file from " + urlconnection.getURL() );
		return new BufferedInputStream( is.get() );
	}

	public Exception getException()
	{
		return exception;
	}

	public File getOutFile()
	{
		return outFile;
	}

	public float getProgress()
	{
		return ( float ) downloaded / size * 100;
	}

	public Result getResult()
	{
		return result;
	}

	@Override
	public void run()
	{
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		try
		{
			HttpURLConnection conn = UtilHttp.openHttpConnection( url );
			int response = conn.getResponseCode();
			int responseFamily = response / 100;

			if ( responseFamily == 3 )
				throw new DownloadException( "The server issued a redirect response which the Updater failed to follow." );
			else if ( responseFamily != 2 )
				throw new DownloadException( "The server issued a " + response + " response code." );

			InputStream in = getConnectionInputStream( conn );

			size = conn.getContentLength();
			outFile = new File( outPath );
			outFile.delete();

			rbc = Channels.newChannel( in );
			fos = new FileOutputStream( outFile );

			stateChanged();

			Thread progress = new MonitorThread( Thread.currentThread(), rbc );
			progress.start();

			fos.getChannel().transferFrom( rbc, 0, size > 0 ? size : Integer.MAX_VALUE );
			in.close();
			rbc.close();
			progress.interrupt();
			if ( size > 0 )
			{
				if ( size == outFile.length() )
					result = Result.SUCCESS;
			}
			else
				result = Result.SUCCESS;

			stateDone();
		}
		catch ( DownloadDeniedException e )
		{
			exception = e;
			result = Result.PERMISSION_DENIED;
		}
		catch ( DownloadException e )
		{
			exception = e;
			result = Result.FAILURE;
		}
		catch ( Exception e )
		{
			exception = e;
			e.printStackTrace();
		}
		finally
		{
			UtilIO.closeQuietly( fos );
			UtilIO.closeQuietly( rbc );
		}

		if ( exception != null )
			AutoUpdater.getLogger().severe( "Download Resulted in an Exception", exception );
	}

	public void setListener( DownloadListener listener )
	{
		this.listener = listener;
	}

	private void stateChanged()
	{
		if ( listener != null )
			listener.stateChanged( name, getProgress() );
	}

	private void stateDone()
	{
		if ( listener != null )
		{
			listener.stateChanged( "Download Done!", 100 );
			listener.stateDone();
		}
	}
}
