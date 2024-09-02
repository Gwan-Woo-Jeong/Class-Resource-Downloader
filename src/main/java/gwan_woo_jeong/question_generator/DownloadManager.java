package gwan_woo_jeong.question_generator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadManager {
    public static void execute(HashMap<Integer, String> targetQuestions) throws IOException {
        String folderName;
        String subUrl;

        for (Integer questionNumber : targetQuestions.keySet()) {
            folderName = String.format(GlobalValues.fileNameFormat, questionNumber); // 생성할 폴더 이름
            subUrl = targetQuestions.get(questionNumber);

            try {
                Path path = Files.createDirectories(
                        Paths.get(GlobalValues.fileDirPath, folderName)
                );

                Document doc = downloadDocument(
                        Files.createDirectories(
                                path
                        ),
                        subUrl
                );

                if (hasOnClickRunPage(doc)) {
                    downloadDocument(
                            path,
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

        // url() 참조 리소스 다운로드
        downloadUrls(doc, saveDir);
    }

    private static void downloadFiles(Elements elements, String attr, Path saveDir) {
        Path assetDir = saveDir.resolve("asset");

        try {
            if (!Files.exists(assetDir)) {
                Files.createDirectories(assetDir);
            }
        } catch (IOException e) {
            System.err.println("asset 폴더 생성에 실패했습니다: " + e.getMessage());
            return;
        }

        boolean filesDownloaded = false;

        for (Element element : elements) {
            String fileUrl = element.absUrl(attr);

            try {
                URL url = new URL(fileUrl);

                String fileName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                String newFilePath = assetDir.resolve(fileName).toString();

                if (downloadFile(fileUrl, newFilePath)) {
                    filesDownloaded = true;
                    element.attr(attr, "asset/" + fileName);
                }
            } catch (Exception e) {
                System.err.println("파일 다운로드 중 오류 발생: " + e.getMessage());
            }
        }

        // asset 디렉토리가 비어있으면 삭제
        try {
            if (!filesDownloaded && Files.exists(assetDir) && isDirectoryEmpty(assetDir)) {
                Files.delete(assetDir);
            }
        } catch (IOException e) {
            System.err.println("asset 폴더 삭제에 실패했습니다: " + e.getMessage());
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


    private static void downloadUrls(Document doc, Path saveDir) {
        // CSS 파일 다운로드
        downloadCssFiles(doc, saveDir);

        // 스타일 태그에서 URL 다운로드
        downloadUrlsFromStyleTags(doc, saveDir);
    }

    private static void downloadCssFiles(Document doc, Path saveDir) {
        Elements cssLinks = doc.select("link[href$=.css]");

        for (Element cssLink : cssLinks) {
            String cssUrl = cssLink.absUrl("href");
            downloadFileFromUrl(cssUrl, saveDir);
        }
    }

    private static void downloadUrlsFromStyleTags(Document doc, Path saveDir) {
        try {
            Elements styleTags = doc.select("style");

            for (Element style : styleTags) {
                String styleContent = style.html(); // <style> 태그의 내용
                styleContent = extractAndDownloadUrls(styleContent, saveDir, new URL(doc.baseUri()));
                style.html(styleContent);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void downloadFileFromUrl(String fileUrl, Path saveDir) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    String cssContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    extractAndDownloadUrls(cssContent, saveDir, new URL(fileUrl));
                }
            } else {
                System.err.println("파일 다운로드할 수 없습니다: " + connection.getResponseCode() + "\n주소: " + url);
            }
        } catch (Exception e) {
            System.err.println("파일 다운로드 중 오류 발생: " + e.getMessage());
        }
    }

    private static String extractAndDownloadUrls(String content, Path saveDir, URL absUrl) {
        Pattern pattern = Pattern.compile("url\\(['\"]?(.*?)['\"]?\\)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String imageUrl = matcher.group(1); // URL 추출

            if (isFontUrl(imageUrl)) {
                continue;
            }

            try {
                URL url = new URL(absUrl, imageUrl);

                String fileName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                Path assetDir = saveDir.resolve("asset");
                String newFilePath = assetDir.resolve(fileName).toString();

                if (downloadFile(url.toString(), newFilePath)) {
                    System.out.println("다운로드 완료: " + newFilePath);
                    content = content.replace(imageUrl, "asset/" + fileName);
                }

            } catch (Exception e) {
                System.err.println("url() 다운로드 중 오류 발생: " + e.getMessage());
            }
        }
        return content; // 수정된 내용을 반환
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

    private static boolean isFontUrl(String url) {
        return url.contains(".woff") || url.contains(".woff2") || url.contains(".ttf") || url.contains(".otf") || url.contains(".eot") || url.contains("fonts/");
    }
}