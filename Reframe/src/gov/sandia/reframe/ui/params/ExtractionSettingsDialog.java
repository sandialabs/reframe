package gov.sandia.reframe.ui.params;

import java.awt.Color;

import javax.swing.JFrame;

import gov.sandia.cortext.fileparsing.pdf.PdfExtractionParameters;
import replete.ui.button.RButton;
import replete.ui.form.RFormPanel;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.text.RTextField;
import replete.ui.windows.escape.EscapeDialog;

public class ExtractionSettingsDialog extends EscapeDialog {


    ////////////
    // FIELDS //
    ////////////

    public static final int OK = 1;
    public static final int CANCEL = 2;

    private int result = CANCEL;
    private ExtractionPanel pnlExtract;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public ExtractionSettingsDialog(JFrame parent, PdfExtractionParameters eParams) {
        super(parent, "PDF Extraction Settings", true);
        setIcon(CommonConcepts.EDIT);

        RButton btnOk, btnCancel;
        Lay.BLtg(this,
            "C", pnlExtract = new ExtractionPanel(eParams),
            "S", Lay.FL("R",
                btnOk = Lay.btn("&OK", CommonConcepts.ACCEPT),
                btnCancel = Lay.btn("&Cancel", CommonConcepts.CANCEL),
                "bg=100,mb=[1t,black]"
            ),
            "size=[250,365],center"
        );

        setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            if(getExtractionParameters() == null) {
                // Dialogs.showWarning(this, "There are validation errors with this rule.", "Rule Validation");
                return;
            }
            result = OK;
            close();
        });
        btnCancel.addActionListener(e -> close());
    }


    ///////////////
    // ACCESSORS //
    ///////////////

    public int getResult() {
        return result;
    }

    public PdfExtractionParameters getExtractionParameters() {
        return pnlExtract.getExtractionParameters();
    }

    private class ExtractionPanel extends RPanel {


        ////////////
        // FIELDS //
        ////////////

        // Constants

        private final Color CLR_INVALID = Lay.clr("255,205,205");

        // UI

        private RTextField txtMinXAllowed;
        private RTextField txtMaxXAllowed;
        private RTextField txtMinYAllowed;
        private RTextField txtMaxYAllowed;
        private RTextField txtMinPage;
        private RTextField txtMaxPage;
        private ExtractionFormPanel extractionForm;

        /////////////////
        // CONSTRUCTOR //
        /////////////////

        public ExtractionPanel(PdfExtractionParameters eParams) {
            Lay.BLtg(this,
                "C", extractionForm = new ExtractionFormPanel(eParams)
            );
        }

        public PdfExtractionParameters getExtractionParameters() {
            return extractionForm.getExtractionParameters();
        }


        /////////////////
        // INNER CLASS //
        /////////////////

        private  Color CLR_INVALIDx = Lay.clr("255,205,205");
        private class ExtractionFormPanel extends RFormPanel {

            public PdfExtractionParameters getExtractionParameters() {
                PdfExtractionParameters eParams = new PdfExtractionParameters()
                    .setMinXAllowed(txtMinXAllowed.getFloat() == null ? Float.MIN_VALUE : txtMinXAllowed.getFloat())
                    .setMaxXAllowed(txtMaxXAllowed.getFloat() == null ? Float.MAX_VALUE : txtMaxXAllowed.getFloat())
                    .setMinYAllowed(txtMinYAllowed.getFloat() == null ? Float.MIN_VALUE : txtMinYAllowed.getFloat())
                    .setMaxYAllowed(txtMaxYAllowed.getFloat() == null ? Float.MAX_VALUE : txtMaxYAllowed.getFloat())
                    .setMinPage(txtMinPage.getInteger() == null ? Integer.MIN_VALUE : txtMinPage.getInteger())
                    .setMaxPage(txtMaxPage.getFloat() == null ? Integer.MAX_VALUE : txtMaxPage.getInteger());
                return eParams;
            }

            public ExtractionFormPanel(PdfExtractionParameters eParams) {
                super(100);
                init();

                if (eParams != null) {
                    txtMinXAllowed.setText(eParams.getMinXAllowed() == Float.MIN_VALUE || eParams.getMinXAllowed() == 0.0 ? "" : eParams.getMinXAllowed());
                    txtMaxXAllowed.setText(eParams.getMaxXAllowed() == Float.MAX_VALUE || eParams.getMaxXAllowed() == 0.0 ? "" : eParams.getMaxXAllowed());
                    txtMinYAllowed.setText(eParams.getMinYAllowed() == Float.MIN_VALUE || eParams.getMinYAllowed() == 0.0 ? "" : eParams.getMinYAllowed());
                    txtMaxYAllowed.setText(eParams.getMaxYAllowed() == Float.MAX_VALUE || eParams.getMaxXAllowed() == 0.0 ? "" : eParams.getMaxYAllowed());
                    txtMinPage.setText(eParams.getMinPage() == Integer.MIN_VALUE || eParams.getMinPage() == 0.0 ? "" : eParams.getMinPage());
                    txtMaxPage.setText(eParams.getMaxPage() == Integer.MAX_VALUE || eParams.getMaxPage() == 0.0 ? "" : eParams.getMaxPage());
                }
            }

            @Override
            protected void addFields() {
                addField("Main", "Min X",     Lay.BL("W", "nogap", Lay.lb("MnX: "),  txtMinXAllowed = Lay.tx("", "selectall", 4)), 40, false);
                addField("Main", "Max X",     Lay.BL("W", "nogap", Lay.lb("MxX: "),  txtMaxXAllowed = Lay.tx("", "selectall", 4)), 40, false);
                addField("Main", "Min Y",     Lay.BL("W", "nogap", Lay.lb("MnY: "),  txtMinYAllowed = Lay.tx("", "selectall", 4)), 40, false);
                addField("Main", "Max Y",     Lay.BL("W", "nogap", Lay.lb("MxY: "),  txtMaxYAllowed = Lay.tx("", "selectall", 4)), 40, false);
                addField("Main", "Min Page",  Lay.BL("W", "nogap", Lay.lb("MnPg: "), txtMinPage     = Lay.tx("", "selectall", 4)), 40, false);
                addField("Main", "Max Page",  Lay.BL("W", "nogap", Lay.lb("MxPg: "), txtMaxPage     = Lay.tx("", "selectall", 4)), 40, false);
            }

            @Override
            protected boolean showSaveButton() {
                return false;
            }
            @Override
            protected boolean showCancelButton() {
                return false;
            }
        }
    }
}
