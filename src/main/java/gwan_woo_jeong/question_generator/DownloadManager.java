package gwan_woo_jeong.question_generator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadManager {
    public static void execute(HashMap<Integer, String> targetQuestions) throws IOException {
        String folderName;
        String subUrl;

        for (Integer questionNumber : targetQuestions.keySet()) {
            folderName = String.format(GlobalValues.fileNameFormat, questionNumber); // 생성할 폴더 이름
            subUrl = targetQuestions.get(questionNumber);

            try {
                Document doc = downloadDocument(
                        Files.createDirectories(
                                Paths.get(GlobalValues.fileDirPath, folderName)
                        ),
                        subUrl
                );

                if (hasOnClickRunPage(doc)) {
                    downloadDocument(
                            Files.createDirectories(
                                    Paths.get(GlobalValues.fileDirPath, folderName, "/실행하기")
                            ),
                            subUrl.replace(".html", "_impl.html")
                    );
                }

            } catch (IOException e) {
                System.err.println("폴더 생성에 실패했습니다: " + e.getMessage());
            }
        }
    }

    private static Document downloadDocument(Path saveDir, String subUrl) throws IOException {
        String baseUrl = GlobalValues.targetUrl + subUrl;

        // HTML 문서 로드
        Document doc = WebCrawler.fetchDocument(baseUrl);

        // HTML 파일 다운로드
        String htmlFileName = subUrl.substring(subUrl.lastIndexOf("/") + 1);
        downloadFile(baseUrl, saveDir.resolve(htmlFileName).toString());

        // CSS, JS, 이미지 파일 다운로드
        downloadResources(doc, saveDir);

        // 수정된 HTML 문서 저장
        Files.write(saveDir.resolve(htmlFileName), doc.outerHtml().getBytes());
        return doc;
    }

    private static void downloadResources(Document doc, Path saveDir) {
        // CSS 파일 다운로드
        downloadFiles(doc.select("link[href$=.css]"), "href", saveDir);

        // JS 파일 다운로드
        downloadFiles(doc.select("script[src$=.js]"), "src", saveDir);

        // 이미지 파일 다운로드
        downloadFiles(doc.select("img[src]"), "src", saveDir);
    }

    private static void downloadFiles(Elements elements, String attr, Path saveDir) {
        Path assetsDir = saveDir.resolve("assets");

        try {
            if (!Files.exists(assetsDir)) {
                Files.createDirectories(assetsDir);
            }
        } catch (IOException e) {
            System.err.println("assets 폴더 생성에 실패했습니다: " + e.getMessage());
            return;
        }

        boolean filesDownloaded = false;

        for (Element element : elements) {
            String fileUrl = element.absUrl(attr);

            try {
                URL url = new URL(fileUrl);

                String fileName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                String newFilePath = assetsDir.resolve(fileName).toString();

                if (downloadFile(fileUrl, newFilePath)) {
                    filesDownloaded = true;
                    element.attr(attr, "assets/" + fileName);
                }
            } catch (Exception e) {
                System.err.println("파일 다운로드 중 오류 발생: " + e.getMessage());
            }
        }

        // assets 디렉토리가 비어있으면 삭제
        try {
            if (!filesDownloaded && Files.exists(assetsDir) && isDirectoryEmpty(assetsDir)) {
                Files.delete(assetsDir);
            }
        } catch (IOException e) {
            System.err.println("assets 폴더 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext(); // 디렉토리가 비어 있으면 true 반환
        }
    }

    private static boolean downloadFile(String fileUrl, String savePath) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(savePath)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    System.out.println("다운로드 완료: " + savePath);
                    return true; // 다운로드 성공

                } catch (FileNotFoundException e) {
                    System.err.println("파일을 찾을 수 없습니다: " + savePath);
                }
            } else {
                System.err.println("다운로드 실패: " + fileUrl + " (" + responseCode + " Error)");
            }

        } catch (IOException e) {
            System.err.println("다운로드 실패: " + fileUrl);
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return false; // 다운로드 실패
    }

    private static boolean hasOnClickRunPage(Document doc) {
        Elements elements = doc.getAllElements();
        for (Element element : elements) {
            String onClickAttr = element.attr("onClick");
            if ("runpage();".equals(onClickAttr)) {
                return true;
            }
        }
        return false;
    }
}