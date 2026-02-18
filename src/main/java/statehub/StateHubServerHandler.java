package statehub;

import common.Envelope;
import common.EventTypes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

public class StateHubServerHandler extends SimpleChannelInboundHandler<String> {
    private final StateHub stateHub;

    public StateHubServerHandler(StateHub stateHub) {
        this.stateHub = stateHub;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Envelope envelope = common.Jsons.MAPPER.readValue(msg, Envelope.class);
        stateHub.onIncomingFromOrgan(ctx.channel(), envelope);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
