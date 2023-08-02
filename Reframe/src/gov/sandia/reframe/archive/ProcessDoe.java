package gov.sandia.reframe.archive;

import java.io.IOException;

public class ProcessDoe {
    public static void main(String[] args) throws IOException {
//        File dir = new File("C:\\Users\\dtrumbo\\work\\eclipse-main\\Orbweaver\\resources-UCNI");
//
//        CSVReader reader = new CSVReader(new FileReader(new File(dir, "doe.csv")));
//        List<String[]> lines = reader.readAll();
//        reader.close();
//
//        List<String[]> finalDocs = new ArrayList<>();
//        String[] prevLine = null;
//        for(String[] line : lines) {
//            String doeDoc = CSVProcessor.massage(line[7]);
//            if(prevLine != null) {
//                for(int i = 3; i < 7; i++) {
//                    if(line[i].equals("")) {
//                        line[i] = prevLine[i];
//                    }
//                }
//            }
//            String doeKey = StringUtil.join(StringUtil.slice(line, 3, 7), "/");
//            finalDocs.add(new String[] {doeKey, doeDoc});
//            prevLine = line;
//        }
//
//        CSVWriter writer = new CSVWriter(new FileWriter(new File(dir, "doe-output.csv")));
//        writer.writeAll(finalDocs);
//        writer.close();
    }
}
