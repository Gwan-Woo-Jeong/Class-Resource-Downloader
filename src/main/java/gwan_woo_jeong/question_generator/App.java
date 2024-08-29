package gwan_woo_jeong.question_generator;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class App {
    public static void main(String[] args) {
        CustomProperties.loadDefaults(); // 설정값 읽기
        GeneratorView view = new GeneratorView(GlobalValues.introduction);

        ConsoleOutputStream outputStream = new ConsoleOutputStream(view.getConsoleArea());
        System.setOut(outputStream);
        System.setErr(outputStream);

        SwingUtilities.invokeLater(() -> {
            view.getCreateButton().addActionListener(e -> {
                view.disableCreateButton();
                view.clearConsole();
                GlobalValues.setValues(view);
                CustomProperties.saveDefaults(view);

                // 비동기
                SwingWorker<Void, String> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            // questions 폴더 안의 문제 목록을 가져옴
                            List<Integer> existingQuestions = QuestionManager.getExistingQuestions();

                            // 사이트에 있는 문제들을 크롤링
                            HashMap<Integer, String> newQuestions = WebCrawler.getNewQuestions();

                            // 두 문제 목록을 비교하여 아직 생성되지 않는 문제 목록을 구함
                            HashMap<Integer, String> targetQuestions = QuestionManager.getTargetQuestions(existingQuestions, newQuestions);

                            // 새로운 문제가 없는 경우
                            if (targetQuestions.isEmpty()) {
                                publish("새로운 문제가 없습니다.");
                                return null;
                            }

                            // 새로운 문제의 폴더를 생성한 후, 해당 페이지의 폴더 생성 및 리소스 다운로드
                            DownloadManager.execute(targetQuestions);
                            publish("리소스 다운로드가 완료되었습니다.");

                        } catch (IOException exc) {
                            publish("입출력 오류가 발생했습니다: " + exc.getMessage());
                            exc.printStackTrace();
                        } catch (Exception exc) {
                            publish("예상치 못한 오류가 발생했습니다: " + exc.getMessage());
                            exc.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        for (String message : chunks) {
                            System.out.println(message);
                        }
                    }

                    @Override
                    protected void done() {
                        view.enableCreateButton();
                    }
                };

                worker.execute();
            });
        });
    }
}
