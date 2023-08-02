package gov.sandia.reframe.ui.params;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import gov.sandia.cortext.fileparsing.pdf.CaptureAs;
import gov.sandia.cortext.fileparsing.pdf.Rule;
import replete.numbers.PositiveNumberRangeList;
import replete.ui.form.FieldDescriptor;
import replete.ui.form.RFormPanel;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.text.DocumentChangeListener;
import replete.ui.text.RTextField;
import replete.ui.windows.escape.EscapeFrame;

public class RulePanel extends RPanel {


    ////////////
    // FIELDS //
    ////////////

    // Constants

    private static final Color CLR_INVALID = Lay.clr("255,205,205");

    // Core

    private Rule rule;

    // UI

    private JCheckBox chkExclusion;
    private RTextField txtFontName;
    private RTextField txtMinFontSize;
    private RTextField txtMaxFontSize;
    private JSlider sldBold;
    private JSlider sldItalic;
    private RTextField txtPages;
    private RTextField txtMinX;
    private RTextField txtMaxX;
    private RTextField txtMinY;
    private RTextField txtMaxY;
    private RTextField txtMaxW;
    private RTextField txtMaxH;
    private RTextField txtPattern;
    private JComboBox<CaptureAs> cboCaptureAs;
    private JCheckBox chkOptional;
    private JCheckBox chkNoTrim;
    private JCheckBox chkStartOnNewLine;
    private JCheckBox chkDisabled;
    private RuleFormPanel pnlForm;

    private boolean suppressChange;
    private boolean autoFs = true;
    private boolean autoX = true;
    private boolean autoY = true;
    private boolean autoW = true;



    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public RulePanel() {
        rule = new Rule();

        Lay.BLtg(this,
            "C", pnlForm = new RuleFormPanel()
        );

        txtMinFontSize.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(autoFs) {
                    suppressChange = true;
                    txtMaxFontSize.setText(txtMinFontSize.getText());
                    suppressChange = false;
                }
            }
        });
        txtMinX.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(autoX) {
                    suppressChange = true;
                    txtMaxX.setText(txtMinX.getText());
                    suppressChange = false;
                }
            }
        });
        txtMinY.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(autoY) {
                    suppressChange = true;
                    txtMaxY.setText(txtMinY.getText());
                    suppressChange = false;
                }
            }
        });
        txtMaxW.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(autoW) {
                    suppressChange = true;
                    txtMaxH.setText(txtMaxW.getText());
                    suppressChange = false;
                }
            }
        });

        txtMaxFontSize.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(!suppressChange) {
                    autoFs = false;
                }
            }
        });
        txtMaxX.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(!suppressChange) {
                    autoX = false;
                }
            }
        });
        txtMaxY.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(!suppressChange) {
                    autoY = false;
                }
            }
        });
        txtMaxH.addChangeListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(!suppressChange) {
                    autoW = false;
                }
            }
        });

        chkExclusion.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                pnlForm.setRedExclusion(chkExclusion.isSelected());
            }
        });
    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public Rule getRule() {
        if(!isValidRule()) {
            return null;
        }

        Rule rule = new Rule()
            .setExclusion(chkExclusion.isSelected())
            .setFontName(wrapTextString(txtFontName))
            .setMinFontSize(wrapTextFloat(txtMinFontSize))
            .setMaxFontSize(wrapTextFloat(txtMaxFontSize))
            .setBold(wrapTextBoolean(sldBold))
            .setItalic(wrapTextBoolean(sldItalic))
            .setPages(wrapTextRange(txtPages))
            .setMinX(wrapTextFloat(txtMinX))
            .setMaxX(wrapTextFloat(txtMaxX))
            .setMinY(wrapTextFloat(txtMinY))
            .setMaxY(wrapTextFloat(txtMaxY))
            .setMaxWidth(wrapTextFloat(txtMaxW))
            .setMaxHeight(wrapTextFloat(txtMaxH))
            .setPattern(wrapTextStringNoTrim(txtPattern))
            .setCaptureAs((CaptureAs) cboCaptureAs.getSelectedItem())
            .setOptional(chkOptional.isSelected())
            .setNoTrim(chkNoTrim.isSelected())
            .setStartOnNewLine(chkStartOnNewLine.isSelected())
            .setDisabled(chkDisabled.isSelected());

        return rule;
    }

    private String wrapTextString(RTextField txt) {
        String value = txt.getTrimmed();
        return value.isEmpty() ? null : value;
    }

    private String wrapTextStringNoTrim(RTextField txt) {
        String value = txt.getText();
        return value.isEmpty() ? null : value;
    }

    private Float wrapTextFloat(RTextField txt) {
        String value = txt.getTrimmed();
        return value.isEmpty() ? null : Float.parseFloat(value);
    }

