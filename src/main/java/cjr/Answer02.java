package cjr;

import java.io.*;

/**
 * @author CJR
 */
public class Answer02 {
    /** 空行 */
    private static long nullLines = 0;
    /** 注释行 */
    private static long annoLines = 0;
    /** 有效代码行 */
    private static long validLineCount = 0;

    public static void main(String[] args) {
        String filePath = "D:\\xiangmu\\zuoye\\src\\main\\resources\\StringUtils.java";
        String outputFilePath = "validLineCount.txt";
         countValidLines(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(String.valueOf(validLineCount));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("有效代码行: " + validLineCount);
        System.out.println("空行:" + nullLines);
        System.out.println("注释行:" + annoLines);
    }

    private static void countValidLines(String filePath){

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimStr = line.trim();
                if (trimStr.isEmpty() && trimStr.length() == 0){
                    nullLines++;
                }else if (trimStr.startsWith("//")
                || trimStr.startsWith("/**")
                || trimStr.startsWith("/*")
                || trimStr.startsWith("*")
                || trimStr.startsWith("*/")
                ){
                    annoLines++;
                }else {
                    validLineCount++;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
