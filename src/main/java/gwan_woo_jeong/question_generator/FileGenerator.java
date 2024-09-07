package gwan_woo_jeong.question_generator;

import java.io.*;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileGenerator {
    public static void generate(HashMap<Integer, String> targetQuestions) throws IOException {
        String folderName;
        String subUrl;

        for (Integer questionNumber : targetQuestions.keySet()) {
            folderName = String.format(GlobalValues.fileNameFormat, questionNumber); // 생성할 폴더 이름
            subUrl = targetQuestions.get(questionNumber);

            try {
                Path path = Files.createDirectories(
                        Paths.get(GlobalValues.fileDirPath, folderName)
                );

                downloadDocument(
                        Files.createDirectories(
                                path
                        ),
                        subUrl
                );

            } catch (IOException e) {
                System.err.println("폴더 생성에 실패했습니다: " + e.getMessage());
            }
        }
    }

    private static void downloadDocument(Path saveDir, String subUrl) throws IOException {
        String htmlContent = getHtmlContent(subUrl);

        String htmlFileName = subUrl.substring(subUrl.lastIndexOf("/") + 1);
        File filePath = saveDir.resolve(htmlFileName).toFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(htmlContent);
            System.out.println("HTML 파일이 생성되었습니다: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHtmlContent(String subUrl) {
        String baseUrl = GlobalValues.targetUrl + subUrl;

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"refresh\" content=\"0; url=" + baseUrl + "\">\n" +
                "    <title>잠시만 기다려주세요...</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            background-color: #f0f0f0;\n" +
                "            font-family: Arial, sans-serif;\n" +
                "        }\n" +
                "        .spinner {\n" +
                "            border: 8px solid #f3f3f3;\n" +
                "            border-top: 8px solid #3498db;\n" +
                "            border-radius: 50%;\n" +
                "            width: 120px;\n" +
                "            height: 120px;\n" +
                "            animation: spin 1s linear infinite;\n" +
                "            margin-bottom: 40px\n" +
                "        }\n" +
                "        @keyframes spin {\n" +
                "            0% { transform: rotate(0deg); }\n" +
                "            100% { transform: rotate(360deg); }\n" +
                "        }\n" +
                "        h1, p {\n" +
                "            text-align: center;\n" +
                "            margin: 10px 0;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"spinner\"></div>\n" +
                "    <h1>잠시만 기다려 주세요...</h1>\n" +
                "    <p>만약 이 페이지에서 멈췄다면, <a href=\"" + baseUrl + "\">여기를 클릭하세요</a>.</p>\n" +
                "</body>\n" +
                "</html>";
    }
}
