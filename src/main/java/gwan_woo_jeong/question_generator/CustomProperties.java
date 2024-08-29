package gwan_woo_jeong.question_generator;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.nio.file.Path;
import java.nio.file.Files;

public class CustomProperties {
    private static final Properties properties = new Properties();
    private static final Path configFile = Path.of("C:\\class\\sy-html-downloader\\properties"); // 설정 파일 경로

    public static void loadDefaults() {
        if (!Files.exists(configFile)) {
            try {
                // 부모 디렉토리 생성
                Files.createDirectories(configFile.getParent());

                // 새로운 파일 생성
                Files.createFile(configFile);

                // 기본값 저장
                saveDefaults(GlobalValues.defaultTargetUrl, GlobalValues.defaultFileDirPath, GlobalValues.defaultFileNameFormat);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "설정 파일을 생성하는 중 오류가 발생했습니다: " + e.getMessage());
                return;
            }
        }

        // 파일에서 기본값 로드
        try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
            properties.load(fis); // 파일에서 기본값 로드

            // 텍스트 필드에 값 설정
            GlobalValues.targetUrl = properties.getProperty("targetUrl");
            GlobalValues.fileDirPath = properties.getProperty("fileDirPath");
            GlobalValues.fileNameFormat = properties.getProperty("fileNameFormat");

        } catch (IOException e) {
            e.printStackTrace(); // 파일 읽기 오류 처리
            JOptionPane.showMessageDialog(null, "기본값을 로드하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public static void saveDefaults(String targetUrl, String fileDirPath, String fileNameFormat) {
        properties.setProperty("targetUrl", targetUrl);
        properties.setProperty("fileDirPath", fileDirPath);
        properties.setProperty("fileNameFormat", fileNameFormat);
        saveValues();
    }

    public static void saveDefaults(GeneratorView view) {
        properties.setProperty("targetUrl", view.getTargetUrlField().getText());
        properties.setProperty("fileDirPath", view.getFileDirPathField().getText());
        properties.setProperty("fileNameFormat", view.getFileNameFormatField().getText());
       saveValues();
    }

    private static void saveValues() {
        try (FileOutputStream fos = new FileOutputStream(configFile.toFile())) {
            properties.store(fos, "Default properties"); // 기본값을 파일에 저장
        } catch (IOException e) {
            e.printStackTrace(); // 파일 저장 오류 처리
            JOptionPane.showMessageDialog(null, "기본값을 저장하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
