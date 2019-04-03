package parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserOutput {

    private String inputLine;
    private List<String> apiseq;
    private Set<String> tokens;
    private String methodname;

    public ParserOutput() {
        this.inputLine = "";
        this.apiseq = new ArrayList<String>();
        this.tokens = new HashSet<String>();
        this.methodname = "";
    }
    public String getInputLine() { return inputLine;  }

    public void setInputLine(String inputLine) {  this.inputLine = inputLine; }

    public Set<String> getTokens() {
        return tokens;
    }

    public void setTokens(Set<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getApiseq() {
        return apiseq;
    }

    public void setApiseq(List<String> apiseq) {
        this.apiseq = apiseq;
    }

    public String getMethodname() {
        return methodname;
    }

    public void setMethodname(String methodname) {
        this.methodname = methodname;
    }

    public String getInBraceLine(String line) {
        // remove method name
        Pattern inBracePattern = Pattern.compile("\\{(.*?)\\}(\\s)*$");
        Matcher inBraceMatcher = inBracePattern.matcher(line);
        line = inBraceMatcher.find() ? inBraceMatcher.group(1): line;
        return line;
    }

    public void printStatus() {
        System.out.println("Method Name:" + methodname);
        System.out.print("API seq:");
        for (String api : apiseq) {
            System.out.print(" " + api);
        }
        System.out.print("\n");
        System.out.print("Tokens: " );
        for (String token : tokens) {
            System.out.print(" " +token);
        }
        System.out.println();
    }

    public void printFile(BufferedWriter bw) throws IOException {
        bw.write("Received Java Code:" + inputLine);
        bw.newLine();
        bw.write("Method Name:" + methodname);
        bw.newLine();
        bw.write("API seq:");
        for (String api : apiseq) {
            bw.write(" " + api);
        }
        bw.newLine();
        bw.write("Tokens:" );
        for (String token : tokens) {
            bw.write(" " +token);
        }
        bw.newLine();
        bw.newLine();
    }

    /**
     * generate DeepCS test file for evaluation
     * @param methnameBw
     * @param apiseqBw
     * @param tokensBw
     * @throws IOException
     */
    public void generateTestFile(BufferedWriter methnameBw, BufferedWriter apiseqBw, BufferedWriter tokensBw) throws IOException {
        methnameBw.write(methodname);
        methnameBw.newLine();
        for (String api : apiseq) {
            apiseqBw.write(" " + api);
        }
        apiseqBw.newLine();
        for (String token : tokens) {
            tokensBw.write(" " +token);
        }
        tokensBw.newLine();
    }
}
