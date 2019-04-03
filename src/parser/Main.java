package parser;

import java.io.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        JavaParser jp = new JavaParser();
        String filepath = ConfigUtil.javaTxtPath1;
        try (FileReader reader = new FileReader(filepath);
             BufferedReader br = new BufferedReader(reader)
        ) {
            // write split file;

            File testMethname = new File(ConfigUtil.testMethname);
            File testApiseq = new File(ConfigUtil.testApiseq);
            File testTokens = new File(ConfigUtil.testTokens);
            BufferedWriter methnameBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testMethname)));
            BufferedWriter apiseqBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testApiseq)));
            BufferedWriter tokensBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testTokens)));

            String line;
            int count = 0;

//            List <String> tst = jp.findNestedApi(" return x.bitLength() <= Long.SIZE - 1;");
//            for(String s : tst) {
//                System.out.println("API:" + s);
//            }
//            String test = "public static Calendar toCalendar(final Date date) { final Calendar c = Calendar.getInstance();  c.setTime(date);    return c; }";
//            jp.parseOne(test);
            while ((line = br.readLine()) != null) {
                count ++;
//                bw.write("ID : " + count);
//                bw.newLine();
//                if(count != 619) continue;
                System.out.println("ID : " + count);
                jp.parseOne(line).generateTestFile(methnameBw, apiseqBw, tokensBw);
            }
            apiseqBw.close();
            methnameBw.close();
            tokensBw.close();
        } catch (IOException e) {
            System.err.println("read errors :" + e);
            e.printStackTrace();
        }

    }

}
