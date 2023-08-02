package gov.sandia.reframe.ui.hierarchy;

import java.util.ArrayList;
import java.util.List;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import replete.collections.Pair;
import replete.equality.EqualsUtil;

public class DeltaNode {

    private String displayLabel;
    private String displayText;
    private int page;
    private String bullet;
    private String title;
    private String text;

    private Pair<String, String> labelDiff;
    private Pair<String, String> bulletDiff;
    private Pair<String, String> titleDiff;
    private Pair<String, String> textDiff;
    private Pair<String, String> pageDiff;
    private Pair<String, String> childrenDiff;

    private List<DeltaNode> children = new ArrayList<>();


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public String getDisplayText() {
        return displayText;
    }
    public int getPage() {
        return page;
    }

    public Pair<String, String>  getLabelDiff() {
        return labelDiff;
    }
    public Pair<String, String>  getBulletDiff() {
        return bulletDiff;
    }
    public Pair<String, String>  getTitleDiff() {
        return titleDiff;
    }
    public Pair<String, String>  getTextDiff() {
        return textDiff;
    }
    public Pair<String, String>  getPageDiff() {
        return pageDiff;
    }
    public Pair<String, String>  getChildrenDiff() {
        return childrenDiff;
    }

    public List<DeltaNode> getChildren() {
        return children;
    }

    // Accessors (Computed)

    public boolean isSame() {
        return
            labelDiff == null &&
            bulletDiff == null &&
            titleDiff == null &&
            textDiff == null &&
            pageDiff == null &&
            childrenDiff == null;
    }

    public boolean isSameIgnoringChildren() {
        return
            labelDiff == null &&
            bulletDiff == null &&
            titleDiff == null &&
            textDiff == null &&
            pageDiff == null;
    }

    public boolean isSameLabelTitlePage() {
        return
            labelDiff == null &&
//            bulletDiff == null &&
            titleDiff == null &&
            pageDiff == null;
    }

    public boolean containsText(String strLower) {
        if(bullet != null && bullet.toLowerCase().contains(strLower)) {
            return true;
        }
        if(title != null && title.toLowerCase().contains(strLower)) {
            return true;
        }
        if(text != null && text.toLowerCase().contains(strLower)) {
            return true;
        }
        return false;
    }

    // Mutators

    public void addChild(DeltaNode dChild) {
        children.add(dChild);
    }

    private Pair<String, String> buildTrace(String lbl, String prev, String cur) {
        StringBuilder prevBuffer = new StringBuilder("<html><b>Previous ");
        prevBuffer.append(lbl);
        prevBuffer.append(" = </b><i>");
        prevBuffer.append(prev);
        prevBuffer.append("</i></html>");

        StringBuilder curBuffer = new StringBuilder("<html><b>Current ");
        curBuffer.append(lbl);
        curBuffer.append(" = </b><i>");
        curBuffer.append(cur);
        curBuffer.append("</i></html>");

        return new Pair<String, String>(prevBuffer.toString(), curBuffer.toString());
    }

    public void init(HNode hPrev, HNode hCur) {
        if(!EqualsUtil.equals(hPrev.getLevelLabel(), hCur.getLevelLabel())) {
            labelDiff = buildTrace("Label", hPrev.getLevelLabel(), hCur.getLevelLabel());
        }
        if(!EqualsUtil.equals(hPrev.getBullet(), hCur.getBullet())) {
            bulletDiff = buildTrace("Bullet",
                spRepl(hPrev.getBullet()),
                spRepl(hCur.getBullet())
            );
        }
        if(!EqualsUtil.equals(hPrev.getTitle(), hCur.getTitle())) {
            titleDiff = buildTrace("Title",
                spRepl(hPrev.getTitle()),
                spRepl(hCur.getTitle())
            );
        }
        if(!EqualsUtil.equals(hPrev.getText(), hCur.getText())) {
            textDiff = buildTrace("Text",
                spRepl(hPrev.getText()),
                spRepl(hCur.getText())
            );
        }
        if(hPrev.getPage() != hCur.getPage()) {
            pageDiff = buildTrace("Page", hPrev.getPage() + "", hCur.getPage() + "");
        }
        if(hPrev.getChildren().size() != hCur.getChildren().size()) {
            childrenDiff = buildTrace("Children Count",
                hPrev.getChildren().size() + "",
                hCur.getChildren().size() + ""
            );
        }

        if(isSameIgnoringChildren()) {
            displayLabel = hPrev.toString();
        } else {
            displayLabel = hPrev.toDiffString(
                labelDiff,
                bulletDiff,
                titleDiff,
                textDiff,
                pageDiff
            );
        }
        displayText = textDiff == null ? hPrev.getText() : "!!!";
        page = hCur.getPage();      // Just kinda need one for navigation regardless of diff...

        bullet = bulletDiff == null ? hPrev.getBullet() : null;
        title = titleDiff == null ? hPrev.getTitle() : null;
        text = textDiff == null ? hPrev.getText() : null;
    }
    private String spRepl(String str) {
        return " ".equals(str) ? "{SPACE}" : str;
    }


    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public String toString() {
        return displayLabel;
    }
}