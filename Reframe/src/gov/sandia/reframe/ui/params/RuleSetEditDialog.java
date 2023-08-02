package gov.sandia.reframe.ui.params;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import replete.ui.button.RButton;
import replete.ui.form.RFormPanel;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.text.RTextField;
import replete.ui.windows.Dialogs;
import replete.ui.windows.escape.EscapeDialog;

public class RuleSetEditDialog extends EscapeDialog {


    ////////////
    // FIELDS //
    ////////////

    public static final int OK = 1;
    public static final int CANCEL = 2;

    private int result = CANCEL;
    private RTextField txtLevelLabel;
    private RTextField txtChildBulletPattern;
    private RTextField txtSiblingBulletPattern;
    private JCheckBox chkDisabled;
    private JCheckBox chkReplaceChildren;
    private JCheckBox chkReplaceSibling;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public RuleSetEditDialog(JFrame parent, String label, String childBullet, String siblingBullet, boolean replaceChildren, boolean replaceSiblings, boolean create) {
        super(parent, create ? "Create Rule Set" : "Edit Rule Set", true);
        setIcon(CommonConcepts.EDIT);

        RButton btnOk, btnCancel;
        Lay.BLtg(this,
            "C", new RuleSetFormPanel(),
            "S", Lay.FL("R",
                btnOk = Lay.btn("&OK", CommonConcepts.ACCEPT),
                btnCancel = Lay.btn("&Cancel", CommonConcepts.CANCEL),
                "bg=100,mb=[1t,black]"
            ),
            "size=[650,350],center"
        );
        txtLevelLabel.setText(label);
        txtChildBulletPattern.setText(childBullet);
        txtSiblingBulletPattern.setText(siblingBullet);
        chkReplaceChildren.setSelected(replaceChildren);
        chkReplaceSibling.setSelected(replaceSiblings);
        //chkDisabled.setSelected(????????);

        setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            if(txtLevelLabel.isBlank()) {
                Dialogs.showWarning(this,
                    "The level label cannot be blank.", "Rule Set Validation");
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

    public String getLevelLabel() {
        return txtLevelLabel.getTrimmed();
    }
    public String getChildBulletPattern() {
        return txtChildBulletPattern.getTrimmed();
    }
    public String getSiblingBulletPattern() {
        return txtSiblingBulletPattern.getTrimmed();
    }
    public boolean getChildPatternReplace() {
        return chkReplaceChildren.isSelected();
    }
    public boolean getSiblingPatternReplace() {
        return chkReplaceSibling.isSelected();
    }
    public int getResult() {
        return result;
    }
    // disabled stuff??????????//


    /////////////////
    // INNER CLASS //
    /////////////////

    private class RuleSetFormPanel extends RFormPanel {

        private RuleSetFormPanel() {
            super(125);
            init();
        }

        @Override
        protected void addFields() {

            JPanel pnlCbp = Lay.BL(
                "C", txtChildBulletPattern = Lay.tx("", "selectall"),
                "E", chkReplaceChildren = Lay.chk("Replace?")
            );
            JPanel pnlSbp = Lay.BL(
                "C", txtSiblingBulletPattern = Lay.tx("", "selectall"),
                "E", chkReplaceSibling = Lay.chk("Replace?")
            );

            addField("Main", "Title", txtLevelLabel  = Lay.tx("", "selectall"), 40, false,
                "<html><i>(e.g. \"Title\", \"H1\", \"Appendix\", not used in hierarchy export)</i></html>");
            addField("Main", "Child Bullet Pattern", pnlCbp, 40, false,
                "<html><i>(pattern with N, A, a, or R as special characters, e.g. \"${a}.\", \"(${N})\")</i></html>");
            addField("Main", "Sibling Bullet Pattern", pnlSbp, 40, false,
                    "<html><i>(Sibling patterns will be preferred over the child bullet patterns of this node's parent)</i></html>");
            addField("Main", "Disabled?", chkDisabled = Lay.chk(), 40, false);

            chkDisabled.setEnabled(false);
            //chkReplace.setEnabled(false);
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
