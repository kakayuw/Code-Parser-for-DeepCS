package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) {
        String raw = "\t  int lastInt=-1;\t  for (  Integer currentInt : pages) {\t    for(int i = 0; i < lenght; i++){ for  (int h:k){dfdf}} if (lastInt != currentInt) {\t      result.add(currentInt);\t    }\t    lastInt=currentInt;\t  }\t  int[] arrayResult=new int[result.size()];";
        String other = "Returns {@code n} choose {@code k}, also known as the binomial coefficient of {@code n} and{@code k}, that is, {@code n!  (k! (n - k)!)}.\n";
        String r = "Returns the smallest power of two greater than or equal to {@code x}. This is equivalent to{@code BigInteger.valueOf(2).pow(log2(x, CEILING))}.\n";
        tst(other);

    }

    public static void tst(String test) {
        String line = "";//#received line of javacode
        JavaParser parser = new JavaParser();
        ParserOutput parseResult = parser.parseOne(line);
        String methname = parseResult.getMethodname();
        List<String> APIsequence = parseResult.getApiseq();
        Set<String> tokens = parseResult.getTokens();
        parseResult.generateTestFile();
    }
}
