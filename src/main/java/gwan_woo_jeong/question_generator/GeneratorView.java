package gwan_woo_jeong.question_generator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GeneratorView {
    private JTextArea consoleArea;
    private JTextField targetUrlField;
    private JTextField fileDirPathField;
    private JTextField fileNameFormatField;
    private JButton createButton;

    public GeneratorView(String consoleAreaValue) {
        JFrame frame = new JFrame("Ssanyong Class Resource Downloader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // 행 간격 조정
        gbc.insets = new Insets(5, 5, 5, 5);

        // 사이트명 입력
        JLabel label1 = new JLabel("사이트 주소:");
        targetUrlField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(targetUrlField, gbc);

        // 문제 폴더 경로 입력
        JLabel label2 = new JLabel("문제 폴더 경로:");
        fileDirPathField = new JTextField(15);
        JButton browseButton = new JButton("찾아보기");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    fileDirPathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(fileDirPathField, BorderLayout.CENTER);
        folderPanel.add(browseButton, BorderLayout.EAST);
        contentPanel.add(folderPanel, gbc);

        // 문제 파일명 형식 입력
        JLabel label3 = new JLabel("문제 파일명 형식:");
        fileNameFormatField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(label3, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(fileNameFormatField, gbc);

        // 문제 생성 버튼
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        createButton = new JButton("리소스 다운로드");
        contentPanel.add(createButton, gbc);

        // 콘솔 창
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;

        consoleArea = new JTextArea(); // 콘솔 영역 인스턴스 변수 초기화
        consoleArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        contentPanel.add(scrollPane, gbc);

        // 프레임에 콘텐츠 패널 추가
        frame.add(contentPanel, BorderLayout.CENTER);

        // 프레임 설정
        frame.pack();
        frame.setSize(600, 450);

        // 초기값 설정
        consoleArea.setText(consoleAreaValue);
        targetUrlField.setText(GlobalValues.targetUrl);
        fileDirPathField.setText(GlobalValues.fileDirPath);
        fileNameFormatField.setText(GlobalValues.fileNameFormat);

        frame.setVisible(true);
    }


    public JButton getCreateButton() {
        return createButton;
    }

    public void clearConsole() {
        consoleArea.setText("");
    }

    public JTextArea getConsoleArea() {
        return consoleArea;
    }

    public JTextField getTargetUrlField() {
        return targetUrlField;
    }

    public JTextField getFileDirPathField() {
        return fileDirPathField;
    }

    public JTextField getFileNameFormatField() {
        return fileNameFormatField;
    }

    public void enableCreateButton() {
        createButton.setText("리소스 다운로드");
        createButton.setEnabled(true);
    }

    public void disableCreateButton() {
        createButton.setText("잠시만 기다려주세요...");
        createButton.setEnabled(false);
    }
}
