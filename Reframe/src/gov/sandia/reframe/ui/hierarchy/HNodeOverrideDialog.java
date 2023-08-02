package gov.sandia.reframe.ui.hierarchy;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.text.StringUtil;
import replete.ui.form.RFormPanel;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.text.DocumentChangeListener;
import replete.ui.text.RTextField;
import replete.ui.text.RTextPane;
import replete.ui.windows.escape.EscapeDialog;

public class HNodeOverrideDialog extends EscapeDialog {


    ////////////
    // FIELDS //
    ////////////

    public static final int SET = 0;
    public static final int CLEAR = 1;
    public static final int CANCEL = 2;

    private int result = CANCEL;

    private HNodeForm pnlOriginal;
    private HNodeForm pnlOverridden;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public HNodeOverrideDialog(JFrame parent, HNode hOriginal, HNode hOverridden) {
        super(parent, "Override Hierarchy Node", true);
        setIcon(CommonConcepts.EDIT);

        JButton btnSet;
        JButton btnClear;
        JButton btnCancel;

        Lay.BLtg(this,
            "C", Lay.GL(1, 2,
                Lay.BL(
                    "N", Lay.FL(Lay.lb("Original"), "bg=EDFFFF,mb=[1b,blue]"),
                    "C", pnlOriginal = new HNodeForm(hOriginal, false),
                    "eb=10b,augb=mb(1r,black)"
                ),
                Lay.BL(
                    "N", Lay.FL(
                        Lay.lb("Override", hOverridden != null ? ReframeImageModel.OVER : null),
                        "bg=FFCDCD,mb=[1b,red]"
                    ),
                    "C", pnlOverridden = new HNodeForm(hOverridden == null ? hOriginal : hOverridden, true),
                    "eb=10b"
                )
            ),
            "S", Lay.FL("R",
                btnSet = Lay.btn("&Set Override", CommonConcepts.ACCEPT),
                btnClear = Lay.btn("Clea&r Override", ReframeImageModel.CLEAR),
                btnCancel = Lay.btn("&Cancel", CommonConcepts.CANCEL),
                "bg=100,mb=[1t,black],hgap=10,vgap=10"
            ),
            "size=[800,500],center"
        );

        btnClear.setEnabled(hOverridden != null);

        pnlOverridden.txtBullet.addChangeListener(sameListener);
        pnlOverridden.chkBulletProblem.addActionListener(actionSameListener);
        pnlOverridden.txtTitle.addChangeListener(sameListener);
        pnlOverridden.txtText.addChangeListener(sameListener);
        checkSame();

        btnSet.addActionListener(e -> {
            result = SET;
            close();
        });
        btnClear.addActionListener(e -> {
            result = CLEAR;
            close();
        });
        btnCancel.addActionListener(e -> close());
    }

    private DocumentChangeListener sameListener = new DocumentChangeListener() {
        @Override
        public void documentChanged(DocumentEvent e) {
            checkSame();
        }
    };

