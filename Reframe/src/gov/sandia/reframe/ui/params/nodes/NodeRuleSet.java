package gov.sandia.reframe.ui.params.nodes;

import javax.swing.Icon;

import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.text.StringUtil;
import replete.ui.images.concepts.ImageLib;
import replete.ui.tree.NodeBase;

public class NodeRuleSet extends NodeBase {


    ////////////
    // FIELDS //
    ////////////

    //private RuleSet ruleSet;
    private String  levelLabel;
    private String  childBulletPattern;
    private boolean childPatternReplace = false;
    private String  siblingBulletPattern;
    private boolean siblingPatternReplace = false;
    private boolean parentDisabled = false;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public NodeRuleSet() {
        this("TBD", null, false, null, false);
    }
    public NodeRuleSet(/*Rule ruleSet, */String levelLabel, String childBulletPattern, boolean childPatternReplace, String siblingBulletPattern, boolean siblingPatternReplace) {
        //this.ruleSet = ruleSet;
        this.levelLabel = levelLabel;
        this.childBulletPattern = childBulletPattern;
        this.childPatternReplace = childPatternReplace;
        this.siblingBulletPattern = siblingBulletPattern;
        this.siblingPatternReplace = siblingPatternReplace;

    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public String getLevelLabel() {
        return levelLabel;
    }
    public String getChildBulletPattern() {
        return childBulletPattern;
    }
    public String getSiblingBulletPattern() {
        return siblingBulletPattern;
    }
    public boolean isParentDisabled() {
        return parentDisabled;
    }
    public boolean getChildPatternReplace() {
        return childPatternReplace;
    }
    public boolean getSiblingPatternReplace() {
        return siblingPatternReplace;
    }

    // Mutator

    public NodeRuleSet setLevelLabel(String levelLabel) {
        this.levelLabel = levelLabel;
        return this;
    }
    public NodeRuleSet setChildBulletPattern(String childBulletPattern) {
        this.childBulletPattern = childBulletPattern;
        return this;
    }
    public NodeRuleSet setSiblingBulletPattern(String siblingBulletPattern) {
        this.siblingBulletPattern = siblingBulletPattern;
        return this;
    }
    public NodeRuleSet setChildPatternReplace(boolean childPatternReplace) {
        this.childPatternReplace = childPatternReplace;
        return this;
    }
    public NodeRuleSet setSiblingPatternReplace(boolean siblingPatternReplace) {
        this.siblingPatternReplace = siblingPatternReplace;
        return this;
    }
    public NodeRuleSet setParentDisabled(boolean parentDisabled) {
        this.parentDisabled = parentDisabled;
        return this;
    }


    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public Icon getIcon(boolean expanded) {
        if(parentDisabled /*|| ruleSet.isDisabled()*/) {  // Gray if this rule or parent rule disabled
            return ImageLib.get(ReframeImageModel.RULESET_DIS);
        }
        return ImageLib.get(ReframeImageModel.RULESET);
    }

    @Override
    public String toString() {
        return "<" + levelLabel + ">" + (!StringUtil.isBlank(childBulletPattern) ? " [Child Bullets: " + childBulletPattern + "]" : "")
                                      + (!StringUtil.isBlank(siblingBulletPattern) ? " [Sibling Bullets: " + siblingBulletPattern + "]" : "");
    }

//    @Override
//    public String toString() {
//        return ruleSet.toSimpleString();  // "[DIS]" only shown if this very rule disabled
//    }
}
