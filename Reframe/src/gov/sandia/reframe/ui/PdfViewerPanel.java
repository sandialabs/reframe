package gov.sandia.reframe.ui;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Viewer;

import gov.sandia.cortext.fileparsing.pdf.ExtractionResult;
import gov.sandia.cortext.fileparsing.pdf.PdfElement;
import gov.sandia.cortext.fileparsing.pdf.PdfElementExtractor;
import gov.sandia.cortext.fileparsing.pdf.PdfExtractionParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseResult;
import gov.sandia.cortext.fileparsing.pdf.PdfParser;
import gov.sandia.cortext.fileparsing.pdf.Rule;
import gov.sandia.reframe.ParseRecord;
import gov.sandia.reframe.Pdf;
import gov.sandia.reframe.ui.elements.PdfElementsPanel;
import gov.sandia.reframe.ui.hierarchy.PdfHierarchyPanel;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import gov.sandia.reframe.ui.params.PdfParametersPanel;
import replete.cli.ConsoleUtil;
import replete.collections.Pair;
import replete.hash.Md5Util;
import replete.io.FileUtil;
import replete.numbers.PositiveNumberRangeList;
import replete.progress.FractionProgressMessage;
import replete.text.StringUtil;
import replete.ttc.TransparentTaskStopException;
import replete.ui.GuiUtil;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.tabbed.RTabbedPane;
import replete.ui.text.RTextArea;
import replete.ui.windows.Dialogs;
import replete.ui.windows.notifications.NotificationClickAction;
import replete.ui.windows.notifications.msg.NotificationCommon;
import replete.ui.windows.notifications.msg.NotificationError;
import replete.ui.windows.notifications.msg.NotificationTask;
import replete.ui.worker.RWorker;
import replete.util.User;

public class PdfViewerPanel extends RPanel {

    ////////////
    // FIELDS //
    ////////////

    // Core

    public static final String BG = "220";

    private Pdf pdf;

    // UI

