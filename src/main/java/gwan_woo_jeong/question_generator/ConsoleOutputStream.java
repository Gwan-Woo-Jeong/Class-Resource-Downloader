package gwan_woo_jeong.question_generator;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleOutputStream extends PrintStream {
    private final JTextArea consoleArea;

    public ConsoleOutputStream(JTextArea textArea) {
        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }, true, java.nio.charset.StandardCharsets.UTF_8); // UTF-8 인코딩 설정
        this.consoleArea = textArea;
    }

    @Override
    public void println(String x) {
        appendToConsole(x + "\n");
    }

    @Override
    public void print(String x) {
        appendToConsole(x);
    }

    @Override
    public void println() {
        appendToConsole("\n");
    }

    private void appendToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }
}
