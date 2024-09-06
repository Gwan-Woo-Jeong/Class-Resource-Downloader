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
    private final static Pattern urlPattern = Pattern.compile("url\\(['\"]?(.*?)['\"]?\\)");
    private final static Pattern concatPattern = Pattern.compile("\"([^\"/]*?(/+)[^\"]*?)\"\\s*\\+");
    private final static Pattern pathPattern = Pattern.compile("(?<!\\+)\"(([^+\\s\"]+\\.(html))|(\\.\\.[^+\\s\"]+(\\.[^+\\s\"]+)*))\"(?=\\s*[^+])");

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
        downloadResources(doc);

        // 수정된 HTML 문서 저장
        Files.write(saveDir.resolve(htmlFileName), doc.outerHtml().getBytes());
        return doc;
    }

    private static void downloadResources(Document doc) {
        // CSS 파일 다운로드
        downloadFiles(doc.select("link[href$=.css]"), "href");

        // JS 파일 다운로드
        downloadFiles(doc.select("script[src$=.js]"), "src");

        // 이미지 파일 다운로드
        downloadFiles(doc.select("img[src]"), "src");

        // url() 참조 리소스 다운로드
        downloadUrls(doc);
    }

    private static void downloadFiles(Elements elements, String attr) {

        for (Element element : elements) {
            String fileUrl = element.absUrl(attr);

            element.attr(attr, fileUrl);
        }
    }

    private static void downloadFile(String fileUrl, String savePath) {
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
    }


    private static void downloadUrls(Document doc) {
        // CSS 파일에서 다운로드
        downloadCssFiles(doc);

        // 스타일 태그에서 URL 다운로드
        downloadUrlsFromTags(doc, "style", urlPattern);

        // 스크립트 태그에서 URL 다운로드
        downloadUrlsFromTags(doc, "script", concatPattern);
        downloadUrlsFromTags(doc, "script", pathPattern);
    }

    private static void downloadCssFiles(Document doc) {
        Elements cssLinks = doc.select("link[href$=.css]");

        for (Element cssLink : cssLinks) {
            String cssUrl = cssLink.absUrl("href");
            downloadFileFromUrl(cssUrl);
        }
    }


    private static void downloadUrlsFromTags(Document doc, String tagName, Pattern urlPattern) {
        try {
            Elements tags = doc.select(tagName);

            for (Element tag : tags) {
                String content = tag.html(); // 태그의 내용
                content = extractAndDownloadUrls(content, new URL(doc.baseUri()), urlPattern);
                tag.html(content);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void downloadFileFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    String cssContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    extractAndDownloadUrls(cssContent, new URL(fileUrl), urlPattern);
                }
            } else {
                System.err.println("파일 다운로드할 수 없습니다: " + connection.getResponseCode() + "\n주소: " + url);
            }
        } catch (Exception e) {
            System.err.println("파일 다운로드 중 오류 발생: " + e.getMessage());
        }
    }

    private static String extractAndDownloadUrls(String content, URL absUrl, Pattern pattern) {

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String matchUrl = matcher.group(1); // URL 추출

            try {
                URL url = new URL(absUrl, matchUrl); // 절대 주소 변경
                if (!content.contains(url.toString())) {
                    content = content.replace(matchUrl, url.toString());
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

}
