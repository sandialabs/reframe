package gov.sandia.reframe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTester {

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("\\s?[A-Z][-&A-Za-z/]*(?:(?:,? and|,? or| to| and/or| of| an| on| a| the|,| /| &)? \\(?[A-Z][-&A-Za-z0-9/]*\\)?)*(?!(\\s[\\w-,\"]|[\\w-,\"]|\\.S\\.))\\.?");
        Matcher matcher = pattern.matcher("PROGRAM AFFORDABILITY");
        System.out.println(matcher.matches());
    }

}
