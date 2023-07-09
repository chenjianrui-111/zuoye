package cjr.answer05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author CJR
 */
public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            System.out.println("Connected to server: " + socket.getInetAddress().getHostAddress());

            // 从命令行读取用户输入的网址
            //https://hanyu.baidu.com/shici/detail?from=kg1&highlight=&pid=c7ada22cc5607be4ada7d458beef9b69&srcid=51369
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter the URL: ");
            String url = reader.readLine();

            // 将网址发送给服务器
            OutputStream output = socket.getOutputStream();
            output.write(url.getBytes());
            output.write('\n');
            output.flush();

            // 读取服务器返回的统计结果并输出
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = responseReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

