package gov.sandia.reframe.ui.images;

import replete.ui.images.concepts.ImageModel;
import replete.ui.images.concepts.ImageModelConcept;
import replete.ui.images.shared.SharedImage;

public class ReframeImageModel extends ImageModel {
    public static final ImageModelConcept AR_BL_L             = conceptLocal("ar-bl-l.png");
    public static final ImageModelConcept AR_BL_R             = conceptLocal("ar-bl-r.png");
    public static final ImageModelConcept AR_GR_L             = conceptLocal("ar-gr-l.png");
    public static final ImageModelConcept AR_GR_R             = conceptLocal("ar-gr-r.png");
    public static final ImageModelConcept CLEAR               = conceptLocal("clear.gif");
    public static final ImageModelConcept DELTA               = conceptLocal("delta.gif");
    public static final ImageModelConcept DIFF                = conceptLocal("diff.gif");
    public static final ImageModelConcept DIFF_CH             = conceptLocal("diff-ch.gif");
    public static final ImageModelConcept ELEMS               = conceptShared(SharedImage.SHAPES_4_MULTI_COLORED);
    public static final ImageModelConcept EXPLORER            = conceptShared(SharedImage.DIR_HIER);
    public static final ImageModelConcept EXTRACT             = conceptLocal("extract.png");
    public static final ImageModelConcept GR                  = conceptLocal("gr.gif");
    public static final ImageModelConcept HIER                = conceptLocal("hier.gif");
    public static final ImageModelConcept NODE                = conceptShared(SharedImage.DOT_BLUE);
    public static final ImageModelConcept NODE_EXCEPTION      = conceptLocal("node-exception.gif");
    public static final ImageModelConcept NODE_OVER           = conceptLocal("node-over.gif");
    public static final ImageModelConcept NODE_OVER_EXCEPTION = conceptLocal("node-over-exception.gif");
    public static final ImageModelConcept OVER                = conceptLocal("over.gif");
    public static final ImageModelConcept READER              = conceptLocal("reader.png");
    public static final ImageModelConcept RULE                = conceptShared(SharedImage.DOT_BLUE_SMALL);
    public static final ImageModelConcept RULE_ADD            = conceptLocal("rule-add.gif");
    public static final ImageModelConcept RULE_DIS            = conceptLocal("rule-dis.gif");
    public static final ImageModelConcept RULE_EXCL           = conceptLocal("rule-excl.gif");
    public static final ImageModelConcept RULESET             = conceptShared(SharedImage.CIRCLES_3_MULTI_COLORED);
    public static final ImageModelConcept RULESET_ADD         = conceptLocal("ruleset-add.gif");
    public static final ImageModelConcept RULESET_DIS         = conceptLocal("ruleset-dis.gif");
    public static final ImageModelConcept SAME                = conceptLocal("same.gif");
    public static final ImageModelConcept TOGGLE_PARAMS       = conceptShared(SharedImage.WINDOW_TITLE_BAR_SEGMENTED);
    public static final ImageModelConcept TRACE               = conceptLocal("trace.gif");


    //////////
    // TEST //
    //////////

    public static void main(String[] args) {
        visualize(3);
    }
}
