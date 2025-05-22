package com.example.util;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.util.concurrent.atomic.AtomicInteger;

public class UiUtils {

    private static final int WINDOW_WIDTH = 700;
    private static final int WINDOW_HEIGHT = 500;
    private static final String FONT_NAME = "Monospaced";
    private static final int FONT_SIZE = 14;

    private static final AtomicInteger OPEN_WINDOW_COUNT = new AtomicInteger(0);

    public static void showNonBlockingWindow(String title, String content) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);

            JTextArea textArea = new JTextArea(content);
            textArea.setEditable(false);
            textArea.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));

            frame.add(new JScrollPane(textArea));
            frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Счётчик открытых окон
            OPEN_WINDOW_COUNT.incrementAndGet();

            // Когда пользователь закрывает окно — уменьшаем счётчик
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    int remaining = OPEN_WINDOW_COUNT.decrementAndGet();
                    if (remaining <= 0) {
                        System.exit(0); // Завершаем приложение
                    }
                }
            });
        });
    }
}
