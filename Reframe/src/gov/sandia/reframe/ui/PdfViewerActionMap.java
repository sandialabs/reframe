package gov.sandia.reframe.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.uiaction.MenuBarActionDescriptor;
import replete.ui.uiaction.UIAction;
import replete.ui.uiaction.UIActionListener;
import replete.ui.uiaction.UIActionMap;

public class PdfViewerActionMap extends UIActionMap {
    public PdfViewerActionMap(final UIController uiController) {


        //////////
        // FILE //
        //////////

        createAction("file")
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&File"));

        UIActionListener listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.newLibrary();
            }
        };
        createAction("new", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&New Library")
                    .setPath("file")
                    .setIcon(CommonConcepts.FAVORITE)
                    .setAccCtrl(true)
                    .setAccKey(KeyEvent.VK_N));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.openLibrary();
            }
        };
        createAction("open", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&Open Library...")
                    .setPath("file")
                    .setIcon(CommonConcepts.OPEN)
                    .setAccCtrl(true)
                    .setAccKey(KeyEvent.VK_O));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.saveLibrary();
            }
        };
        createAction("save", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&Save Library")
                    .setPath("file")
                    .setIcon(CommonConcepts.SAVE)
                    .setAccCtrl(true)
                    .setAccKey(KeyEvent.VK_S));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.saveLibraryAs();
            }
        };
        createAction("saveAs", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Save Library &As...")
                    .setPath("file")
                    .setIcon(CommonConcepts.SAVE_AS));

        createAction()
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setSeparator(true));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.exit();
            }
        };
        createAction("exit", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("E&xit")
                    .setPath("file")
                    .setIcon(CommonConcepts.EXIT));


        /////////////
        // LIBRARY //
        /////////////

        createAction("library")
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&Library"));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("addpdf", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Add Documents...")
                    .setPath("library")
                    .setIcon(CommonConcepts.ADD));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("removepdf", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Remove Documents")
                    .setPath("library")
                    .setIcon(CommonConcepts.REMOVE));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("setpdfpath", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Set Document File Paths...")
                    .setPath("library")
                    .setIcon(CommonConcepts.EDIT));

        createAction()
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setSeparator(true));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("showpdf", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Show Documents")
                    .setPath("library")
                    .setIcon(CommonConcepts.SEARCH));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("showsys", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Show With System Editor")
                    .setPath("library")
                    .setIcon(ReframeImageModel.READER));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("showexp", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Show In Windows Explorer")
                    .setPath("library")
                    .setIcon(ReframeImageModel.EXPLORER));

        createAction()
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setSeparator(true));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.notImpl();
            }
        };
        createAction("exportxml", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Export Tarjetas XML...")
                    .setPath("library")
                    .setIcon(CommonConcepts.XML));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.exportPdfs();
            }
        };
        createAction("exportpdf", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Export Documents...")
                    .setPath("library")
                    .setIcon(CommonConcepts.EXPORT));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.importPdfs();
            }
        };
        createAction("importpdf", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("Import Documents...")
                    .setPath("library")
                    .setIcon(CommonConcepts.IMPORT));


        //////////
        // VIEW //
        //////////

        createAction("view")
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&View"));

        listener = new UIActionListener() {
            public void actionPerformed(ActionEvent e, UIAction action) {
                uiController.toggleView();
            }
        };
        createAction("toggleview", listener)
            .addDescriptor(
                new MenuBarActionDescriptor()
                    .setText("&Toggle Parameters View")
                    .setPath("view")
                    .setIcon(ReframeImageModel.TOGGLE_PARAMS)
                    .setAccCtrl(true)
                    .setAccKey(KeyEvent.VK_T));
   }
}