    private ActionListener actionSameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            checkSame();
        }
    };

    private void checkSame() {
        boolean same = pnlOverridden.txtBullet.getText().equals(pnlOriginal.txtBullet.getText());
        pnlOverridden.txtBullet.setBackground(same ? Color.white : Lay.clr("255,205,205"));

        same = pnlOverridden.chkBulletProblem.isSelected() == pnlOriginal.chkBulletProblem.isSelected();
        pnlOverridden.chkBulletProblem.setBackground(same ? new JCheckBox().getBackground() : Lay.clr("255,205,205"));

        same = pnlOverridden.txtTitle.getText().equals(pnlOriginal.txtTitle.getText());
        pnlOverridden.txtTitle.setBackground(same ? Color.white : Lay.clr("255,205,205"));

        same = pnlOverridden.txtText.getText().equals(pnlOriginal.txtText.getText());
        pnlOverridden.txtText.setBackground(same ? Color.white : Lay.clr("255,205,205"));
    }


    ///////////////
    // ACCESSORS //
    ///////////////

    public int getResult() {
        return result;
    }
    public String getOverrideBullet() {
        return pnlOverridden.txtBullet.getTrimmed();      // Cannot be null
    }
    public boolean getOverrideBulletProblem() {
        return pnlOverridden.chkBulletProblem.isSelected();
    }
    public String getOverrideTitle() {
        return pnlOverridden.txtTitle.getTrimmed();
    }
    public String getOverrideText() {
        return pnlOverridden.txtText.getTrimmed();
    }
    public boolean isRemove() {
        return pnlOverridden.chkRemove.isSelected();
    }


    //////////
    // MISC //
    //////////

    private String nl(String str) {
        return str.trim().equals("") ? null : str;
    }


    /////////////////
    // INNER CLASS //
    /////////////////

    private class HNodeForm extends RFormPanel {

        private RTextField txtBullet;
        private RTextField txtTitle;
        private RTextPane txtText;
        private JCheckBox chkBulletProblem;
        private JCheckBox chkBulletGuessed;
        private JCheckBox chkRemove;
        private RTextField lblBulletPreGuessed;
        private HNode hNode;
        private boolean editable;

        public HNodeForm(HNode hEdited, boolean editable) {
            super(150);    // Label area pixel width
            hNode = hEdited;
            this.editable = editable;
            init();
        }


        @Override
        protected void addFields() {
            String MAIN_PANE = "Main";

            addField(MAIN_PANE, "Bullet",          txtBullet = Lay.tx("", "selectall"), 40, false);
            addField(MAIN_PANE, "Bullet Problem?", chkBulletProblem = Lay.chk(),        40, false);
            addField(MAIN_PANE, "Bullet Guessed?", chkBulletGuessed = Lay.chk(),        40, false);
            addField(MAIN_PANE, "Title",           txtTitle = Lay.tx("", "selectall"),  40, false);
            addField(MAIN_PANE, "Text",            Lay.sp(txtText = Lay.txp()),         40, true);
            if(editable) {
                addField(MAIN_PANE, "Remove?",     chkRemove = Lay.chk(),               40, false);
            } else {
                addField(MAIN_PANE, "Pre-Guessed Bullet", lblBulletPreGuessed = Lay.tx("", "selectall"), 40, false);
            }

            if(hNode != null) {    // TODO: Why would hNode be null?
                txtBullet.setText(StringUtil.cleanNull(hNode.getBullet()));
                txtTitle.setText(StringUtil.cleanNull(hNode.getTitle()));
                txtText.setText(StringUtil.cleanNull(hNode.getText()));
                chkBulletProblem.setSelected(hNode.isBulletProblem());
                chkBulletGuessed.setSelected(hNode.isBulletGuessed());
            }

            chkBulletGuessed.setEnabled(false);
            if(!editable) {
                txtBullet.setEditable(false);
                chkBulletProblem.setEnabled(false);
                txtTitle.setEditable(false);
                txtText.setEditable(false);
                txtBullet.setBackground(Color.white);
                txtTitle.setBackground(Color.white);
                lblBulletPreGuessed.setText(StringUtil.cleanNull(hNode.getBulletPreGuessed()));
                lblBulletPreGuessed.setEditable(false);
                lblBulletPreGuessed.setBackground(Color.white);
            }
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


    //////////
    // TEST //
    //////////

    public static void main(String[] args) {

        HNode hOrig = new HNode();
        hOrig.setBullet("b.");
        hOrig.setTitle("Area Requirements");
        hOrig.setText(null);
        hOrig.setBulletProblem(true);
        hOrig.setBulletGuessed(false);

        HNodeOverrideDialog dlg = new HNodeOverrideDialog(null, hOrig, hOrig);
        dlg.setVisible(true);
        if(dlg.getResult() == HNodeOverrideDialog.SET) {
            System.out.println("changing");
        } else if(dlg.getResult() == HNodeOverrideDialog.CLEAR) {
            System.out.println("Clearing");
        }
    }
}
