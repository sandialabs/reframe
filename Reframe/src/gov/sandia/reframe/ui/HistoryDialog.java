package gov.sandia.reframe.ui;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

import gov.sandia.reframe.ParseRecord;
import gov.sandia.reframe.Pdf;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.table.DefaultUiHintedTableModel;
import replete.ui.windows.escape.EscapeDialog;
import replete.util.DateUtil;

public class HistoryDialog extends EscapeDialog {


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public HistoryDialog(JFrame owner, Pdf pdf) {
        super(owner, "PDF History", true);
        setIcon(CommonConcepts.DATE_TIME);

        HistoryTableModel mdl = new HistoryTableModel(pdf);
        Lay.BLtg(this,
            "C", Lay.sp(Lay.tbl(mdl)),
            "S", Lay.FL("R", Lay.btn("&Close", CommonConcepts.CANCEL, createCloseListener())),
            "size=[800,600],center"
        );
    }


    /////////////////
    // INNER CLASS //
    /////////////////

    public class HistoryTableModel extends DefaultUiHintedTableModel {


        ////////////
        // FIELDS //
        ////////////

        private Pdf pdf;


        /////////////////
        // CONSTRUCTOR //
        /////////////////

        public HistoryTableModel(Pdf pdf) {
            this.pdf = pdf;
        }


        ////////////////
        // OVERRIDDEN //
        ////////////////

        @Override
        protected void init() {
            addColumn("User",          String.class,  new int[] { 80,  80,  80});
            addColumn("Started",       String.class,  new int[] {150, 150, 150});
            addColumn("Duration",      String.class,  new int[] { 70,  70,  70});
            addColumn("Node Count",    Integer.class, new int[] {100, 100, 100});
            addColumn("Node Count/NB", Integer.class, new int[] {100, 100, 100});
            addColumn("MD5",           String.class,  new int[] { 80,  80, 300});
            addColumn("Error?",        String.class,  new int[] { -1,  -1,  -1});
        }

        @Override
        public int getRowCount() {
            if(pdf == null || pdf.getParseHistory() == null) {
                return 0;
            }
            return pdf.getParseHistory().size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            ParseRecord record = pdf.getParseHistory().get(row);
            switch(col) {
                case 0: return record.getUser();
                case 1: return DateUtil.toLongString(record.getStarted());
                case 2: return DateUtil.toElapsedString(record.getEnded() - record.getStarted());
                case 3: return record.getNodeCount();
                case 4: return record.getNodeCountNonBlank();
                case 5: return record.getPdfMd5Hash();
                case 6: return record.getError() != null ? "Yes: " + record.getError().getClass().getName() : "No";
            }
            return null;
        }

        @Override
        public int getAlignment(int row, int col) {
            if(col == 2 || col == 3 || col == 4) {
                return SwingConstants.RIGHT;
            } else if(col == 1) {
                return SwingConstants.CENTER;
            }
            return SwingConstants.LEFT;
        }
    }
}