package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/// Circular button.
public class RoundButton extends JButton {

    private Shape shape;

    public RoundButton(String label) {
        super(label);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isArmed()) {
            g2.setColor(Color.GRAY);
        } else {
            g2.setColor(getBackground());
        }

        g2.fillOval(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
        }
        return shape.contains(x, y);
    }
}
