package gov.sandia.reframe.ui.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import gov.sandia.cortext.fileparsing.pdf.ExtractionResult;
import gov.sandia.cortext.fileparsing.pdf.PdfElement;
import gov.sandia.cortext.fileparsing.pdf.Range;
import replete.collections.Pair;
import replete.ui.ColorLib;
import replete.ui.GuiUtil;
import replete.ui.button.IconButton;
import replete.ui.drag.MouseDragHelped;
import replete.ui.drag.MouseDragHelper;
import replete.ui.drag.RectangleIterator;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.sp.RScrollPane;

public class GraphicalPanel extends RPanel {


    ////////////
    // FIELDS //
    ////////////

    private DrawPanel pnlDraw;
    private ExtractionResult result;
    private int curPage = -1;
    private JLabel lblMouse;
    private JLabel lblSel;
    private Map<Range, Pair<Color, Color>> highlights;
    private boolean resetScroll = false;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public GraphicalPanel() {
        final JCheckBox chkRegions;
        IconButton btnZoomIn, btnZoomOut;
        RScrollPane p;

        Lay.BLtg(this,
            "N", Lay.FL("L",
                chkRegions = Lay.chk("Show Regions"),
                btnZoomOut = (IconButton) Lay.btn(CommonConcepts.ZOOM_OUT),
                btnZoomIn = (IconButton) Lay.btn(CommonConcepts.ZOOM_IN),
                lblMouse = Lay.lb("Mouse:"),
                lblSel = Lay.lb("# Selected: 0")
            ),
            "C", p = Lay.sp(pnlDraw = new DrawPanel())
        );

        btnZoomIn.toImageOnly();
        btnZoomOut.toImageOnly();

        chkRegions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlDraw.setShowRegions(chkRegions.isSelected());
            }
        });
        btnZoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlDraw.changeZoom(1);
            }
        });
        btnZoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlDraw.changeZoom(-1);
            }
        });
    }


    //////////////
    // MUTATORS //
    //////////////

    public void setResult(ExtractionResult result) {
        this.result = result;
        pnlDraw.setResult(result);
    }

    public void setPage(int curPage) {
        if(curPage != this.curPage) {
            pnlDraw.requestScrollAfterRepaint = true;
        }
        this.curPage = curPage;
        pnlDraw.updateRect();
    }



    /////////////////
    // INNER CLASS //
    /////////////////

    private class DrawPanel extends RPanel implements MouseDragHelped {


        ////////////
        // FIELDS //
        ////////////

        private Map<Integer, List<GraphicalElement>> data = new HashMap<>();
        private MouseDragHelper helper = new MouseDragHelper(this);
        private boolean showRegions;
        private int X_MARGIN = 10;
        private int Y_MARGIN = 10;
        private Set<GraphicalElement> selected = new HashSet<>();
        private int zoomIndex = 0;
        private Point mousePos;
        private boolean requestScrollAfterRepaint = false;


        /////////////////
        // CONSTRUCTOR //
        /////////////////

        public DrawPanel() {
            Lay.hn(this, "bg=100");
            setMouseDragSelection(true);
            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateMousePoint(e);
                }
                @Override
                public void mouseDragged(MouseEvent e) {
                    updateMousePoint(e);
                }
            });
        }


        //////////////
        // MUTATORS //
        //////////////

        public void setShowRegions(boolean showRegions) {
            this.showRegions = showRegions;
            repaint();
        }

        private Rectangle getCurRect() {
            if(curPage == -1 || result == null || result.getPageSizes() == null) {
                return null;
            }
            return result.getPageSizes().get(curPage);
        }

        public void changeZoom(int i) {
            Rectangle r = getCurRect();
            if(r != null) {
                zoomIndex += i;
                double z = 1.0 + 0.1 * zoomIndex;
                setPreferredSize(new Dimension((int) (r.width * z), (int) (r.height * z)));
                ((JComponent) getParent().getParent().getParent()).updateUI();
                repaint();
            }
        }

        public void updateRect() {
            Rectangle r = getCurRect();
            if(r != null) {
                Dimension size = new Dimension(X_MARGIN + r.width, Y_MARGIN + r.height);
                setPreferredSize(size);
                repaint();
                invalidate();
            }
        }

        public void setResult(ExtractionResult result) {
            data.clear();

            if(result != null) {
                // Filtered PDF elements
                List<PdfElement> filteredElements = result.getFilteredElements();
                int index = 0;
                for(PdfElement element : filteredElements) {
                    int pageIdx = element.getPage() - 1;
                    List<GraphicalElement> pageData = data.get(pageIdx);
                    if(pageData == null) {
                        pageData = new ArrayList<>();
                        data.put(pageIdx, pageData);
                    }
                    GraphicalElement gElement = new GraphicalElement(element, index);
                    pageData.add(gElement);
                    index++;
                }

                // Discarded PDF elements
                List<PdfElement> discardedElements = result.getDiscardedElements();
                for(PdfElement element : discardedElements) {
                    int pageIdx = element.getPage() - 1;
                    List<GraphicalElement> pageData = data.get(pageIdx);
                    if(pageData == null) {
                        pageData = new ArrayList<>();
                        data.put(pageIdx, pageData);
                    }
                    GraphicalElement gElement = new GraphicalElement(element, -1);
                    gElement.discarded = true;
                    pageData.add(gElement);
                }
            }

            repaint();
        }

        protected List<PdfElement> findElems(int x, int y) {
            List<PdfElement> found = new ArrayList<>();
            if(curPage != -1 && data.containsKey(curPage)) {
                for(GraphicalElement ge : data.get(curPage)) {
                    PdfElement elem = ge.element;
                    if(x >= elem.getX() && x <= elem.getX() + elem.getW() &&
                                    y >= elem.getY() && y <= elem.getY() + elem.getH()) {  // -1 ?
                        found.add(elem);
                    }
                }
            }
            return found;
        }


        ///////////
        // PAINT //
        ///////////

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if(requestScrollAfterRepaint) {
                scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                requestScrollAfterRepaint = false;
            }

            Rectangle pageRect = getCurRect();
            if(pageRect != null) {

                // Set zoom transform.
                Graphics2D g2 = (Graphics2D) g;
                double z = 1.0 + 0.1 * zoomIndex;
                AffineTransform tx = g2.getTransform();
                tx.scale(z, z);
                g2.setTransform(tx);

                drawPageRegion(g, pageRect);
                drawRulers(g, pageRect);

                List<GraphicalElement> pageData = data.get(curPage);
                if(pageData != null) {
                    drawHighlights(g, pageData);
                    drawElements(g, pageData);
                }

                helper.paint(g);
            }
        }

        private void drawHighlights(Graphics g, List<GraphicalElement> pageData) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            boolean foundOne = false;

            for(GraphicalElement gElement : pageData) {
                Color hlBgColor = null;
                if(highlights != null) {
                    for(Range range : highlights.keySet()) {
                        if(gElement.index >= range.getStart() && gElement.index < range.getEndNonIncl()) {
                            hlBgColor = highlights.get(range).getValue2();
                        }
                    }
                }

                if(hlBgColor == null) {
                    continue;
                }

                String ch = gElement.element.getText();
                int chW = GuiUtil.stringWidth(g, ch);
                int chH = GuiUtil.stringHeight(g);

                int chX = X_MARGIN + gElement.getX();
                int chY = Y_MARGIN + gElement.getY() + gElement.getH();

                int chX2 = chX - 1;
                int chY2 = chY - chH + 4;
                int chW2 = chW + 2;

                g.setColor(hlBgColor);
                g.fillRect(chX2, chY2, chW2, chH);

                if(resetScroll) {
                    foundOne = true;
                    if(chX2 < minX) {
                        minX = chX2;
                    }
                    if(chY2 < minY) {
                        minY = chY2;
                    }
                    int X2 = chX2 + chW2 - 1;
                    if(X2 > maxX) {
                        maxX = X2;
                    }
                    int Y2 = chY2 + chH - 1;
                    if(Y2 > maxY) {
                        maxY = Y2;
                    }
                }
            }

            if(resetScroll && foundOne) {
                scrollRectToVisible(new Rectangle(minX, minY, maxX - minX, maxY - minY));
                resetScroll = false;
            }
        }

        private void drawElements(Graphics g, List<GraphicalElement> pageData) {
            for(GraphicalElement gElement : pageData) {
                Color hlFgColor = null;
                if(highlights != null) {
                    for(Range range : highlights.keySet()) {
                        if(gElement.index >= range.getStart() && gElement.index < range.getEndNonIncl()) {
                            hlFgColor = highlights.get(range).getValue1();
                        }
                    }
                }

                boolean sel = selected.contains(gElement);

                String ch = gElement.element.getText();
                int chX = X_MARGIN + gElement.getX();
                int chY = Y_MARGIN + gElement.getY();
                int chYbl = chY + gElement.getH();

                if(showRegions) {
                    if(sel) {
                        g.setColor(Lay.clr("255,205,205"));
                        g.fillRect(chX, chY, gElement.getW(), gElement.getH());
                    }
                    g.setColor(Color.red);
                    g.drawRect(chX, chY, gElement.getW() - 1, gElement.getH() - 1);
                }

                Color chColor;
                if(sel) {
                    chColor = Color.YELLOW;
                } else if(gElement.discarded) {
                    chColor = Color.LIGHT_GRAY;
                } else if(hlFgColor != null) {
                    chColor = hlFgColor;
                } else {
                    chColor = Color.black;
                }

                g.setColor(chColor);
                g.setFont(
                    new Font(
                        gElement.getFont(),
                        (gElement.isBold() || hlFgColor != null ? Font.BOLD : 0) +
                        (gElement.isItalic() ? Font.ITALIC : 0),
                        (int) gElement.element.getFontSize()
                    )
                );

                g.drawString(ch, chX, chYbl);
            }
        }

        private void drawPageRegion(Graphics g, Rectangle pageRect) {
            g.setColor(Color.white);
            g.fillRect(X_MARGIN, Y_MARGIN, pageRect.width, pageRect.height);
            g.setColor(Color.black);
            g.drawRect(X_MARGIN, Y_MARGIN, pageRect.width - 1, pageRect.height - 1);
        }

        private void drawRulers(Graphics g, Rectangle pageRect) {
            int width = pageRect.width;
            int height = pageRect.height;

            g.setColor(ColorLib.DEFAULT);
            g.fillRect(0, 0, X_MARGIN + width, Y_MARGIN);
            g.fillRect(0, 0, X_MARGIN, Y_MARGIN + height);

            g.setColor(Color.blue);
            for(int x = 0; x < width; x++) {
                if(x % 100 == 0) {
                    g.drawLine(X_MARGIN + x, 0, X_MARGIN + x, Y_MARGIN - 1);
                } else if(x % 50 == 0) {
                    g.drawLine(X_MARGIN + x, 0, X_MARGIN + x, (int)(Y_MARGIN * 0.75) - 1);
                } else if(x % 10 == 0) {
                    g.drawLine(X_MARGIN + x, 0, X_MARGIN + x, (Y_MARGIN / 2) - 1);
                }
            }
            for(int y = 0; y < height; y++) {
                if(y % 100 == 0) {
                    g.drawLine(0, Y_MARGIN + y, X_MARGIN - 1, Y_MARGIN + y);
                } else if(y % 50 == 0) {
                    g.drawLine(0, Y_MARGIN + y, (int) (X_MARGIN * 0.75) - 1, Y_MARGIN + y);
                } else if(y % 10 == 0) {
                    g.drawLine(0, Y_MARGIN + y, (X_MARGIN / 2) - 1, Y_MARGIN + y);
                }
            }

            if(mousePos != null) {
                boolean validX = mousePos.x >= X_MARGIN && mousePos.x < X_MARGIN + width;
                boolean validY = mousePos.y >= Y_MARGIN && mousePos.y < Y_MARGIN + height;

                g.setColor(Color.red);
                if(validX) {
                    g.drawLine(mousePos.x, 0, mousePos.x, Y_MARGIN - 1);
                }
                if(validY) {
                    g.drawLine(0, mousePos.y, X_MARGIN - 1, mousePos.y);
                }

                if(validX && validY) {
                    g.drawLine(mousePos.x, Y_MARGIN, mousePos.x, mousePos.y);
                    g.drawLine(X_MARGIN, mousePos.y, mousePos.x, mousePos.y);
                }
            }
        }


        /////////////////
        // INNER CLASS //
        /////////////////

        private class GraphicalElement {
            PdfElement element;
            int index;
            String font;
            boolean bold;
            boolean italic;
            boolean discarded = false;

            public GraphicalElement(PdfElement element, int index) {
                this.element = element;
                this.index = index;

                String source = element.getFontName().toLowerCase();
                if(source.contains("times new roman") || source.contains("timesnewroman")) {
                    font = "Times New Roman";
                } else if(source.contains("arial")) {
                    font = "Arial";
                } else if(source.contains("helvetica")) {
                    font = "Helvetica";
                } else if(source.contains("garamond")) {
                    font = "Garamond";
                } else {
                    font = "Arial";
                }
                if(source.contains("bold")) {
                    bold = true;
                }
                if(source.contains("italic")) {
                    italic = true;
                }
            }

            public String getFont() {
                return font;
            }
            public boolean isBold() {
                return bold;
            }
            public boolean isItalic() {
                return italic;
            }
            public int getX() {
                return (int) element.getX();
            }
            public int getY() {
                return (int) element.getY();
            }
            public int getW() {
                return (int) element.getW();
            }
            public int getH() {
                return (int) element.getH();
            }
        }


        ////////////////////////////
        // DRAG (LASSO) SELECTION //
        ////////////////////////////

        @Override
        public void setMouseDragSelection(boolean enabled) {
            helper.setMouseDragSelection(enabled);
        }
        @Override
        public boolean hasSelection(MouseEvent e) {
            return !findElems(e.getPoint().x, e.getPoint().y).isEmpty();
        }
        @Override
        public void clearSelection() {
            selected.clear();
            lblSel.setText("# Selected: " + selected.size());
        }
        @Override
        public RectangleIterator getRectangleIterator(int x, int y) {
            return new PdfElementIterator(x, y);
        }
        @Override
        public void updateCleanUp() {
        }

        private void updateMousePoint(MouseEvent e) {
            if(result != null) {
                mousePos = e.getPoint();
                updateMouseLabel();
                repaint();
            }
        }

        private void updateMouseLabel() {
            if(curPage != -1) {
                Rectangle pageRect = getCurRect();
                if(mousePos.x >= X_MARGIN && mousePos.x < X_MARGIN + pageRect.width &&
                        mousePos.y >= Y_MARGIN && mousePos.y < Y_MARGIN + pageRect.height) {
                    lblMouse.setText(
                        "Mouse: (" + (mousePos.x - X_MARGIN) + ", " +
                        (mousePos.y - Y_MARGIN) + ")"
                    );
                } else {
                    lblMouse.setText("");
                }
            } else {
                lblMouse.setText("");
            }
        }

        private class PdfElementIterator extends RectangleIterator {
            private int idx;
            private int cur;
            List<GraphicalElement> elems;

            public PdfElementIterator(int x, int y) {
                cur = idx = 0;  // For clarity

                if(curPage != -1 && data.containsKey(curPage)) {
                    elems = data.get(curPage);
                } else {
                    elems = new ArrayList<>();
                }
            }
            @Override
            public boolean hasNext() {
                return idx < elems.size();
            }
            @Override
            public Rectangle next() {
                cur = idx;
                GraphicalElement gElement = elems.get(idx++);
                Rectangle rect = new Rectangle(
                    X_MARGIN + gElement.getX(),
                    Y_MARGIN + gElement.getY(),
                    gElement.getW(), gElement.getH());
                return rect;
            }
            @Override
            public void addSelection() {
                GraphicalElement gElement = elems.get(cur);
                selected.add(gElement);
                lblSel.setText("# Selected: " + selected.size());
            }
            @Override
            public void removeSelection() {
                selected.remove(elems.get(cur));
                lblSel.setText("# Selected: " + selected.size());
            }
        }
    }

    public void setNodeHighlights(Map<Range, Pair<Color, Color>> hl) {
        highlights = hl;
        resetScroll = true;
        repaint();
    }
}
