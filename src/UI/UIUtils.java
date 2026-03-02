package UI;

import javax.swing.*;
import java.awt.*;

/// Utilities related to the UI.
public class UIUtils {

    /// Sets the cursor to hand when it hovers above any button.
    public static void setHandCursorForAllButtons(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (comp instanceof Container) {
                setHandCursorForAllButtons((Container) comp);
            }
        }
    }
}
