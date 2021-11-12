package com.siesta.monica.connector;

import com.siesta.monica.ConnectStatus;
import com.siesta.monica.util.ByteUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

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
            if (sqlConnector.getStatus().equals(ConnectStatus.CONNECT)) {
                sqlConnector.readProtocol(msg);
            } else if (sqlConnector.getStatus().equals(ConnectStatus.AUTH_PACKAGE)) {
                byte[] header = new byte[4];
                msg.readBytes(header);

                int len = (header[0] & 0xFF) | ((header[1] & 0xFF) << 8) | ((header[2] & 0xFF) << 16);
                byte[] body = new byte[len];
                msg.readBytes(body);
                // 根据第一个字节判断是否成功
                /**
                 * <pre>
                 *     type             byte[0]
                 *     OkPacket         0x00
                 *     ErrorPacket      0xFF
                 *     FieldPacket      0x01-0xFA
                 *     Row Data Packet  0x01-0xFA
                 *     EOF Packet       0xFE
                 * </pre>
                 */
                if (body[0] == -1) {
                    String errorMsg = new String(ByteUtil.splitBytes(body, 1, len - 1), StandardCharsets.UTF_8);
                    log.info("error msg: {}", errorMsg);
                } else {
                    ByteUtil.printBytes(body);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }

}
