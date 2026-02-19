package statecentre.control;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

//状态模块

public class StateServer {
    int port = 9000;

    public StateServer() {
//        订阅调度器事件


        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new LineBasedFrameDecoder(64 * 1024));
                            p.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            p.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            p.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {

                                }
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

                                    ctx.close();
                                }
                            });
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            System.out.println("[StateServer] listening on " + port);
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
