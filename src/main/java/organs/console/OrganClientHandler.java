package organs.console;

import common.Envelope;
import common.EventTypes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OrganClientHandler extends SimpleChannelInboundHandler<String> {
    private final EarMouthConsoleOrgan organ;

    public OrganClientHandler(EarMouthConsoleOrgan organ) {
        this.organ = organ;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Envelope env = common.Jsons.MAPPER.readValue(msg, Envelope.class);
        if (EventTypes.SCHEDULER_COMMAND.equals(env.eventType()) || EventTypes.ORGAN_COMMAND.equals(env.eventType())) {
            organ.onCommand(env);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
