package memorymap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 🗺️ Digital Memory Map of India — Main Application
 * Features:
 *  - Scrollable and zoomable map
 *  - Add, edit, and delete memory pins
 *  - Auto-save and auto-load pins
 *  - Modern top toolbar UI
 */
public class MemoryMapApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MemoryMapApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        // 🪟 Create main window
        JFrame frame = new JFrame("🇮🇳 Digital Memory Map of India");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 📜 Create scroll pane (for panning)
        JScrollPane scrollPane = new JScrollPane();

        // 🗺️ Create the map panel
        MapPanel mapPanel = new MapPanel(frame, scrollPane);
        scrollPane.setViewportView(mapPanel);
     // 🧭 Start from the top-center of the map
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            Dimension mapSize = mapPanel.getPreferredSize();
            Dimension viewSize = viewport.getExtentSize();

            int centerX = Math.max(0, (mapSize.width - viewSize.width) / 2);
            int topY = 0;

            viewport.setViewPosition(new Point(centerX, topY));
        });


        // ➕➖ Create toolbar buttons
        JButton zoomInBtn = new JButton("🔍 +");
        JButton zoomOutBtn = new JButton("🔍 −");
        JButton clearPinsBtn = new JButton("🗑️ Clear All");
        JButton savePinsBtn = new JButton("💾 Save");
        JButton loadPinsBtn = new JButton("📂 Load");

        // Apply consistent style
        styleButton(zoomInBtn);
        styleButton(zoomOutBtn);
        styleButton(clearPinsBtn);
        styleButton(savePinsBtn);
        styleButton(loadPinsBtn);

        // 🎛️ Add toolbar actions
        zoomInBtn.addActionListener(e -> mapPanel.zoomIn());
        zoomOutBtn.addActionListener(e -> mapPanel.zoomOut());
        clearPinsBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                frame, "Clear all pins?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) mapPanel.clearAllPins();
        });
        savePinsBtn.addActionListener(e -> mapPanel.savePinsToFile());
        loadPinsBtn.addActionListener(e -> mapPanel.loadPinsFromFile());

        // 🧭 Create top toolbar panel
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        topBar.setBackground(new Color(240, 245, 255));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 200, 240)));

        JLabel titleLabel = new JLabel("🗺️ My Digital Memory Map");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        titleLabel.setForeground(new Color(25, 25, 80));

        topBar.add(titleLabel);
        topBar.add(Box.createHorizontalStrut(30));
        topBar.add(zoomInBtn);
        topBar.add(zoomOutBtn);
        topBar.add(savePinsBtn);
        topBar.add(loadPinsBtn);
        topBar.add(clearPinsBtn);
        topBar.add(Box.createHorizontalStrut(40));


        // ⚙️ Layout setup
        frame.setLayout(new BorderLayout());
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
     // ⌨️ Keyboard shortcuts for zooming
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control EQUALS"), "zoomIn");
        frame.getRootPane().getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.zoomIn();
            }
        });

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control MINUS"), "zoomOut");
        frame.getRootPane().getActionMap().put("zoomOut", new AbstractAction() {
        	
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.zoomOut();
            }
        });

        // Optional: Ctrl + 0 to reset zoom
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control 0"), "resetZoom");
        frame.getRootPane().getActionMap().put("resetZoom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.resetZoom();
            }
        });

        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // open full screen
        frame.setVisible(true);


        // 🔄 Auto-load pins when app starts
        SwingUtilities.invokeLater(mapPanel::loadPinsFromFile);
    }

    /**
     * 🎨 Button Styling for Toolbar
     */
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setBackground(new Color(200, 225, 255));
        button.setForeground(Color.DARK_GRAY);
        button.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 1));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
    }
}
