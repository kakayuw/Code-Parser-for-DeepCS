package parser;

import javafx.scene.paint.Stop;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaParser {

    final String notFound = "Mathch Not Found";

    public ParserOutput parseOne(String javaLine) throws Exception {
        System.out.println("Received line: " + javaLine);
        ParserOutput po = new ParserOutput();
        po.setInputLine(javaLine);
        javaLine = Util.preProcess(javaLine);
        System.out.println("Except for: " + javaLine);
        // return if contains annotation
//        if (javaLine.contains("@")) return po;
        javaLine = javaLine.replace('@', ' ');
        // find method name using re
        po.setMethodname(reFindMethodName(javaLine));
        // find apiseq using re
        po.setApiseq(reFindApiseq(javaLine, true));
        // find tokens using re
        po.setTokens(reFindTokens(javaLine));
        po.printStatus();
        System.out.println();
        return po;
    }

    public ParserOutput parseOneNoScreenOutput(String javaLine) throws Exception {
//        System.out.println("Received line: " + javaLine);
        ParserOutput po = new ParserOutput();
        po.setInputLine(javaLine);
        javaLine = Util.preProcess(javaLine);
//        System.out.println("Except for: " + javaLine);
        // return if contains annotation
        javaLine = javaLine.replace('@', ' ');
        // find method name using re
        po.setMethodname(reFindMethodName(javaLine));
        // find apiseq using re
        po.setApiseq(reFindApiseq(javaLine, true));
        // find tokens using re
        po.setTokens(reFindTokens(javaLine));
//        po.printStatus();
//        System.out.println();
        return po;
    }

    public ParserOutput parseOnePureBody(String javaLine) throws Exception {
        javaLine = javaLine.replace("\n" , "\t");
        System.out.println("Received line: " + javaLine);
        ParserOutput po = new ParserOutput();
        po.setInputLine(javaLine);
        try{
            javaLine = javaLine + ";";
            javaLine = Util.preProcess(javaLine);
            System.out.println("Except for: " + javaLine);
            javaLine = javaLine.replace('@', ' ');
            po.setMethodname(reFindMethodName(javaLine));
            // find apiseq using re
            po.setApiseq(reFindApiseq(javaLine, false));
            // find tokens using re
            po.setTokens(reFindTokens(javaLine));
        } catch (Exception e) {
            System.err.println(e.getMessage());
//            e.printStackTrace();
        }
        po.printStatus();
        System.out.println();
        return po;
    }

    private String reFindMethodName(String input) {
        // use regular expression to search: find method name
        String mnPatten = "(([A-Z]|[a-z]|$|_|\\.|\\[|\\]|\\<|\\>|\\d)+)( +)(([A-Z]|[a-z]|$|_|\\.|\\d)+)( *)\\(.*?\\)";
        Pattern mnr = Pattern.compile(mnPatten);
        Matcher mnm = mnr.matcher(input);
        String methodname = notFound;
        while (mnm.find()) {
            methodname = mnm.group();
            //System.out.println("Found value: " + methodname);
            if ( ! methodname.equals(notFound)) break;
        }
        if (methodname.equals(notFound)) {  // in case not return value
            methodname = input;
        }
//        System.out.println("method name:" + methodname);
        methodname = methodname.trim();
        int leftParen = methodname.indexOf('(');
        int start = methodname.indexOf(' ') > leftParen ? 0 : methodname.indexOf(' ') + 1;
        methodname =  methodname.substring(start, leftParen);
        return CamelCase2Underline(methodname).replace('_', ' ');
    }

    public List<String> reFindApiseq(String line, boolean hasBrace) {
        // remove method name
        if (hasBrace) {
            Pattern inBracePattern = Pattern.compile("\\{(.*?)\\}(\\s)*$");
            Matcher inBraceMatcher = inBracePattern.matcher(line);
            line = inBraceMatcher.find() ? inBraceMatcher.group(): line;
        }
        // extract sequence in the brace
        String apiPattern = "(([A-Z]|[a-z]|\\d|$|_|\\.|\\[|\\]|\\<|\\>)+)( *)\\(.*?\\)[^;|\\{|\\}]*(;|\\{|\\])+";
        List<String> apiseq = new ArrayList<String>();
        Pattern apir = Pattern.compile(apiPattern);
        Matcher apim = apir.matcher(line);
        while (apim.find()) {
            String onecall = apim.group();
//            System.out.println("FOUND API:" + onecall);
            apiseq.addAll(findNestedApi(onecall));
        }
        // prepare apiseq: continuous call and 'new' method
        apiseq = extractContinuousCall(line, apiseq);

        return apiseq;
    }

    /**
     * Use regular expression to catch tokens;
     * @param line
     * @return
     */
    public static Set<String> reFindTokens(String line) {
        // remove method name
        Pattern inBracePattern = Pattern.compile("\\{(.*?)\\}(\\s)*$");
        Matcher inBraceMatcher = inBracePattern.matcher(line);
        line = inBraceMatcher.find() ? inBraceMatcher.group(): line;
        // extract sequence in the brace
        String apiPattern = "([A-Z]|[a-z]|$|_)+";
        Set<String> rawTokens = new HashSet<String>();
        Pattern rawPtn = Pattern.compile(apiPattern);
        Matcher rawMatch = rawPtn.matcher(line);
        while (rawMatch.find()) {
            String rawToken = rawMatch.group();
//            System.out.println("Found token:" + rawToken);
            rawTokens.add(rawToken);
        }
        return extractTokens(rawTokens);
    }



    /**
     * Used in <reFindApiseq>
     * It find apis in nested method call in parenthesis and filtered out keyword like 'if' and 'for'
     * @param rawcode
     * @return apiseq
     */
    public List<String> findNestedApi(String rawcode) {
//        System.out.println("rawocode:" + rawcode);
        List<String> apiseqs = new ArrayList<String>();
        // parse outer method with parenthesis
        rawcode = rawcode.trim();
        String keyword = Util.getKeyword(rawcode);
//        System.out.println("keyword:"+ keyword);
        if (rawcode.indexOf('(') < 0) {  // pure code without any parenthesis
//            System.err.println("raw:" +rawcode);
//            System.err.println("key:" +keyword);

        } else if(keyword.equals("if")||keyword.equals("while") || keyword.equals("switch")) {
            apiseqs.addAll(findNestedApi(Util.extractFromParen(rawcode)));
        } else if(keyword.equals("for")){
            rawcode = Util.extractFromParen(rawcode);
            for(String s : rawcode.split(";")) {
                apiseqs.addAll(findNestedApi(s));
            }
        } else if (keyword.equals("catch")) {
            return apiseqs;
        } else if(keyword.equals(",")) {    // parse each parameter
            List<String> rawcodeList = Util.splitStringWithComma(rawcode);
            for(String method: rawcodeList) {
                apiseqs.addAll(findNestedApi(method));
            }
        } else {    // normal method call
            if(rawcode.indexOf(';') > 0)  rawcode = rawcode.substring(0, rawcode.lastIndexOf(';'));
            List<String> rawcodeList = Util.splitStringWithParen(rawcode);
            for(String method: rawcodeList) {
                apiseqs.addAll(findNestedApi(method));
            }
            if (rawcodeList.size() == 0) {  // only one method call without any '.'
                List<String> innerCodeApiseq = findNestedApi(Util.extractFromParen(rawcode));
                Matcher tmpMch = Pattern.compile("^([A-Z]|[a-z]|$|_|\\d|\\.|\\<|\\>)+$").matcher(keyword.trim());
                if (tmpMch.find()) {    // test real function name
                    String foreTrunc = rawcode.indexOf(keyword) >= 0 ? rawcode.substring(0, rawcode.indexOf(keyword)) : "";
//                    System.out.println("Trunc:" + foreTrunc);
                    String methodname = keyword;
                    if(Pattern.compile("(^|\\W)new\\s+$").matcher(foreTrunc).find())
                        methodname = methodname + ".new";
//                    System.out.println("RE METHODNAME:" + methodname);
                    apiseqs.addAll(innerCodeApiseq);
                    apiseqs.add(methodname);
                }
            }
        }
        return apiseqs;
    }

    /**
     * Extract tokens from raw token sets:
     * 1. remove stop words
     * 2. split words according to camel names
     * @param raw
     * @return extracted token set
     */
    private static Set<String> extractTokens(Set<String> raw) {
        // construct stop words
        final List<String> stopWords = Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with",
                "exception", "return", "if", "else", "final",
                "new", "int", "double", "string", "list", "array", "set", "hash",
                "try", "while", "catch", "synchronized", "exception",
                "void", "public", "private",
                "null"
        );
        // split camel case and generate derivatives
        Set<String> tokens = new HashSet<String>();
        for (String word : raw) {
            for(String deri: CamelCase2Underline(word).split("_")){
                deri = deri.toLowerCase().trim();
                if (!stopWords.contains(deri) && deri.length() > 1) // remove pure letters
                    tokens.add(deri);
            }
        }
//        System.out.println("raw:");
//        for(String rt:raw) {
//            System.out.print(" " + rt);
//        }
//        System.out.println();
//        System.out.println("new:");
//        for(String rt:tokens) {
//            System.out.print(" " + rt);
//        }
//        System.out.println();
        return tokens;
    }

    /**
     * Convert haml to underline
     * @example
     * input: "camelCaseToUnderline"
     * output:"camel_case_to_underline"
     * @param para
     * @return
     */
    public static String CamelCase2Underline(String para) {
//        System.out.println(para);
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;
        Boolean beforeLowerCase = false;
        if (!para.contains("_")) {
            for (int i = 0; i < para.length(); i++) {
                if (Character.isUpperCase(para.charAt(i)) && beforeLowerCase) {
                    sb.insert(i + temp, "_");
                    temp += 1;
                }
                if(Character.isLowerCase(para.charAt(i)))
                    beforeLowerCase = true;
                else beforeLowerCase = false;
            }
        }
        return sb.toString().toLowerCase();
    }


    /**
     * Expand method call name and replace variable name
     * @param apiseq
     * @return full API sequence
     */
    private static List<String> extractContinuousCall(String oriLine, List<String> apiseq) {
        System.out.println("api" + apiseq);
        for (int i = 0; i < apiseq.size(); i++) {
            String api = apiseq.get(i);
            if (api.equals("return")) continue;
            if (api.charAt(0) == '.') {
                String head = null;
                if (i == 0) {
                    Matcher matcher = Pattern.compile("\\W\\(((\\w|\\.)+)\\s+\\)(\\w+)\\)\\s*$").matcher(oriLine.substring(0, oriLine.indexOf(api)));
                    if (matcher.find()) head = matcher.group(1);
                } else {
                    String lastApi = apiseq.get(i - 1);
                    head = lastApi.contains(".") ? lastApi.substring(0, lastApi.lastIndexOf('.')) : lastApi;
                }

                apiseq.set(i, head + api);
            }
            api = apiseq.get(i);
            if ( Character.isLowerCase(api.charAt(0)) && api.contains(".")) {
                String variable = api.substring(0, api.indexOf('.'));
                Matcher mcr = Pattern.compile("(^|\\W)((\\w|\\.)+)\\s+" + variable + "\\W").matcher(oriLine);
                boolean found = mcr.find();
                if (found && !mcr.group(2).contains("new")) {
                    apiseq.set(i, mcr.group(2) + api.substring(api.indexOf('.')));
                } else if (found && mcr.group(2).contains("new")) {
                    apiseq.set(i, api + ".new");
                }
            } else if (Character.isUpperCase(api.charAt(0)) && !api.contains(".")) {
                apiseq.set(i, api + ".new");
            }

        }
        return apiseq;
    }
}
