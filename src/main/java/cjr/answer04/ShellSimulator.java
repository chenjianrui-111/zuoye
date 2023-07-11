package cjr.answer04;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author CJR
 */
public class ShellSimulator {
    public  static HashMap<String,String> directory = new HashMap<>();
    public static void main(String[] args) {
        directory.put("linuxData.txt","src\\main\\resources\\");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command = null;
        try {
            command = br.readLine();
            executeCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void executeCommand(String command) {
        String[] parts = command.split("\\|");
        String output = null;
        for (int i = 0; i < parts.length; i++) {
            String subCommand = parts[i].trim();
            if (subCommand.startsWith("cat")) {
                output = executeCatCommand(subCommand, output);
            } else if (subCommand.startsWith("grep")) {
                output = executeGrepCommand(subCommand, output);
            } else if (subCommand.startsWith("wc")) {
                executeWcCommand(subCommand, output);
            }
        }
    }

    private static String executeCatCommand(String command, String input) {
        String[] catCommand = command.split(" ");
        String filename = catCommand[1].trim();
        System.out.println(filename);
        if (!directory.containsKey(filename)) {
            System.out.println("File not found: " + filename);
            return null;
        }
        String filePath = directory.get(filename);
        String content = readFileContent(filePath, filename);
        if (content == null) {
            return "File not found: " + filename;
        }

        return content;
    }

    private static String executeGrepCommand(String command, String input) {
        String[] grepCommand = command.split(" ");
        String keyword = grepCommand[1].trim();
        String filteredContent = filterContent(input, keyword);
        if (filteredContent != null) {
            System.out.println(filteredContent);
            return filteredContent;
        } else {
            System.out.println("No matching lines found for keyword: " + keyword);
            return null;
        }
    }

    private static void executeWcCommand(String command, String input) {
        String[] wcCommand = command.split("-l ");

        if (input != null){
            int lineCount = countLines(input);
            System.out.println("Line count: " + lineCount);
        }else {
            String newCommand = wcCommand[0] +wcCommand[1];
            String catCommand = executeCatCommand(newCommand, input);
            int lines = countLines(catCommand);
            System.out.println(lines);
        }

    }

    private static String readFileContent(String filePath, String filename) {
        Path fullPath = Paths.get(filePath, filename);
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            return contentBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String filterContent(String input, String keyword) {
        if (input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(keyword);
        Matcher matcher = pattern.matcher(input);
        StringBuilder filteredContentBuilder = new StringBuilder();
        while (matcher.find()) {
            filteredContentBuilder.append(matcher.group()).append("\n");
        }
        return filteredContentBuilder.toString();
    }

    private static int countLines(String input) {
        if (input == null) {
            return 0;
        }
        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineCount;
    }
}
