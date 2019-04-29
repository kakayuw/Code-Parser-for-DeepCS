package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) throws IOException {
        String dirPath = "C:\\Users\\kakay\\Desktop\\self_blog\\deep-code-search\\server_connection\\CheckBoxSquare.java_1877.txt";
        JavaParser jp = new JavaParser();

        String filepath = dirPath ;
        FileReader reader = new FileReader(filepath);
        BufferedReader br = new BufferedReader(reader);
        String line;
        int count = 0;
        List<ParserOutput.IndexedCode> codes = new ArrayList<>();
        FileWriter err = new FileWriter(new File("error.log"));

        while ((line = br.readLine()) != null) {
            count ++;
//            if (count != 28 )    continue;
                    System.out.println("id:" + count );
            try {
                try{
                    ParserOutput.IndexedCode code = jp.parseOneNoScreenOutput(line).generateParsedCode(count);
                    codes.add(code);
                } catch (Error e) {
                    err.write(count + " :" + e.getMessage() + "\n");
                }

            } catch (Exception e) {
                err.write(count + " :" + e.getMessage() + "\n");
            }
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
        FileWriter fw = new FileWriter(new File("result.json"));
        fw.write(gson.toJson(codes));
        fw.close();
        err.close();
    }

    public static void tst(String test) {
    }
}
