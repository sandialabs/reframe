package gov.sandia.reframe.ui.params;

import javax.swing.JFrame;

import gov.sandia.cortext.fileparsing.pdf.Rule;
import replete.ui.button.RButton;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.windows.Dialogs;
import replete.ui.windows.escape.EscapeDialog;

public class RuleEditDialog extends EscapeDialog {


    ////////////
    // FIELDS //
    ////////////

    public static final int OK = 1;
    public static final int CANCEL = 2;

    private int result = CANCEL;
    private RulePanel pnlRule;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public RuleEditDialog(JFrame parent, Rule rule, boolean create) {
        super(parent, create ? "Create Rule" : "Edit Rule", true);
        setIcon(CommonConcepts.EDIT);

        RButton btnOk, btnCancel;
        Lay.BLtg(this,
            "C", pnlRule = new RulePanel(),
            "S", Lay.FL("R",
                btnOk = Lay.btn("&OK", CommonConcepts.ACCEPT),
                btnCancel = Lay.btn("&Cancel", CommonConcepts.CANCEL),
                "bg=100,mb=[1t,black]"
            ),
            "size=[550,765],center"
        );

        setDefaultButton(btnOk);

        pnlRule.setRule(rule);

        btnOk.addActionListener(e -> {
            if(pnlRule.getRule() == null) {
                Dialogs.showWarning(this,
                    "There are validation errors with this rule.", "Rule Validation");
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

    public Rule getRule() {
        return pnlRule.getRule();
    }
    public int getResult() {
        return result;
    }
}
