package cjr.answer05.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.nio.charset.StandardCharsets;


public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (request.method() == HttpMethod.GET) {
            String url = "http://" + request.headers().get(HttpHeaderNames.HOST) + request.uri();
            System.out.println("Received request for URL: " + url);

            // 发起HTTP请求获取数据
            CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                HttpGet httpGet = new HttpGet(url);

                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    System.out.println("Response: " + responseBody);

                    // 进行统计
                    int totalCharacters = responseBody.length();
                    int chineseCharacters = countChineseCharacters(responseBody);
                    int englishCharacters = countEnglishCharacters(responseBody);
                    int punctuationCount = countPunctuation(responseBody);

                    // 构建响应
                    String responseContent = "Total Characters: " + totalCharacters + "\n"
                            + "Chinese Characters: " + chineseCharacters + "\n"
                            + "English Characters: " + englishCharacters + "\n"
                            + "Punctuation Count: " + punctuationCount + "\n";

                    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseContent.getBytes()));
                    fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
                    fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());

                    ctx.writeAndFlush(fullHttpResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // 发生异常时返回错误响应
                FullHttpResponse errorResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                ctx.writeAndFlush(errorResponse);
            } finally {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static int countChineseCharacters(String content) {
        // 统计中文字符个数
        int count = 0;
        for (int i = 0; i < content.length(); i++) {
            if (isChineseCharacter(content.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private static boolean isChineseCharacter(char c) {
        // 判断字符是否为中文字符
        // 使用Unicode对中文字符的范围进行判断
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    private static int countEnglishCharacters(String content) {
        // 统计英文字符个数
        int count = 0;
        for (int i = 0; i < content.length(); i++) {
            if (isEnglishCharacter(content.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private static boolean isEnglishCharacter(char c) {
        // 判断字符是否为英文字符的逻辑
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static int countPunctuation(String content) {
        // 统计标点符号个数
        int count = 0;
        for (int i = 0; i < content.length(); i++) {
            if (isPunctuation(content.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private static boolean isPunctuation(char c) {
        // 判断字符是否为标点符号
        // 使用正则表达式进行判断
        return String.valueOf(c).matches("\\p{P}");
    }
}

