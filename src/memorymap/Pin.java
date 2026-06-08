package memorymap;

import java.awt.*;
import java.io.Serializable;

/**
 * 🌟 Represents a memory pin on the map.
 */
public class Pin implements Serializable {
    private static final long serialVersionUID = 1L;
    private int x, y;
    private String note;

    // Adjust the click area (bigger = easier to click)
    private static final int CLICK_RADIUS = 15;

    public Pin(int x, int y, String note) {
        this.x = x;
        this.y = y;
        this.note = note;
    }

    public boolean contains(int mx, int my) {
        // Check if click is close to the center of the pin
        return Math.hypot(mx - x, my - y) <= CLICK_RADIUS;
    }

    public void draw(Graphics2D g2) {
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        g2.drawString("🌟", x - 11, y + 8); // centered visually
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
