/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright 2015 Chiori-chan. All Right Reserved.
 * @author Chiori Greene
 * @email chiorigreene@gmail.com
 */
package com.chiorichan.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpInitializer extends ChannelInitializer<SocketChannel>
{
	@Override
	protected void initChannel( SocketChannel ch ) throws Exception
	{
		ChannelPipeline p = ch.pipeline();
		
		p.addLast( "decoder", new HttpRequestDecoder() );
		// Uncomment the following line if you don't want to handle HttpChunks.
		// p.addLast("aggregator", new HttpObjectAggregator(1048576));
		p.addLast( "encoder", new HttpResponseEncoder() );
		p.addLast( "deflater", new HttpContentCompressor() );
		p.addLast( "handler", new HttpHandler() );
	}
}