    private UIController uiController;
    private RTextArea txtIntermediate = new RTextArea();
    private JLabel lblDuration;
    private RTabbedPane tabs;
    private Viewer viewer;
    private JSplitPane spl;
    private PdfElementsPanel pnlElems;
    private PdfParametersPanel pnlParams;
    private PdfHierarchyPanel pnlHier;
    private int libDivLoc;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PdfViewerPanel(final UIController uiController, final Pdf pdf) {
        this.uiController = uiController;
        this.pdf = pdf;

        JPanel pnl;
        if(FileUtil.isReadableFile(pdf.getFile())) {
            pnl = Lay.p();
            viewer = new Viewer(pnl, null);
            ConsoleUtil.disable();        // Suppress dumb JPedal "No menu item set" messages
            viewer.setupViewer();
            ConsoleUtil.restore();
            Object[] input = new Object[]{pdf.getFile().toString()};
            viewer.executeCommand(Commands.OPENFILE, input);
        } else {
            pnl = Lay.GBL(
                Lay.lb("Could not find document '" + pdf.getFile().getAbsolutePath() + "'.", "fg=red")
            );
        }

        Lay.BLtg(this,
            spl = Lay.SPL(
                Lay.p(
                    tabs = Lay.TBL(
                        "Document",
                            CommonConcepts.FILE,
                            pnl,
                        "Elements",
                            ReframeImageModel.ELEMS,
                            pnlElems = new PdfElementsPanel(pdf),
                        "Intermediate",
                            CommonConcepts.DISCONNECT,
                            Lay.BL(
                                "N", lblDuration = Lay.lb("Last Parse Duration: N/A"),
                                "C", Lay.sp(txtIntermediate), "eb=5,bg=" + BG
                            ),
                        "Hierarchy",
                            ReframeImageModel.HIER,
                            pnlHier = new PdfHierarchyPanel(pdf),
                        "borders"
                    ),
                    "bg=020087"
                ),
                pnlParams = new PdfParametersPanel(uiController, pdf)
            )
        );

        pnlParams.addExpandListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(pnlParams.isExpanded()) {
                    spl.setDividerLocation(libDivLoc);
                } else {
                    libDivLoc = spl.getDividerLocation();
                    int w = spl.getDividerSize();
                    spl.setDividerLocation(spl.getWidth() - 27 - w);
                }
            }
        });

        pnlElems.setParametersPanel(pnlParams);

        pnlElems.addCreateRuleListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                List<PdfElement> selectedElements = pnlElems.getSelectedElements();
                Rule derivedRule = deriveRule(selectedElements);
                pnlParams.addRuleFromExternal(derivedRule);
            }
        });

        txtIntermediate.setEditable(false);

        if(!FileUtil.isReadableFile(pdf.getFile())) {
            pnlParams.setProcessButtonEnabled(false);
        }

        pnlParams.addProcessListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final ReframeFrame parent =
                    (ReframeFrame) GuiUtil.fra(PdfViewerPanel.this);
                parent.waitOn();
                pnlParams.setProcessButtonEnabled(false);
                PdfParserWorker worker = new PdfParserWorker();
                NotificationTask prog = new NotificationTask()
                    .setTitle("Processing " + pdf.getFile().getName())
                    .setAction(worker)
                    .setUseWaitCursor(true)
                    .setAddError(true)
                    .setClickAction(new NotificationClickAction() {
                        @Override
                        public void clicked(NotificationCommon notif) {
                            uiController.setSelectedTab(pdf);
                        }
                    });
                parent.getNotificationModel().getTasks().add(prog);
                worker.execute();
            }
        });
        pnlParams.addExtractListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final ReframeFrame parent =
                        (ReframeFrame) GuiUtil.fra(PdfViewerPanel.this);
                parent.waitOn();

                pnlParams.setExtractButtonEnabled(false);
                PdfExtractWorker worker = new PdfExtractWorker();
                NotificationTask prog = new NotificationTask()
                    .setTitle("Fetching elements from " + pdf.getFile().getName())
                    .setAction(worker)
                    .setUseWaitCursor(true)
                    .setAddError(true);
                parent.getNotificationModel().getTasks().add(prog);
                worker.execute();
            }
        });

        updateFromPdf();
    }

    public void toggleView() {
        if(spl.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            spl.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        } else {
            spl.setOrientation(JSplitPane.VERTICAL_SPLIT);
        }
        spl.setDividerLocation(0.5);
    }

    protected Rule deriveRule(List<PdfElement> selectedElements) {
        boolean allBold = true;
        boolean allItalic = true;
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minFs = Float.MAX_VALUE;
        float maxFs = Float.MIN_VALUE;
        int minP = Integer.MAX_VALUE;
        int maxP = Integer.MIN_VALUE;
        boolean fontNameSet = false;
        String fontName = null;
        String text = "";

        for(PdfElement elem : selectedElements) {
            if(!elem.isBold()) {
                allBold = false;
            }
            if(!elem.isItalic()) {
                allItalic = false;
            }
            if(elem.getPage() < minP) {
                minP = elem.getPage();
            }
            if(elem.getPage() > maxP) {
                maxP = elem.getPage();
            }
            if(elem.getX() < minX) {
                minX = elem.getX();
            }
            if(elem.getX() > maxX) {
                maxX = elem.getX();
            }
            if(elem.getFontSize() < minFs) {
                minFs = elem.getFontSize();
            }
            if(elem.getFontSize() > maxFs) {
                maxFs = elem.getFontSize();
            }
            if(!fontNameSet || (fontName != null && elem.getFontName().equals(fontName))) {
                fontName = elem.getFontName();
                fontNameSet = true;
            } else {
                fontName = null;
            }
            text += elem.getText();
        }

        String pages = minP != maxP ? minP + "-" + maxP : "" + minP;
        String pattern;
        try {
            pattern = findPattern(text);
        } catch(Exception e) {
            pattern = "<<ERROR: " + e.getMessage() + ">>";
        }

        Rule rule = new Rule()
            .setFontName(fontName)
            .setBold(allBold ? true : null)
            .setItalic(allItalic ? true : null)
            .setMinX((float) Math.floor(minX))
            .setMaxX((float) Math.ceil(maxX))
            .setMinFontSize(minFs)
            .setMaxFontSize(maxFs)
            .setPages(PositiveNumberRangeList.parse(pages))
            .setPattern(pattern)
        ;

        return rule;
    }

    private String findPattern(String text) {
        int prevCat = 0;   // 1 = digit, 2 = alpha, 3 = other, 4 = done
        String p = "";
        String special = "*?+.[]{}()\\";
        char prevCh = 0;
        for(int i = 0; i <= text.length(); i++) {

            char ch;
            int cat;
            if(i < text.length()) {
                ch = text.charAt(i);
                if(Character.isDigit(ch)) {
                    cat = 1;
                } else if(Character.isAlphabetic(ch)) {
                    cat = 2;
                } else {
                    cat = 3;
                }
            } else {
                cat = 4;
                ch = 0;
            }

            if(prevCat != cat || prevCat == cat && cat == 3) {
                if(prevCat == 1) {
                    p += "\\d+";
                } else if(prevCat == 2) {
                    p += "[A-Za-z]+";
                } else if(prevCat == 3) {
                    if(special.indexOf(prevCh) != -1) {
                        p += "\\";
                    }
                    p += prevCh;
                }
            }

            prevCat = cat;
            prevCh = ch;
        }
        return p;
    }

    public void init() {
//        spl.setDividerLocation(0.8);
        spl.setDividerLocation(650);
    }

    public Pdf getPdf() {
        return pdf;
    }

    public void cleanUp() {
        if(viewer != null) {
            viewer.dispose();
        }
    }

    public void saveParameters() {
        PdfExtractionParameters extractionParams = pnlParams.createExtractionParameters();
        pdf.setExtractionParams(extractionParams);
    }

    private void updateFromPdf() {
        if(pdf.getResult() != null && pdf.getResult().getSegments() != null) {
            populateIntermediate();
        } else {
            txtIntermediate.clear();
        }
        pnlHier.updateFromPdf();
    }

    private void populateIntermediate() {
        String dur = pdf.getResult().getLastParseDuration() == 0 ? "Unknown" :
            pdf.getResult().getLastParseDuration() + " ms";
        lblDuration.setText("Last Parse Duration: " + dur);
        String str = StringUtil.join(pdf.getResult().getSegments(), "\n");
        txtIntermediate.setText(str);
        txtIntermediate.setCaretPosition(0);
    }

    /////////////////
    // INNER CLASS //
    /////////////////

    private class PdfExtractWorker extends RWorker<Void, ExtractionResult> {
        @Override
        protected ExtractionResult background(Void gathered) throws Exception {
            PdfExtractionParameters params = pnlParams.createExtractionParameters();
            PDDocument pd = PDDocument.load(pdf.getFile());
            PdfElementExtractor extractor = new PdfElementExtractor(
                params.getMinXAllowed(), params.getMaxXAllowed(),
                params.getMinYAllowed(), params.getMaxYAllowed(),
                params.getMaxPage());
            extractor.getText(pd);
            return new ExtractionResult(extractor, pd);
        }
        @Override
        protected void complete() {
            ReframeFrame parent = uiController.getParent();
            try {
                ExtractionResult result = getResult();
                pdf.setExtractionResult(result);

                List<PdfElement> elements = result.getFilteredElements();
                pnlElems.updateElementsFromPdf(elements);
                pnlElems.updateUIAfterInitialize(elements);
            } catch(Exception e) {
                NotificationError error = new NotificationError()
                    .setError(e)
                    .setTitle("An error has occurred fetching the elements.");
                parent.getNotificationModel().getErrors().add(error);
            }
            pnlParams.setExtractButtonEnabled(true);
            parent.waitOff();
            pnlHier.updateGraphicalPanel();
        }
    }

    private class PdfParserWorker extends RWorker<Void, PdfParseResult> {
        public PdfParserWorker() {
            super(true, true);
        }

        @Override
        protected PdfParseResult background(Void gathered) throws Exception {
            PdfExtractionParameters eParams = pnlParams.createExtractionParameters();
            pdf.setExtractionParams(eParams);
            PdfParseParameters params = pnlParams.createParseParameters();
            pdf.setParams(params);

            final PdfParser parser = new PdfParser();
            parser.addProgressListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    Pair<Integer, Integer> progress = parser.getCurrentProgess();
                    int percentComplete = (int)((double)progress.getValue1() / progress.getValue2() * 100);
                    FractionProgressMessage msg = new FractionProgressMessage(
                        "Percent Complete", percentComplete + "%",
                        progress.getValue1(), progress.getValue2());
                    publish(msg);
                }
            });
            uiController.setDirty(true);
            return parser.parse(pdf.getFile(), eParams, params, ttContext);
        }

        @Override
        protected void complete() {
            tabs.setSelectedIndex(3);
            ReframeFrame parent = uiController.getParent();

            ParseRecord record = new ParseRecord()
                .setUser(User.getName())
                .setStarted(getBackgroundStarted())
                .setEnded(getBackgroundEnded())
                .setPdfMd5Hash(Md5Util.getMd5(pdf.getFile()));

            try {
                PdfParseResult result = getResult();
                pdf.setResult(result);

                updateFromPdf();
                pdf.clearPrevResult();

                int[] nodeCounts = result.getHierarchy().calculateNodeCounts();
                record
                    .setNodeCount(nodeCounts[0])
                    .setNodeCountNonBlank(nodeCounts[1]);

            } catch(TransparentTaskStopException e) {
                Dialogs.showMessage(parent, "Processing has been stopped for document\n    " + pdf,
                    "Processing Stopped");

            } catch(Exception e) {
                NotificationError error = new NotificationError()
                    .setError(e)
                    .setTitle("An error has occurred processing the document.")
                ;
                parent.getNotificationModel().getErrors().add(error);
                record.setError(e);
            }

            pdf.getParseHistory().add(record);
            pnlParams.setProcessButtonEnabled(true);
            parent.waitOff();
        }
    }
}
