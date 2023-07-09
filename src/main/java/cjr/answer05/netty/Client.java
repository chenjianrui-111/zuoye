package cjr.answer05.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Client {
    public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(8192))
                                    .addLast(new ClientHandler());
                        }
                    });

            // 连接到服务器
            ChannelFuture f = b.connect("localhost", 8080).sync();

            // 从命令行读取用户输入的网址
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter a website URL: ");
            String websiteUrl = reader.readLine();

            // 构建HTTP请求
            URI uri = new URI(websiteUrl);
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
            request.headers().set(HttpHeaderNames.HOST, uri.getHost());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            // 发送HTTP请求
            f.channel().writeAndFlush(request);

            // 关闭连接
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    static class ClientHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                System.out.println("Response Status: " + response.status());
            }

            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                String response = buf.toString(io.netty.util.CharsetUtil.UTF_8);
                System.out.println("Response Content: \n" + response);
                ctx.close();
            }
        }
    }
}
