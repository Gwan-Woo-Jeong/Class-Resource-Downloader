package gwan_woo_jeong.question_generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    public static HashMap<Integer, String> getNewQuestions() throws IOException {
        Document doc = fetchDocument(GlobalValues.targetUrl);

        HashMap<Integer, String> newQuestions = new HashMap<>();
        Elements questions = doc.select(".item-question");

        Pattern pattern = Pattern.compile("'([^']*)*'");

        String url;
        int questionNumber;

        for (Element question : questions) {
            questionNumber = Integer.parseInt(question.select("span").first().text());
            Matcher matcher = pattern.matcher(question.attr("onClick"));
            if(matcher.find()) {
                url = matcher.group().replaceAll("'", "");
                newQuestions.put(questionNumber, url.toLowerCase());
            }
        }

        return newQuestions;
    }

    public static Document fetchDocument(String targetUrl) throws IOException {
        return Jsoup.connect(targetUrl)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                .header("scheme", "https")
                .header("accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("accept-encoding", "gzip, deflate, br")
                .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7,es;q=0.6")
                .header("cache-control", "no-cache").header("pragma", "no-cache")
                .header("upgrade-insecure-requests", "1").get();
    }
}
