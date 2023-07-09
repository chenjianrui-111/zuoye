package cjr.answer05;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CJR
 */
public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server started. Listening on port 8080...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // 处理客户端请求
                new Thread(() -> processClientRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processClientRequest(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream output = clientSocket.getOutputStream();

            // 读取客户端请求的网址
            String url = reader.readLine();

            // 通过HTTP获取网页内容
            String webpageContent = getWebpageContent(url);
            System.out.println(webpageContent);

            //获取页面元素实际内容
            String ActualPageContent = getActualPageContent(url);
            System.out.println(ActualPageContent);

            // 判断网页内容的数据类型
            String contentType = determineContentType(webpageContent);
            System.out.println(contentType);

            // 统计字符数、页面实际字符数、汉字数、英文字符数、标点符号数、页面实际标点符号数
            int totalCharCount = webpageContent.length();
            int actualCharCount = countDisplayedCharacters(ActualPageContent);
            int chineseCharCount = countChineseCharacters(webpageContent,contentType);
            int englishCharCount = countEnglishCharacters(webpageContent,contentType);
            int punctuationCount = countPunctuationCharacters(webpageContent);
            int actualPunctuationCount = countPunctuationCharacters(ActualPageContent);

            // 构建统计结果字符串
            String result = "Total characters: " + totalCharCount + "\n" +
                    "Actual characters:" + actualCharCount +"\n" +
                    "Chinese characters: " + chineseCharCount + "\n" +
                    "English characters: " + englishCharCount + "\n" +
                    "Punctuation characters: " + punctuationCount + "\n" +
                    "Actual Punctuation characters:" + actualPunctuationCount;

            // 将处理结果发送给客户端
            output.write(result.getBytes());

            // 关闭连接
            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断页面数据返回类型
     */
    private static String determineContentType(String webpageContent) {
        // 根据网页内容的特征判断数据类型，可以使用正则表达式、字符串匹配等方法进行判断
        if (webpageContent.contains("content-type") || webpageContent.contains("Content-Type")) {
            String regex = "<meta[^>]*http-equiv[^>]*content-type[^>]*>";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(webpageContent);

            if (matcher.find()) {
                String metaTag = matcher.group(0);
                String regex2 = "content=\"(.*?)\"";
                Pattern contentTypePattern = Pattern.compile(regex2);
                Matcher contentTypeMatcher = contentTypePattern.matcher(metaTag);

                if (contentTypeMatcher.find()) {
                    String contentType = contentTypeMatcher.group(1);

                    if (contentType.contains("text/plain")) {
                        return "text/plain";
                    } else if (contentType.contains("application/json")) {
                        return "application/json";
                    } else if (contentType.contains("application/xml")) {
                        return "application/xml";
                    } else if (contentType.contains("text/html")) {
                        return "text/html";
                    }
                }
            }
        }

        return "unknown";
    }

    /**
     * 页面返回纯文本
     * @param url
     * @return
     */
    private static String getWebpageContent(String url) {
        StringBuilder content = new StringBuilder();

        try {
            URL webpageUrl = new URL(url);
            URLConnection connection = webpageUrl.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    /**
     * 获取元素自身文本内容
     * @param
     * @return
     */
    private static String getActualPageContent(String url) throws IOException {
        StringBuilder content = new StringBuilder();
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        Elements elements = doc.getAllElements();
        for (org.jsoup.nodes.Element element : elements) {
            content.append(element.ownText());  // 仅获取元素自身的文本内容
        }
        return content.toString();
    }

    private static int countDisplayedCharacters(String text) {
        // 统计页面实际展示字符数量的逻辑
        return text.length();
    }

    private static JSONObject parseJsonContent(String jsonContent) {
        return JSON.parseObject(jsonContent);
    }


    /**
     * 统计汉字数量
     * @param text
     * @return
     */
    private static int countChineseCharacters(String text, String contentType) {
        int count = 0;

        if (contentType.equalsIgnoreCase("text/plain")) {
            // 处理纯文本类型的数据
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (isChineseCharacter(c)) {
                    count++;
                }
            }
        } else if (contentType.equalsIgnoreCase("application/json")) {
            // 处理JSON类型的数据
            count = countChineseCharactersUsingSegmenter(text);
        } else if (contentType.equalsIgnoreCase("application/xml")) {
            // 处理XML类型的数据
            count = countChineseCharactersUsingSegmenter(text);
        } else if ( contentType.equalsIgnoreCase("text/html")){
            // 处理text/html类型的数据
            org.jsoup.nodes.Document doc = Jsoup.parse(text);
            String textContent = doc.text();

            count = countChineseCharactersInText(textContent);
        }

        return count;
    }

    private static int countChineseCharactersInText(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isChineseCharacter(c)) {
                count++;
            }
        }
        return count;
    }

    private static int countChineseCharactersUsingSegmenter(String text) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        int count = 0;

        List<String> segments = segmenter.sentenceProcess(text);
        for (String segment : segments) {
            for (int i = 0; i < segment.length(); i++) {
                char c = segment.charAt(i);
                if (isChineseCharacter(c)) {
                    count++;
                }
            }
        }

        return count;
    }


    /**
     * 判断字符是否为汉字
     */
    private static boolean isChineseCharacter(char c) {
        // 判断字符是否在汉字的Unicode范围内
        // 汉字的Unicode范围：[0x4E00, 0x9FA5]
        return c >= 0x4E00 && c <= 0x9FA5;
    }


    /**
     * 处理Json数据汉字统计
     * @param text
     * @return
     */
    private static int countJsonChineseCharacters(String text,String contentType) {
        int count = 0;
        if (contentType.equalsIgnoreCase("application/json")) {
            // JSON 格式，使用 Jackson 解析库解析文本，统计汉字数量
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode rootNode = objectMapper.readTree(text);
                count = countChineseCharactersInJson(rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private static int countChineseCharactersInJson(JsonNode jsonNode) {
        int count = 0;
        if (jsonNode.isTextual()) {
            String value = jsonNode.asText();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (isChineseCharacter(c)) {
                    count++;
                }
            }
        } else if (jsonNode.isObject()) {
            Iterator<JsonNode> fields = jsonNode.elements();
            while (fields.hasNext()) {
                JsonNode field = fields.next();
                count += countChineseCharactersInJson(field);
            }
        } else if (jsonNode.isArray()) {
            Iterator<JsonNode> elements = jsonNode.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                count += countChineseCharactersInJson(element);
            }
        }
        return count;
    }

    /**
     * 处理xml格式汉字统计
     * @param text
     * @return
     */
    private static int countXmlChineseCharacters(String text,String contentType) {
        int count = 0;
        if (contentType.equalsIgnoreCase("application/xml")) {
            // 处理 XML 格式的数据
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(text)));
                count = countChineseCharactersInXml(document.getDocumentElement());
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private static int countChineseCharactersInXml(Element element) {
        int count = 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                count += countChineseCharactersInXml((Element) child);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String value = child.getNodeValue();
                if (value != null) {
                    String normalizedValue = normalizeText(value);
                    for (int j = 0; j < normalizedValue.length(); j++) {
                        char c = normalizedValue.charAt(j);
                        if (isChineseCharacter(c)) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private static String normalizeText(String text) {
        // 去除换行符、制表符等特殊字符
        text = text.replaceAll("\\s+", "");
        // 将全角字符转换为半角字符
        text = StringUtils.normalizeSpace(text);
        return text;
    }


    /**统计英文字符数量
     *
     * @param text
     * @param contentType
     * @return
     */
    private static int countEnglishCharacters(String text,String contentType) {
        // 统计英文字符数量的逻辑
        int count = 0;

        if (contentType.equals("application/json")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(text);

                // 提取文本内容
                String content = rootNode.get("content").asText();

                count = getEnglishCharacterNum(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (contentType.equals("application/xml")) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(text)));

                // 提取文本内容
                NodeList nodeList = document.getElementsByTagName("content");
                if (nodeList.getLength() > 0) {
                    Element element = (Element) nodeList.item(0);
                    String content = element.getTextContent();

                    count = getEnglishCharacterNum(content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (contentType.equals("text/plain")){
            // 处理文本类型数据的英文字符数量统计逻辑
            // 过滤特殊字符
            String filteredContent = text.replaceAll("[^a-zA-Z]", "");
            count = getEnglishCharacterNum(filteredContent);
        }else {
            //处理text/html类型
            org.jsoup.nodes.Document doc = Jsoup.parse(text);
            String content = doc.text();
            // 过滤特殊字符
            String filteredContent = content.replaceAll("[^a-zA-Z]", "");
            count = getEnglishCharacterNum(filteredContent);

        }

        return count;
    }

    /**
     * 统计英文字符数量
     */
    private static int getEnglishCharacterNum(String content){
        int count = 0;
        // 统计英文字符数量的逻辑
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (isEnglishCharacter(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断是否为英文字符
     * @param
     * @return
     */
    private static boolean isEnglishCharacter(char c) {
        // 判断字符是否为英文字符的逻辑
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * 统计标点符号
     * @param text
     * @return
     */
    private static int countPunctuationCharacters(String text) {
        // 统计标点符号数量的逻辑
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isPunctuationCharacter(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断其是否为标点符号
     * @param c
     * @return
     */
    private static boolean isPunctuationCharacter(char c) {
        // 判断字符是否为标点符号的逻辑
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

}

