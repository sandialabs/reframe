package gov.sandia.reframe.archive;


public class CSVProcessor {
    public static void main(String[] args) throws Exception {
//        File inFile = new File("C:\\Users\\jtmccl\\Documents\\test.csv");
//        File outFile = new File("C:\\Users\\jtmccl\\Documents\\out.csv");
//        CSVReader reader = new CSVReader(new FileReader(inFile));
//        CSVWriter writer = new CSVWriter(new FileWriter(outFile));
//        String[] line;
//        while((line = reader.readNext()) != null) {
//            for(int i = 0; i < line.length; i++) {
//                System.out.println(line[i]);
//                line[i] = massage(line[i]);
//                System.out.println(line[i]);
//                System.out.println("**************************************************************");
//            }
//            writer.writeNext(line);
//        }
    }

    public static String massage(String string) {
        return string.replaceAll(
            "^\\s*(Vol [^\\(]*)?(([0-9]*[0-9]*\\.|[a-z]\\.|\\([^\\)]*\\))\\s*)*", ""
        );
    }
}
