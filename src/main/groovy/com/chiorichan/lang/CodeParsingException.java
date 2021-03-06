/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.lang;

import com.chiorichan.utils.UtilStrings;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.control.CompilationFailedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeParsingException extends Exception
{
	private static final long serialVersionUID = 6622374555743321786L;

	protected int lineNumber = -1;
	protected int columnNumber = -1;
	protected String fileName = null;
	protected String sourceCode = "";
	protected String message = "";

	public CodeParsingException( GroovyRuntimeException e, String code )
	{
		super();

		message = UtilStrings.escapeHtml( e.getMessage() );

		Pattern p = Pattern.compile( "startup failed: (.*):.*line ([0-9]+), column ([0-9]+).*?" );
		Matcher m = p.matcher( e.getMessage() );

		if ( m.find() )
		{
			fileName = m.group( 1 );
			lineNumber = Integer.parseInt( m.group( 2 ) );
			columnNumber = Integer.parseInt( m.group( 3 ) );
		}
		else
		{
			p = Pattern.compile( "line ([0-9]+), column ([0-9]+)" );
			m = p.matcher( e.getMessage() );

			if ( m.find() )
			{
				lineNumber = Integer.parseInt( m.group( 1 ) );
				columnNumber = Integer.parseInt( m.group( 2 ) );
			}
		}

		// System.out.println( lineNumber + " -- " + columnNumber + " -- " + fileName );

		sourceCode = code;
	}

	public CodeParsingException( CompilationFailedException e )
	{
		this( e, "" );
	}

	public void setMessage( String msg )
	{
		message = msg;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

	public String getSourceCode()
	{
		return sourceCode;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public int getColumnNumber()
	{
		return columnNumber;
	}

	public String getSourceFile()
	{
		return fileName;
	}
}
