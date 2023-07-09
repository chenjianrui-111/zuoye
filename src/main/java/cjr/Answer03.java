package cjr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CJR
 */
public class Answer03 {
    public static void main(String[] args) {
        String propFilePath = "D:\\xiangmu\\zuoye\\src\\main\\resources\\sdxl_prop.txt";
        String templateFilePath = "D:\\xiangmu\\zuoye\\src\\main\\resources\\sdxl_template.txt";
        String outputFilePath = "sdxl.txt";

        try {
            // 读取 sdxl_prop.txt 文件内容
            List<String> propLines = readLinesFromFile(propFilePath);

            // 读取 sdxl_template.txt 文件内容
            String templateText = readTextFromFile(templateFilePath);

            // 替换占位符并生成完整小说
            String generatedText = replacePlaceholders(templateText, propLines);

            // 去除索引数字
            generatedText = removeIndexNumbers(generatedText);

            // 将完整小说写入 sdxl.txt 文件
            writeTextToFile(generatedText, outputFilePath);

            System.out.println("生成完整小说成功！");

        } catch (IOException e) {
            System.out.println("处理文件时出现错误：" + e.getMessage());
        }
    }

    // 读取文件内容，按行存储到列表中
    private static List<String> readLinesFromFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }

    // 读取文件内容并返回字符串
    private static String readTextFromFile(String filePath) throws IOException {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            text.append(line).append("\n");
        }
        reader.close();
        return text.toString();
    }

    // 替换占位符函数并生成完整小说
    private static String replacePlaceholders(String templateText, List<String> propLines) {
        StringBuilder result = new StringBuilder();
        String[] lines = templateText.split("\n");
        for (String line : lines) {
            String replacedLine = replacePlaceholder(line, propLines);
            result.append(replacedLine).append("\n");
        }
        return result.toString();
    }

    // 替换单行中的占位符
    private static String replacePlaceholder(String line, List<String> propLines) {
        String regex = "\\$(\\w+)\\((\\d+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        StringBuffer replacedLine = new StringBuffer();
        while (matcher.find()) {
            String function = matcher.group(1);
            int index = Integer.parseInt(matcher.group(2));

            String replacement = "";
            if (function.equals("natureOrder")) {
                replacement = propLines.get(index);
            } else if (function.equals("indexOrder")) {
                List<String> sortedLines = new ArrayList<>(propLines);
                Collections.sort(sortedLines, Comparator.comparingInt(o -> Integer.parseInt(o.split("\t")[0])));
                replacement = sortedLines.get(index);
            } else if (function.equals("charOrder")) {
                List<String> sortedLines = new ArrayList<>(propLines);
                Collections.sort(sortedLines, Comparator.comparing(Answer03::getAlphaChars));
                replacement = sortedLines.get(index);
            } else if (function.equals("charOrderDESC")) {
                List<String> sortedLines = new ArrayList<>(propLines);
                Collections.sort(sortedLines, Comparator.comparing(Answer03::getAlphaChars).reversed());
                replacement = sortedLines.get(index);
            }

            matcher.appendReplacement(replacedLine, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(replacedLine);
        return replacedLine.toString();
    }

    // 提取字符串中的字母并返回
    private static String getAlphaChars(String line) {
        StringBuilder alphaChars = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (Character.isLetter(c)) {
                alphaChars.append(c);
            }
        }
        return alphaChars.toString();
    }

    // 去除完整小说中的索引数字
    private static String removeIndexNumbers(String text) {
        return text.replaceAll("\\d+\\s+", "");
    }

    // 将字符串写入文件
    private static void writeTextToFile(String text, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(text);
        writer.close();
    }
}
