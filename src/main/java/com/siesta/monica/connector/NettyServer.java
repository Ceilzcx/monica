package com.siesta.monica.connector;

import com.siesta.monica.common.ConnectStatus;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServer {
    private static final Map<SocketAddress, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final Bootstrap bootstrap;
    private static final MySQLConnector sqlConnector = MySQLConnectFactory.getDefaultMySQLConnector("localhost", 3306);

    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new BusinessHandler());
                    }
                });
    }

    private NettyServer() {}

    public static Channel open(SocketAddress address) throws InterruptedException {
        if (CHANNEL_MAP.containsKey(address)) return CHANNEL_MAP.get(address);
        ChannelFuture future = bootstrap.connect(address).sync();
        if (future.isSuccess()) {
//            future.channel().pipeline().get(BusinessHandler.class).latch.await();
            Channel channel = future.channel();
            CHANNEL_MAP.put(address, channel);
            return channel;
        }
        return null;
    }

    public static class BusinessHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            if (sqlConnector.getStatus().equals(ConnectStatus.HAND_SHAKE)) {
                sqlConnector.readProtocol(msg);
            } else if (sqlConnector.getStatus().equals(ConnectStatus.AUTH_SWITCH)) {
                sqlConnector.readAuthSwitchRequest(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
        }
    }

}
