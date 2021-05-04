package air;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
class BarThread extends Thread {
    private static int DELAY = 500;

    JProgressBar progressBar;

    public BarThread(JProgressBar bar) {
        progressBar = bar;
    }

    public void run() {
        int minimum = progressBar.getMinimum();
        int maximum = progressBar.getMaximum();
     //   for (int i = minimum; i < maximum; i++) {
            try {
                int value = progressBar.getValue();
                progressBar.setValue(value + 1);

                Thread.sleep(DELAY);
            } catch (InterruptedException ignoredException) {
            }
        }
  //  }
}