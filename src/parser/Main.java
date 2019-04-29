package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        JavaParser jp = new JavaParser();

        String dirPath = args[0];
        String outputDir = args[1];
//        String outputDir = "/mnt/sdb/yh/deepcs/data/";
//        String outputDir = "src/resources/";


//        String dirPath = "/mnt/sdb/heyq/javaSplit/";
//        String dirPath = "C:\\Users\\kakay\\Desktop\\self_blog\\deep-code-search\\server_connection\\javaSplits\\javaSplits\\";
        List<String> fnList = getFilenameList(dirPath);
        FileWriter errorFw = new FileWriter("ErrorParserLine.log");
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();;

        int fileCount = 0;
        FileWriter fw = null;
//        File testMethname = null, testApiseq = null, testTokens = null, methbody = null;
//        BufferedWriter methnameBw = null, apiseqBw = null, tokensBw = null, methbodyBw = null;
        for (String name: fnList) {
//            testMethname = new File(outputDir + name + "methname.txt");   // same as testApiseq testApiseq methbody
//            methnameBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testMethname))); // same as apiseqBw tokensBw methbodyBw
            fileCount ++;
            if (name.contains(".txt")) {
                System.out.println("Now parsing:" + name);
                String filepath = dirPath + name;
                FileReader reader = new FileReader(filepath);
                BufferedReader br = new BufferedReader(reader);
                String line;
                int count = 0;
                List<ParserOutput.IndexedCode> codes = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    count ++;
//                    System.out.println("id:" + count + " FILE:" + name);
                    try {
                        try {
                            codes.add(jp.parseOneNoScreenOutput(line).generateParsedCode(count));
                        } catch (Error err) {
//                            System.gc();
                            errorFw.write(name + " " + count + " RE_STACKOVERFLOW\n" );
                        }
                    } catch (Exception e) {
                        errorFw.write(name + " " + count + " " + e.getMessage() +  "\n");
                    }
                }
                System.out.println(name + " parsed over:" + fileCount + "/" + fnList.size() + "  length:" + codes.size());
                fw = new FileWriter(new File(outputDir + name + ".json"));
                fw.write(gson.toJson(codes));
                fw.flush();
                fw.close();
            }
            System.gc();
        }
        errorFw.close();
    }


    public static List<String> getFilenameList(String dirPath) {
        // scan target directory and get file list
        File[] allFiles = new File(dirPath).listFiles();
        List<String> filenameList = new ArrayList<String>();
        for (int i = 0; i < allFiles.length; i++) {
            File file = allFiles[i];
            if (file.isFile())  filenameList.add(file.getName());
        }
        return filenameList;
    }
}
