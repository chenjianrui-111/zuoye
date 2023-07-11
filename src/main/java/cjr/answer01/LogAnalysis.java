package cjr.answer01;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CJR
 */
public class LogAnalysis {

        private static final Logger logger = LogManager.getLogger(LogAnalysis.class);

        public static void main(String[] args) {
            File logFile = new File("src\\main\\resources\\access.log");
            try {
                // 读取日志文件内容
                String logContent = Files.toString(logFile, Charset.defaultCharset());

                // 统计请求总量
                int totalRequests = countTotalRequests(logContent);
                logger.info("Total requests: " + totalRequests);
                System.out.println("Total requests: " + totalRequests);
                // 统计请求最频繁的10个HTTP接口及其请求数量
                Map<String, Integer> endpointCount = countEndpointRequests(logContent);

                // 排序
                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(endpointCount.entrySet());
                sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                // Print the top 10 HTTP interfaces
                logger.info("Top 10 endpoints:");
                System.out.println("Top 10 endpoints:");
                int count = 0;
                for (Map.Entry<String, Integer> entry : sortedEntries) {
                    if (count >= 10) {
                        break;
                    }
                    String uri = entry.getKey();
                    int frequency = entry.getValue();
                    logger.info("Interface: " + uri + ", Request Count: " + frequency);
                    System.out.println("Interface: " + uri + ", Request Count: " + frequency);
                    count++;
                }

                // 统计POST和GET请求量
                int postRequests = countRequestByType(logContent, "POST");
                int getRequests = countRequestByType(logContent, "GET");
                logger.info("POST requests: " + postRequests);
                logger.info("GET requests: " + getRequests);
                System.out.println("POST requests: " + postRequests);
                System.out.println("GET requests: " + getRequests);
                // 按AAA分类输出URI
                Map<String, StringBuilder> uriCategories = new HashMap<>();
                String[] lines = logContent.split("\n");

                for (String line : lines) {
                    String uri = line.split(" ")[1];
                    String category = getCategoryFromURI(uri);

                    if (!uriCategories.containsKey(category)) {
                        uriCategories.put(category, new StringBuilder());
                    }

                    uriCategories.get(category).append(uri).append("\n");
                }
                //按 AAA 分类，输出各个类别下 URI 写入文件output.txt
                BufferedWriter writer = new BufferedWriter(new FileWriter("src\\main\\resources\\output.txt"));
                for (Map.Entry<String, StringBuilder> entry : uriCategories.entrySet()) {
                    String category = entry.getKey();
                    StringBuilder uriList = entry.getValue();

                    writer.write("Category: " + category);
                    writer.newLine();
                    writer.write("URI List:");
                    writer.newLine();
                    writer.write(uriList.toString());
                    writer.newLine();
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                logger.error("An error occurred while processing the log file.", e);
                e.printStackTrace();
            }

        }


    private static int countTotalRequests(String logContent) {
        // 实现请求总量的统计逻辑
        // 使用正则表达式匹配请求行的模式
        String regex = "^(GET|POST)\\s+.*";
        Pattern pattern = Pattern.compile(regex);

        // 根据换行符拆分日志内容为行
        String[] lines = logContent.split("\n");

        int count = 0;
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                count++;
            }
        }

        return count;

    }

    private static Map<String, Integer> countEndpointRequests(String logContent) {
        Map<String, Integer> endpointCount = new HashMap<>();
        String[] lines = logContent.split("\n");

        for (String line : lines) {
            String uri = line.split(" ")[1].split("\\?")[0];
            endpointCount.put(uri, endpointCount.getOrDefault(uri, 0) + 1);
        }

        return endpointCount;
    }

    private static int countRequestByType(String logContent, String requestType) {
        int count = 0;
        String[] lines = logContent.split("\n");

        for (String line : lines) {
            if (line.startsWith(requestType)) {
                count++;
            }
        }

        return count;
    }

    private static String getCategoryFromURI(String uri) {
        String[] parts = uri.split("/");
        return parts.length >= 2 ? parts[1] : "";
    }

}
