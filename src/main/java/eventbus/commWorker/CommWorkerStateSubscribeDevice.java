package eventbus.commWorker;

import eventbus.SchedulerInterface;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class CommWorkerStateSubscribeDevice {
    int port = 9000;
    SchedulerInterface scheduler;

    public CommWorkerStateSubscribeDevice(SchedulerInterface scheduler) {
        this.scheduler = scheduler;
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    // 增加配置：允许重复绑定端口（开发环境友好）
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            // 基于换行符解码，最大帧长度64K
                            p.addLast(new LineBasedFrameDecoder(64 * 1024));
                            p.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            p.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            p.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    System.out.println("Received data: " + msg);
                                    // 可选：给客户端回送响应
                                    ctx.writeAndFlush("已收到注册信息：" + msg + "\n");
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.out.println("客户端已连接: " + ctx.channel().remoteAddress());
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    System.out.println("客户端异常断开: " + cause.getMessage());
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

    // 测试用：添加main方法方便运行
    public static void main(String[] args) {
        new CommWorkerStateSubscribeDevice(null);
    }
}