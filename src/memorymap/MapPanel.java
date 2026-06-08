package memorymap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;


/**
 * 🌟 Interactive Indian Memory Map — smooth fade-in, zoom gestures, scrolling, and hover animations
 */
public class MapPanel extends JPanel {
    private Image mapImage;
    private java.util.List<Pin> pins = new ArrayList<>();
    private JFrame parentFrame;
    private JScrollPane scrollPane;
    private double scale = 0.7; // 🖐️ For smooth panning and inertia scroll
    private Point lastDragPoint = null;
    private double velocityX = 0;
    private double velocityY = 0;
    private javax.swing.Timer inertiaTimer;


    // Tooltip handling
    private Pin hoveredPin = null;
    private Point mousePoint = null;
    private float tooltipAlpha = 0f;
    private javax.swing.Timer fadeTimer = null;
    private int tooltipYOffset = 15;

    // Fade-in for map startup
    private float mapAlpha = 0f;
    private javax.swing.Timer mapFadeTimer;

    public MapPanel(JFrame frame, JScrollPane scrollPane) {
        this.parentFrame = frame;
        this.scrollPane = scrollPane;

        // 🗺️ Load map image
        mapImage = new ImageIcon(getClass().getResource("/memorymap/INDIAN_MAP.png")).getImage();
        setPreferredSize(new Dimension(mapImage.getWidth(null), mapImage.getHeight(null)));

        // 💨 Fade in animation for the map
        startMapFadeIn();

        // 🖱️ Mouse clicks for adding/editing pins
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int adjustedX = (int) (e.getX() / scale);
                int adjustedY = (int) (e.getY() / scale);

                for (Pin pin : pins) {
                    if (pin.contains(adjustedX, adjustedY)) {
                        // Only open dialog — no right-click delete anymore
                        if (showStyledDialog(pin)) savePinsToFile();
                        repaint();
                        return;
                    }
                }

                // Add a new pin if no existing pin was clicked
                Pin newPin = new Pin(adjustedX, adjustedY, "");
                if (showStyledDialog(newPin)) {
                    pins.add(newPin);
                    savePinsToFile();
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredPin = null;
                startFadeOut();
            }
        });



        // 🖱️ Hover effect
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int adjustedX = (int) (e.getX() / scale);
                int adjustedY = (int) (e.getY() / scale);
                mousePoint = e.getPoint();

                Pin newHovered = null;
                for (Pin pin : pins) {
                    if (pin.contains(adjustedX, adjustedY)) {
                        newHovered = pin;
                        break;
                    }
                }

                if (newHovered != hoveredPin) {
                    hoveredPin = newHovered;
                    if (hoveredPin != null)
                        startFadeIn();
                    else
                        startFadeOut();
                }
                repaint();
            }
        });
        

        // 🖱️ Mouse wheel + touchpad zoom
     // 🎚️ Improved wheel control — scroll normally, zoom only on big gestures
     // 🖱️ Smooth scrolling + zoom (trackpad-friendly)
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double rotation = e.getPreciseWheelRotation();

                // Ctrl + scroll → zoom
                if (e.isControlDown()) {
                    Point mousePos = e.getPoint();
                    double oldScale = scale;

                    if (rotation < 0)
                        scale = Math.min(scale + 0.1, 3.0);
                    else
                        scale = Math.max(scale - 0.1, 0.3);

                    zoomAround(mousePos, oldScale);
                } 
                // Otherwise → scroll normally
                else {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    JScrollBar horizontal = scrollPane.getHorizontalScrollBar();

                    if (e.isShiftDown())
                        horizontal.setValue(horizontal.getValue() + (int) (rotation * 40));
                    else
                        vertical.setValue(vertical.getValue() + (int) (rotation * 40));
                }
            }
        });




     // 🖱️ Drag to pan
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JViewport viewport = scrollPane.getViewport();

                if (lastDragPoint != null) {
                    Point current = e.getPoint();
                    Point viewPos = viewport.getViewPosition();

                    int dx = lastDragPoint.x - current.x;
                    int dy = lastDragPoint.y - current.y;

                    viewport.setViewPosition(new Point(viewPos.x + dx, viewPos.y + dy));

                    // Track velocity for inertia
                    velocityX = dx;
                    velocityY = dy;
                }

                lastDragPoint = e.getPoint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // Start inertia scroll when user releases
                startInertiaScroll();
                lastDragPoint = null;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Stop inertia scroll while dragging
                stopInertiaScroll();
                lastDragPoint = e.getPoint();
            }
        });

    }

    // 🕹️ Zoom Controls
    public void zoomIn() {
        double oldScale = scale;
        scale = Math.min(scale + 0.1, 3.0);
        zoomAround(getVisibleCenter(), oldScale);
    }

    public void zoomOut() {
        double oldScale = scale;
        scale = Math.max(scale - 0.1, 0.3);
        zoomAround(getVisibleCenter(), oldScale);
    }

    private Point getVisibleCenter() {
        JViewport viewport = scrollPane.getViewport();
        Point viewPos = viewport.getViewPosition();
        Dimension viewSize = viewport.getSize();
        return new Point(viewPos.x + viewSize.width / 2, viewPos.y + viewSize.height / 2);
    }

    private void zoomAround(Point mousePoint, double oldScale) {
        double zoomFactor = scale / oldScale;
        JViewport viewport = scrollPane.getViewport();
        Point viewPos = viewport.getViewPosition();

        int newViewX = (int) ((mousePoint.x + viewPos.x) * zoomFactor - mousePoint.x);
        int newViewY = (int) ((mousePoint.y + viewPos.y) * zoomFactor - mousePoint.y);

        revalidate();
        repaint();

        // 🌟 Add smooth repaint timer
        javax.swing.Timer smoothZoomTimer = new javax.swing.Timer(10, e -> repaint());
        smoothZoomTimer.setRepeats(false);
        smoothZoomTimer.start();

        SwingUtilities.invokeLater(() -> {
            viewport.setViewPosition(new Point(newViewX, newViewY));
        });
    }


    // ✨ Fade-in for the map on startup
    private void startMapFadeIn() {
        mapFadeTimer = new javax.swing.Timer(30, e -> {
            mapAlpha = Math.min(1f, mapAlpha + 0.05f);
            repaint();
            if (mapAlpha >= 1f) ((javax.swing.Timer) e.getSource()).stop();
        });
        mapFadeTimer.start();
    }

    // 💬 Stylish dialog for notes (emoji supported)
 // 💬 Stylish dialog for notes (emoji supported + delete option)
    private boolean showStyledDialog(Pin pin) {
        JTextArea noteArea = new JTextArea(pin.getNote(), 5, 20);
        noteArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);

        JScrollPane areaScroll = new JScrollPane(noteArea);
        areaScroll.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(250, 250, 255));
        panel.add(new JLabel("🗒️ Enter or edit your memory:"), BorderLayout.NORTH);
        panel.add(areaScroll, BorderLayout.CENTER);

        // Custom buttons (OK, Cancel, Delete)
        Object[] options = {"💾 Save", "❌ Cancel"};
        if (pins.contains(pin)) { // Only show delete for existing pins
            options = new Object[]{"💾 Save", "❌ Cancel", "🗑️ Delete"};
        }

        int result = JOptionPane.showOptionDialog(
                parentFrame,
                panel,
                "Memory Note",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        // Handle user choice
        if (result == 0) { // Save
            pin.setNote(noteArea.getText().trim());
            return true;
        } else if (result == 2 && pins.contains(pin)) { // Delete
            int confirm = JOptionPane.showConfirmDialog(
                    parentFrame,
                    "Are you sure you want to delete this memory?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                pins.remove(pin);
                savePinsToFile();
                repaint();
            }
        }
        return false;
    }


    // 💾 Save pins
    public void savePinsToFile() {
        try (PrintWriter writer = new PrintWriter("pins_data.txt")) {
            for (Pin p : pins) {
                writer.println(p.getX() + "," + p.getY() + "," + p.getNote().replace("\n", "\\n"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, "Error saving pins: " + e.getMessage());
        }
    }

    // 📂 Load pins
    public void loadPinsFromFile() {
        File file = new File("pins_data.txt");
        if (!file.exists()) return;

        try (Scanner sc = new Scanner(file)) {
            pins.clear();
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",", 3);
                if (parts.length == 3) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    String note = parts[2].replace("\\n", "\n");
                    pins.add(new Pin(x, y, note));
                }
            }
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, "Error loading pins: " + e.getMessage());
        }
    }

    // 🗑️ Clear pins
    public void clearAllPins() {
        pins.clear();
        savePinsToFile();
        repaint();
    }

    // 🌫️ Tooltip Fade
    private void startFadeIn() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        fadeTimer = new javax.swing.Timer(30, e -> {
            tooltipAlpha = Math.min(1f, tooltipAlpha + 0.1f);
            tooltipYOffset = Math.max(0, tooltipYOffset - 2);
            if (tooltipAlpha >= 1f) ((javax.swing.Timer) e.getSource()).stop();
            repaint();
        });
        fadeTimer.start();
    }

    private void startFadeOut() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        fadeTimer = new javax.swing.Timer(30, e -> {
            tooltipAlpha = Math.max(0f, tooltipAlpha - 0.1f);
            tooltipYOffset = 15;
            if (tooltipAlpha <= 0f) ((javax.swing.Timer) e.getSource()).stop();
            repaint();
        });
        fadeTimer.start();
    }

    // 🖌️ Painting everything
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.scale(scale, scale);

        // Map fade-in
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, mapAlpha));
        g2.drawImage(mapImage, 0, 0, this);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Draw pins
        for (Pin pin : pins) pin.draw(g2);

        // Tooltip fade
        if (hoveredPin != null && mousePoint != null && hoveredPin.getNote() != null
                && !hoveredPin.getNote().isEmpty() && tooltipAlpha > 0f) {
            drawTooltip(g2, hoveredPin.getNote(), mousePoint, tooltipAlpha);
        }
    }

    // 💬 Tooltip display
    private void drawTooltip(Graphics2D g2, String text, Point mouse, float alpha) {
        Font titleFont = new Font("Segoe UI Emoji", Font.BOLD, 14);
        Font bodyFont = new Font("Segoe UI Emoji", Font.PLAIN, 13);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        FontMetrics fm = g2.getFontMetrics(bodyFont);
        int maxWidth = 220;
        java.util.List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split(" ")) {
            if (fm.stringWidth(currentLine + word + " ") > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word + " ");
            } else currentLine.append(word).append(" ");
        }
        lines.add(currentLine.toString());

        int width = maxWidth + 16;
        int height = (fm.getHeight() + 3) * lines.size() + 36;

        int x = (int) (mouse.x / scale) + 20;
        int y = (int) (mouse.y / scale) - 10 - tooltipYOffset;

        g2.setColor(new Color(255, 255, 255, 245));
        g2.fillRoundRect(x, y - height, width, height, 12, 12);
        g2.setColor(new Color(80, 110, 180));
        g2.drawRoundRect(x, y - height, width, height, 12, 12);

        g2.setFont(titleFont);
        g2.setColor(new Color(30, 40, 120));
        g2.drawString("📌 Memory", x + 8, y - height + 20);

        g2.setFont(bodyFont);
        g2.setColor(new Color(20, 30, 90));
        int lineY = y - height + 40;
        for (String line : lines) {
            g2.drawString(line.trim(), x + 8, lineY);
            lineY += fm.getHeight() + 3;
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (int) (mapImage.getWidth(null) * scale),
                (int) (mapImage.getHeight(null) * scale)
        );
    }
 // 🎢 Start smooth glide motion after drag release
    private void startInertiaScroll() {
        if (inertiaTimer != null && inertiaTimer.isRunning()) inertiaTimer.stop();

        inertiaTimer = new javax.swing.Timer(16, e -> {
            JViewport viewport = scrollPane.getViewport();
            Point viewPos = viewport.getViewPosition();

            viewPos.translate((int) velocityX, (int) velocityY);
            viewport.setViewPosition(viewPos);

            // Slow down gradually (friction)
            velocityX *= 0.9;
            velocityY *= 0.9;

            // Stop when almost still
            if (Math.abs(velocityX) < 0.5 && Math.abs(velocityY) < 0.5) {
                ((javax.swing.Timer) e.getSource()).stop();
            }
        });

        inertiaTimer.start();
    }

    // 🛑 Stop any ongoing glide
    private void stopInertiaScroll() {
        if (inertiaTimer != null && inertiaTimer.isRunning()) {
            inertiaTimer.stop();
        }
    }
    public void resetZoom() {
        scale = 0.7;
        revalidate();
        repaint();
    }


 
}
