package gov.sandia.reframe.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gov.sandia.reframe.AppState;
import gov.sandia.reframe.Pdf;
import gov.sandia.reframe.ui.library.PdfLibraryPanel;
import replete.ui.GuiUtil;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.tabbed.RTabbedPane;
import replete.ui.tabbed.TabCloseEvent;
import replete.ui.tabbed.TabCloseListener;
import replete.ui.uiaction.UIActionMap;
import replete.ui.uiaction.UIActionMenuBar;
import replete.ui.windows.Dialogs;
import replete.ui.windows.notifications.NotificationFrame;
import replete.ui.windows.notifications.msg.NotificationError;
import replete.ui.windows.notifications.msg.NotificationTask;
import replete.ui.worker.RWorker;

public class ReframeFrame extends NotificationFrame {


    ////////////
    // FIELDS //
    ////////////

    // UI

    private UIController uiController;
    private PdfLibraryPanel pnlLibrary;
    private RTabbedPane tabs;
    private int libDivLoc;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public ReframeFrame(UIController uiController) {
        super("Reframe PDF Viewer & Parser");
        this.uiController = uiController;
        setIcon(CommonConcepts.PDF);
        uiController.setParent(this);
        setShowStatusBar(true);

        GuiUtil.enableTabsHighlighted();

        final JSplitPane spl;
        Lay.BLtg(this,
            "C", spl = Lay.SPL(
                pnlLibrary = new PdfLibraryPanel(uiController),
                tabs = Lay.TBL("dc"),
                "bg=100"
            ),
            "size=[1200,900],center"
        );

        uiController.addRefreshListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                tabs.removeAll();
            }
        });

        pnlLibrary.addExpandListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(pnlLibrary.isExpanded()) {
                    spl.setDividerLocation(libDivLoc);
                } else {
                    libDivLoc = spl.getDividerLocation();
                    spl.setDividerLocation(27);
                }
            }
        });
        pnlLibrary.addOpenListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                openSelectedPdfTabs();
            }
        });
        pnlLibrary.addCloseListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                for(Pdf pdf : pnlLibrary.getSelected()) {
                    int idx = tabs.indexOfTabByKey(pdf);
                    // If the tab was never opened then there is no reason to delete it.
                    if(idx != -1) {
                        tabs.remove(idx);
                    }
                }
            }
        });

        tabs.addTabCloseListener(new TabCloseListener() {
            public void stateChanged(TabCloseEvent e) {
                PdfViewerPanel pnlViewer = (PdfViewerPanel) e.getComponent();
                pnlViewer.saveParameters();
                pnlViewer.cleanUp();
            }
        });
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int idx = tabs.getSelectedIndex();
                if(idx != -1) {
                    Pdf pdf = (Pdf) tabs.getKeyAt(idx);
                    pnlLibrary.setSelectedDocument(pdf);
                }
            }
        });

        UIActionMap actionMap = new PdfViewerActionMap(uiController);
        UIActionMenuBar bar = new UIActionMenuBar(actionMap);
        setJMenuBar(bar);
    }

    public void toggleView() {
        for(int t = 0; t < tabs.getTabCount(); t++) {
            PdfViewerPanel pnlViewer = (PdfViewerPanel) tabs.getComponentAt(t);
            pnlViewer.toggleView();
        }
    }

    public void cleanUp() {
        for(int t = 0; t < tabs.getTabCount(); t++) {
            PdfViewerPanel pnlViewer = (PdfViewerPanel) tabs.getComponentAt(t);
            pnlViewer.cleanUp();
        }
    }

    public void saveParameters() {
        for(int t = 0; t < tabs.getTabCount(); t++) {
            PdfViewerPanel pnlViewer = (PdfViewerPanel) tabs.getComponentAt(t);
            pnlViewer.saveParameters();
        }
    }

    private void openSelectedPdfTabs() {
        RWorker<Void, List<PdfViewerPanel>> worker =
                        new RWorker<Void, List<PdfViewerPanel>>() {

            private int select = -1;   // Another "output" of the background process.

            @Override
            protected List<PdfViewerPanel> background(Void gathered) throws Exception {
                List<PdfViewerPanel> panels = new ArrayList<>();
                for(Pdf pdf : pnlLibrary.getSelected()) {
                    if(tabs.indexOfTabByKey(pdf) == -1) {
                        panels.add(new PdfViewerPanel(uiController, pdf));
                    } else {
                        select = tabs.indexOfTabByKey(pdf);
                    }
                }
                return panels;
            }

            @Override
            protected void complete() {
                try {
                    List<PdfViewerPanel> pnls = get();
                    Object lastKey = null;
                    for(PdfViewerPanel pnl : pnls) {
                        Pdf pdf = pnl.getPdf();
                        String path = pdf.getFile().getAbsolutePath();
                        if(tabs.indexOfTabByKey(pdf) == -1) {
                            tabs.addTab(
                                pdf.getTitle(), CommonConcepts.PDF,
                                    pnl, path, pdf);
                            lastKey = pdf;
                            pnl.init();
                        } else {
                            System.err.println("Panel opening: Invalid state.  PDF Object '" +
                                path + "' not a valid key in tabbed pane.");
                        }
                    }
                    if(lastKey == null && select != -1) {
                        tabs.setSelectedIndex(select);
                    } else {
                        tabs.setSelectedIndex(tabs.indexOfTabByKey(lastKey));
                    }
                } catch(Exception e) {
                    NotificationError error = new NotificationError()
                        .setError(e)
                        .setTitle("An error has occurred opening the document.");
                    getNotificationModel().getErrors().add(error);
                }
            }
        };

        NotificationTask prog = new NotificationTask()
            .setTitle("Opening " + (pnlLibrary.getSelected().size() == 1 ? "Document" : "Documents"))
            .setAction(worker)
            .setUseWaitCursor(true)
            .setAddError(true);
        getNotificationModel().getTasks().add(prog);
        worker.execute();
    }

    public void updateTitle() {
        setTitle("Reframe PDF Viewer & Parser - " +
            (AppState.getState().getLibraryFile() == null ? "[New]" :
                AppState.getState().getLibraryFile().toString()) +
                (AppState.getState().isLibraryDirty() ? "*" : ""));
    }

    public List<Pdf> getSelected() {
        return pnlLibrary.getSelected();
    }

    public void setSelectedTab(Pdf pdf) {
        int index = tabs.indexOfTabByKey(pdf);
        if(index != -1) {
            tabs.setSelectedIndex(index);
        } else {
            Dialogs.showMessage(this, "That document tab is no longer open.", "Document Parsing");
        }
    }
}
