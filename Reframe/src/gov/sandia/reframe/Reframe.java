package gov.sandia.reframe;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.cortext.fileparsing.pdf.Hierarchy;
import gov.sandia.cortext.fileparsing.pdf.PdfParseParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseResult;
import gov.sandia.cortext.fileparsing.pdf.Rule;
import gov.sandia.cortext.fileparsing.pdf.RuleSet;
import gov.sandia.cortext.fileparsing.pdf.RuleSetHierarchy;
import gov.sandia.cortext.fileparsing.pdf.RuleSetNode;
import gov.sandia.reframe.ui.ReframeFrame;
import gov.sandia.reframe.ui.UIController;
import replete.ui.windows.Dialogs;
import replete.xstream.XStreamWrapper;

public class Reframe {
    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Dialogs.showDetails(null, "Error", e));

        addAliases(
            Pdf.class,
            PdfParseParameters.class,
            PdfParseResult.class,
            RuleSetHierarchy.class,
            RuleSetNode.class,
            RuleSet.class,
            Rule.class,
            Hierarchy.class,
            HNode.class
            // TODO: Add SerializationResult
            // TODO: Add NumberRange
            // TODO: Add ParseRecord
        );

//        if(AppState.appFile.exists()) {
//            try {
//                AppState.read();
//                for(Pdf pdf : AppState.getState().getPdfs()) {
//                    if(pdf.getResult() != null && pdf.getResult().getHierarchy() != null && pdf.getResult().getHierarchy().getRoot() != null) {
//                        pdf.getResult().getHierarchy().getRoot().fixRedacted();
//                    }
//                }
//            } catch(Exception e) {
//                Dialogs.showDetails((Component) null,
//                    "Could not load state from file.", "Possible Error", e);
//            }
//        }

        final UIController uiController = new UIController();
        final ReframeFrame win = new ReframeFrame(uiController);
        win.addAttemptToCloseListener(e -> {
            if(!uiController.checkSave()) {
                e.cancelClose();
            }
        });
        win.addClosingListener(e -> win.cleanUp());
        win.setVisible(true);
    }

    private static void addAliases(Class... classes) {
        for(Class clazz : classes) {
            XStreamWrapper.addAlias(clazz.getSimpleName(), clazz);
        }
    }
}
