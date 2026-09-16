package ma.WhiteLab.mvc.ui.palette.charts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class SimpleChart extends JPanel {

    public enum Type { BAR, PIE }

    private final Type type;
    private final String title;
    private Map<String, Double> data;
    
    // Modern Palette
    private final Color[] colors = {
            new Color(52, 152, 219), // Blue
            new Color(46, 204, 113), // Green
            new Color(241, 196, 15), // Yellow
            new Color(231, 76, 60),  // Red
            new Color(155, 89, 182), // Purple
            new Color(52, 73, 94),   // Dark Blue
            new Color(22, 160, 133), // Teal
            new Color(230, 126, 34)  // Orange
    };

    public SimpleChart(String title, Type type) {
        this.title = title;
        this.type = type;
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            paintEmpty(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Title
        g2.setColor(new Color(80, 80, 80));
        g2.setFont(new Font("Optima", Font.BOLD, 16));
        g2.drawString(title, 15, 25);

        int w = getWidth();
        int h = getHeight();
        int topMargin = 40;

        if (type == Type.BAR) paintBarChart(g2, w, h, topMargin);
        else paintPieChart(g2, w, h, topMargin);
    }

    private void paintEmpty(Graphics g) {
        g.setColor(Color.GRAY);
        g.drawString(title + " (Aucune donnée)", 15, 25);
    }

    private void paintBarChart(Graphics2D g2, int w, int h, int top) {
        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (max == 0) max = 1;

        int bars = data.size();
        int availableWidth = w - 40; 
        int barWidth = Math.min(60, availableWidth / bars);
        int gap = (availableWidth - (bars * barWidth)) / (bars + 1);
        int bottomMargin = 30;
        int chartH = h - top - bottomMargin;

        int idx = 0;
        int x = 20 + gap;

        for (Map.Entry<String, Double> e : data.entrySet()) {
            double val = e.getValue();
            int barH = (int) ((val / max) * (chartH - 20));

            g2.setColor(colors[idx % colors.length]);
            g2.fillRoundRect(x, top + chartH - barH, barWidth, barH, 5, 5);

            // Value
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String valStr = String.format("%.0f", val);
            int tw = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, x + (barWidth - tw) / 2, top + chartH - barH - 5);

            // Label
            String cat = e.getKey();
            if (cat.length() > 8) cat = cat.substring(0, 6) + "..";
            int cw = g2.getFontMetrics().stringWidth(cat);
            g2.drawString(cat, x + (barWidth - cw) / 2, h - 10);

            x += barWidth + gap;
            idx++;
        }
    }

    private void paintPieChart(Graphics2D g2, int w, int h, int top) {
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return;

        // Pie bounds
        int d = Math.min(w / 2, h - top - 20); // Use half width to leave room for legend
        int x = 20; 
        int y = top + (h - top - d) / 2;

        double currentAngle = 90;
        int idx = 0;

        List<String> keys = new ArrayList<>(data.keySet());

        // Draw Pie Slices
        for (String key : keys) {
            Double val = data.get(key);
            double angle = (val / total) * 360.0;
            
            g2.setColor(colors[idx % colors.length]);
            g2.fill(new Arc2D.Double(x, y, d, d, currentAngle, -angle, Arc2D.PIE));
            
            currentAngle -= angle;
            idx++;
        }

        // Draw Legend (Right side)
        int lx = x + d + 30; // Legend X
        int ly = top + 20;   // Legend Y start
        
        idx = 0;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        for (String key : keys) {
            Double val = data.get(key);
            double pct = (val / total) * 100;

            g2.setColor(colors[idx % colors.length]);
            g2.fillRoundRect(lx, ly, 12, 12, 3, 3); // Color box

            g2.setColor(Color.DARK_GRAY);
            String label = String.format("%s (%.0f%%)", key, pct);
            g2.drawString(label, lx + 20, ly + 11);

            ly += 25; // Line height
            idx++;
        }
    }
}