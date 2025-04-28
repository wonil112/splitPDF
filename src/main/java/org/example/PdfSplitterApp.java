package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PdfSplitterApp extends JFrame {
    private JTextField inputField;      // 원본 PDF 경로
    private JTextField rangeField;      // 페이지 범위 입력
    private JTextField outputField;     // 저장 폴더
    private JButton selectInputButton;
    private JButton selectOutputButton;
    private JButton splitButton;
    private JLabel statusLabel;

    private File inputFile;
    private File outputDir;

    public PdfSplitterApp() {
        setTitle("PDF 분할 프로그램");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 3, 5, 5));

        inputField = new JTextField();
        rangeField = new JTextField();     // 페이지 범위 입력 필드 추가
        outputField = new JTextField();
        selectInputButton = new JButton("PDF 선택");
        selectOutputButton = new JButton("폴더 선택");
        splitButton = new JButton("쪼개기 시작");
        statusLabel = new JLabel("");

        selectInputButton.addActionListener(e -> chooseInputFile());
        selectOutputButton.addActionListener(e -> chooseOutputFolder());
        splitButton.addActionListener(e -> splitPdf());

        add(new JLabel("원본 PDF:"));
        add(inputField);
        add(selectInputButton);

        add(new JLabel("페이지 범위 (예: 1,3,5 또는 1-5):"));
        add(rangeField);    // 페이지 범위 입력 필드
        add(new JLabel(""));

        add(new JLabel("저장 폴더:"));
        add(outputField);
        add(selectOutputButton);

        add(new JLabel(""));
        add(splitButton);
        add(statusLabel);

        setVisible(true);
    }

    private void chooseInputFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            inputFile = fileChooser.getSelectedFile();
            inputField.setText(inputFile.getAbsolutePath());
        }
    }

    private void chooseOutputFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = folderChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputDir = folderChooser.getSelectedFile();
            outputField.setText(outputDir.getAbsolutePath());
        }
    }

    private void splitPdf() {
        if (inputFile == null || outputDir == null) {
            statusLabel.setText("파일과 폴더를 모두 선택하세요.");
            return;
        }

        // 범위 입력
        String rangeInput = rangeField.getText().trim();

        try {
            PDDocument document = PDDocument.load(inputFile);
            int totalPages = document.getNumberOfPages();

            // 범위 입력이 없으면 기존처럼 페이지 하나씩 쪼갬
            if (rangeInput.isEmpty()) {
                for (int i = 0; i < totalPages; i++) {
                    PDDocument singlePageDoc = new PDDocument();
                    singlePageDoc.addPage(document.getPage(i));
                    String outputFileName = new File(outputDir, "page_" + (i + 1) + ".pdf").getAbsolutePath();
                    singlePageDoc.save(outputFileName);
                    singlePageDoc.close();
                }
            } else {
                // 범위 입력이 있으면 해당 범위에 맞게 쪼갬
                List<List<Integer>> pageRanges = parseRanges(rangeInput, totalPages);

                // 각 범위에 맞게 PDF 생성
                for (List<Integer> range : pageRanges) {
                    PDDocument newDoc = new PDDocument();
                    for (int pageIndex : range) {
                        newDoc.addPage(document.getPage(pageIndex - 1)); // 0-based index
                    }

                    String outputFileName = new File(outputDir, "pages_" + range.get(0) + (range.size() > 1 ? "-" + range.get(range.size() - 1) : "") + ".pdf").getAbsolutePath();
                    newDoc.save(outputFileName);
                    newDoc.close();
                }
            }

            document.close();
            statusLabel.setText("완료!");
        } catch (IOException e) {
            statusLabel.setText("에러 발생!");
            e.printStackTrace();
        }
    }

    private List<List<Integer>> parseRanges(String rangeInput, int totalPages) {
        List<List<Integer>> pageRanges = new ArrayList<>();
        String[] ranges = rangeInput.split(",");

        for (String range : ranges) {
            range = range.trim();

            if (range.contains("-")) {
                // 범위가 "1-2" 형식일 경우
                String[] bounds = range.split("-");
                int start = Integer.parseInt(bounds[0].trim());
                int end = Integer.parseInt(bounds[1].trim());

                // 페이지 범위가 유효한지 확인
                if (start >= 1 && end <= totalPages && start <= end) {
                    List<Integer> pageRange = new ArrayList<>();
                    for (int i = start; i <= end; i++) {
                        pageRange.add(i);
                    }
                    pageRanges.add(pageRange);
                }
            } else {
                // 단일 페이지 번호일 경우
                int pageNumber = Integer.parseInt(range.trim());
                if (pageNumber >= 1 && pageNumber <= totalPages) {
                    pageRanges.add(Collections.singletonList(pageNumber));
                }
            }
        }

        return pageRanges;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PdfSplitterApp::new);
    }
}