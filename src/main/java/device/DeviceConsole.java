package device;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import statecentre.model.Action;
import statecentre.model.Device;

import java.nio.charset.StandardCharsets;

public class DeviceConsole {
    static int port = 9000;
    static Channel channel;
    static Device device;

    public DeviceConsole() {

    }

    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 客户端只需要workerGroup，命名更规范
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            // 关键修复1：添加和服务端匹配的字符串编解码器
                            p.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            p.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            p.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                    System.out.println("收到服务端响应: " + s);
                                }
                            });
                        }
                    });

            boolean flag = true;
            do {
                try {
                    ChannelFuture f = b.connect("127.0.0.1", port).sync();
                    channel = f.channel();
                    System.out.println("连接成功");
                    flag = false;
                } catch (Exception e) {
                    System.out.println("连接失败，1秒后重试...");
                    Thread.sleep(1000);
                }
            } while (flag);

            register();
            // 关键：等待通道关闭，避免主线程退出导致客户端断开
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关键：优雅关闭EventLoopGroup
            workerGroup.shutdownGracefully();
        }
    }

    static void register() {
        device = new Device();
        device.name="用户命令控制台";
        device.capability="用户通过该设备向系统发布自然语言命令，以及接收系统的反馈文本信息";
        Action action = new Action();
        action.setCapability("向用户显示反馈文本信息");
        action.setName("输出");
        Action.Args args = new Action.Args(Action.Args.ArgsType.STRING, "要给用户显示的文本信息");
        action.addInputArgs(args);
        System.out.println("发送注册信息");
        // 关键修复2：添加换行符，匹配服务端的LineBasedFrameDecoder
        channel.writeAndFlush(new DeviceEvent(DeviceEvent.EventType.REGISTER, device)+"\n");
        System.out.println(device.name);
    }
}