
package com.cinematch;

import javax.swing.SwingUtilities;

import com.cinematch.ui.LoginFrame;

public class CineMatchApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
