/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.db.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;

import java.util.List;

/**
 * Database packet codec.
 */
@RequiredArgsConstructor
@Slf4j
public final class PacketCodec extends ByteToMessageCodec<DatabasePacket<?>> {
    
    @SuppressWarnings("rawtypes")
    private final DatabasePacketCodecEngine databasePacketCodecEngine;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        int readableBytes = in.readableBytes();
        if (!databasePacketCodecEngine.isValidHeader(readableBytes)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Read from client {} :\n{}", context.channel().id().asShortText(), ByteBufUtil.prettyHexDump(in));
        }
        databasePacketCodecEngine.decode(context, in, out);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void encode(final ChannelHandlerContext context, final DatabasePacket<?> message, final ByteBuf out) {
        databasePacketCodecEngine.encode(context, message, out);
        if (log.isDebugEnabled()) {
            log.debug("Write to client {} :\n{}", context.channel().id().asShortText(), ByteBufUtil.prettyHexDump(out));
        }
    }
}