//    private Integer wrapTextInt(RTextField txt) {
//        String value = txt.getTrimmed();
//        return value.isEmpty() ? null : Integer.parseInt(value);
//    }

    private Boolean wrapTextBoolean(JSlider sld) {
        int value = sld.getValue();
        if(value == 1) {
            return null;
        } else if(value == 0) {
            return false;
        }
        return true;
    }

    private PositiveNumberRangeList wrapTextRange(RTextField txt) {
        String value = txt.getTrimmed();
        return value.isEmpty() ? null : PositiveNumberRangeList.parse(value);  // needs to be validated
    }

    // Mutators

    public void setRule(Rule rule) {
        this.rule = rule;
        populateFromRule();
    }


    //////////
    // MISC //
    //////////

    private boolean isValidRule() {
        boolean valid = true;

        // No validation needed for exclusion

        try {
            wrapTextFloat(txtMinFontSize);
            txtMinFontSize.setBackground(Color.white);
        } catch(Exception e) {
            txtMinFontSize.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMaxFontSize);
            txtMaxFontSize.setBackground(Color.white);
        } catch(Exception e) {
            txtMaxFontSize.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextRange(txtPages);
            txtPages.setBackground(Color.white);
        } catch(Exception e) {
            txtPages.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMinX);
            txtMinX.setBackground(Color.white);
        } catch(Exception e) {
            txtMinX.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMaxX);
            txtMaxX.setBackground(Color.white);
        } catch(Exception e) {
            txtMaxX.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMinY);
            txtMinY.setBackground(Color.white);
        } catch(Exception e) {
            txtMinY.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMaxY);
            txtMaxY.setBackground(Color.white);
        } catch(Exception e) {
            txtMaxY.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMaxW);
            txtMaxW.setBackground(Color.white);
        } catch(Exception e) {
            txtMaxW.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            wrapTextFloat(txtMaxH);
            txtMaxH.setBackground(Color.white);
        } catch(Exception e) {
            txtMaxH.setBackground(CLR_INVALID);
            valid = false;
        }

        try {
            Pattern.compile(txtPattern.getText().trim());
            txtPattern.setBackground(Color.white);
        } catch(Exception e) {
            txtPattern.setBackground(CLR_INVALID);
            valid = false;
        }

        return valid;
    }

    public void cancel() {
        populateFromRule();
    }

    private void populateFromRule() {
        chkExclusion.setSelected(rule.isExclusion());
        txtFontName.setText(wrapBean(rule.getFontName()));
        txtMinFontSize.setText(wrapBean(rule.getMinFontSize()));
        txtMaxFontSize.setText(wrapBean(rule.getMaxFontSize()));
        sldBold.setValue(wrapBean(rule.getBold()));
        sldItalic.setValue(wrapBean(rule.getItalic()));
        txtPages.setText(wrapBean(rule.getPages()));
        txtMinX.setText(wrapBean(rule.getMinX()));
        txtMaxX.setText(wrapBean(rule.getMaxX()));
        txtMinY.setText(wrapBean(rule.getMinY()));
        txtMaxY.setText(wrapBean(rule.getMaxY()));
        txtMaxW.setText(wrapBean(rule.getMaxWidth()));
        txtMaxH.setText(wrapBean(rule.getMaxHeight()));
        txtPattern.setText(wrapBean(rule.getPattern()));
        cboCaptureAs.setSelectedItem(rule.getCaptureAs() == null ? CaptureAs.NOTHING : rule.getCaptureAs());
        chkOptional.setSelected(rule.isOptional());
        chkNoTrim.setSelected(rule.isNoTrim());
        chkStartOnNewLine.setSelected(rule.isStartOnNewLine());
        chkDisabled.setSelected(rule.isDisabled());

        pnlForm.setRedExclusion(chkExclusion.isSelected());    // May not be needed
    }

    private String wrapBean(Object value) {
        return value == null ? "" : value.toString();
    }
    private int wrapBean(Boolean value) {
        return value == null ? 1 : value.equals(false) ? 0 : 2;
    }


    /////////////////
    // INNER CLASS //
    /////////////////

    private  Color CLR_INVALIDx = Lay.clr("255,205,205");
    private class RuleFormPanel extends RFormPanel {

        FieldDescriptor fdEx;

        private RuleFormPanel() {
            super(100);
            init();
            fdEx.pnlField.lblCaption.setOpaque(true);
        }

        public void setRedExclusion(boolean r) {
            if(r) {
                fdEx.cmp.setBackground(CLR_INVALIDx);
                fdEx.pnlField.lblCaption.setBackground(CLR_INVALIDx);
            } else {
                fdEx.cmp.setBackground(new JPanel().getBackground());
                fdEx.pnlField.lblCaption.setBackground(new JPanel().getBackground());
            }
        }

        @Override
        protected void addFields() {

            JLabel lblDiscard;
            JPanel pnlEx = Lay.BL(
                "W", Lay.BxL("X",
                    chkExclusion = Lay.chk(),
                    Lay.lb("  "),
                    lblDiscard = Lay.lb("<html><i>(discards text matching this rule)</i></html>"),
                    "nogap"
                ),
                "chtransp"
            );

            lblDiscard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    chkExclusion.setSelected(!chkExclusion.isSelected());
                }
            });

            JPanel pnlF = Lay.BL(
                "W", Lay.BxL("X",
                    txtMinFontSize = Lay.tx("", 6, "selectall"),
                    Lay.lb("  To  "),
                    txtMaxFontSize = Lay.tx("", 6, "selectall"),
                    Lay.lb("  "),
                    Lay.lb("<html><i>(blank means unbounded)</i></html>"),
                    "nogap"
                )
            );

            JPanel pnlX = Lay.BL(
                "W", Lay.BxL("X",
                    txtMinX = Lay.tx("", 6, "selectall"),
                    Lay.lb("  To  "),
                    txtMaxX = Lay.tx("", 6, "selectall"),
                    Lay.lb("  "),
                    Lay.lb("<html><i>(blank means unbounded)</i></html>"),
                    "nogap"
                )
            );

            JPanel pnlY = Lay.BL(
                "W", Lay.BxL("X",
                    txtMinY = Lay.tx("", 6, "selectall"),
                    Lay.lb("  To  "),
                    txtMaxY = Lay.tx("", 6, "selectall"),
                    Lay.lb("  "),
                    Lay.lb("<html><i>(blank means unbounded)</i></html>"),
                    "nogap"
                )
            );

            JPanel pnlH = Lay.BL(
                "W", Lay.BxL("X",
                    txtMaxW = Lay.tx("", 6, "selectall"),
                    Lay.lb("  To  "),
                    txtMaxH = Lay.tx("", 6, "selectall"),
                    Lay.lb("  "),
                    Lay.lb("<html><i>(blank means unbounded)</i></html>"),
                    "nogap"
                )
            );

            fdEx = addField("Main", "Exclusion Rule?",  pnlEx, 40, false);
            addField("Main", "Font Name",    txtFontName  = Lay.tx("", "selectall"), 40, false,
                "<html><i>(regular expression SOON)</i></html>");
            addField("Main", "Font Size",    pnlF, 40, false);
            addField("Main", "Bold",         sldBold      = makeSlider("Bold"), 50, false);
            addField("Main", "Italic",       sldItalic    = makeSlider("Italic"), 50, false);
            addField("Main", "Pages",        txtPages     = Lay.tx("", "selectall"), 40, false,
                "<html><i>(e.g. \"1-3, 6-13, 22\")</i></html>");
            addField("Main", "X Position",   pnlX, 40, false);
            addField("Main", "Y Position",   pnlY, 40, false);
            addField("Main", "Bounds(w, h)", pnlH, 40, false);
            addField("Main", "Pattern",      txtPattern   = Lay.tx("", "selectall"), 40, false,
                "<html><i>(regular expression)</i></html>");
            addField("Main", "Capture As",   cboCaptureAs = Lay.cb(CaptureAs.values()), 40, false);
            addField("Main", "Optional?",    chkOptional  = Lay.chk(), 40, false);
            addField("Main", "No Trim?",     chkNoTrim = Lay.chk(), 40, false);
            addField("Main", "Starts Line?", chkStartOnNewLine = Lay.chk(), 40, false);
            addField("Main", "Disabled?",    chkDisabled = Lay.chk(), 40, false);
            chkDisabled.setEnabled(false);
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

    private JSlider makeSlider(String style) {
        JSlider sld = new JSlider(SwingConstants.HORIZONTAL, 0, 2, 1);
        sld.setMajorTickSpacing(1);
        sld.setMinorTickSpacing(1);
        sld.setPaintLabels(true);
        sld.setOpaque(false);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(new Integer(0), new JLabel("NOT " + style));
        labelTable.put(new Integer(1), new JLabel("No Preference"));
        labelTable.put(new Integer(2), new JLabel(style));
        sld.setLabelTable(labelTable);
        return sld;
    }


    //////////
    // TEST //
    //////////

    public static void main(String[] args) {

        final EscapeFrame f = new EscapeFrame("Rule Panel Test");
        final RulePanel pnlRule = new RulePanel();

        JButton btnCancel, btnSet, btnGet, btnExit;
        Lay.BLtg(f,
            "C", pnlRule,
            "S", Lay.FL("R",
                btnCancel = Lay.btn("&Cancel"),
                btnSet = Lay.btn("&Set"),
                btnGet = Lay.btn("&Get"),
                btnExit = Lay.btn("&Exit"),
                "bg=100,mb=[1t,black]"
            ),
            "size=[550,645],center,visible=true"
        );

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlRule.cancel();
            }
        });
        btnSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rule rule = new Rule()
                    .setExclusion(true)
                    .setBold(true)
                    .setItalic(null)
                    .setFontName("Arial")
                    .setMinFontSize(12.0F)
                    .setMaxFontSize(12.0F)
                ;
                pnlRule.setRule(rule);
            }
        });
        btnGet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rule rule = pnlRule.getRule();
                if(rule != null) {
                    System.out.println(rule);
                    System.out.println(rule.toSimpleString());
                }
            }
        });
        btnExit.addActionListener(e -> f.close());
    }
}
