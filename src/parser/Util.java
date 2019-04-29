package parser;
import com.sun.deploy.util.StringUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Util {


    public static void main(String[] args) throws Exception {
        String test =
" Iterator<Integer> iter = l.iterator();while (iter.hasNext()) {if (iter.next() == 5) {iter.remove();}}\n"
                ;
        System.out.println(test);
        JavaParser jp = new JavaParser();

            jp.parseOne(preProcess(test));

        System.out.println(test);
    }


    /**
     * Parse string and return split list with parenthesis reserved
     * @example
     * input: Toast.makeText(this,R.string.toast_pick_file_error,Toast.LENGTH_SHORT).show();
     * output: ['Toast.makeText(this,R.string.toast_pick_file_error,Toast.LENGTH_SHORT)', 'show()']
     * @return empty list if only contains one element; split list if contains multiple results
     */
    public static List<String> splitStringWithParen(String str) {
        int parenCount = 0, begin = 0;
        List<Integer> splitSpots = new ArrayList<Integer>();
        List<String> methodCalls = new ArrayList<String>();
        Boolean inStr = false;
        for(int i = 0, len = str.length(); i < len; i++){
            if(str.charAt(i) == '"') inStr = !inStr;
            else if(str.charAt(i) == '(' && !inStr) {
                if (parenCount == 0)  begin = i;
                parenCount ++;
            } else if (str.charAt(i) == ')' && !inStr) {
                parenCount --;
                if (parenCount == 0) {
                    splitSpots.add(i+1);
                    begin = i;
                }
            }
        }
        begin = 0;
        for (Integer it : splitSpots) {
            methodCalls.add(str.substring(begin, it.intValue()));
            begin = it.intValue() ;
        }
        if (methodCalls.size() == 1) return new ArrayList<String>();
        return methodCalls;
    }

    /**
     * Extract string format content from raw input;
     * @param str
     * @return
     */
    public static String extractFromParen(String str) {
//        System.out.println("raw str:" + str);
        int start = 0;
        int end = str.length();
        int countParen = 0;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                countParen++;
                if (countParen == 1)
                    start = i;
            } else if (str.charAt(i) == ')') {
                countParen --;
                if (countParen == 0)
                    end = i;
            }
        }

        return str.substring(start + 1, end);
    }

    /**
     * Get Method name from raw string;
     * Return keyword if contains keyword
     * Return ',' if contains multiple parameter
     * @param str
     * @return
     */
    public static String getKeyword(String str) {
        if(isParameters(str)) return ",";   // contains multiple expression
        String rawcode = str.indexOf('(') > 0 ? str.substring(0, str.indexOf('(')+1).trim() : str;
        Pattern p = Pattern.compile("([A-Z]|[a-z]|$|_|\\.|\\d|\\<|\\>|\\[|\\])+\\s*\\(");
        Matcher m = p.matcher(rawcode);
        int matchPos = -1;
        if(m.find()) {
            String tgt = m.group();
            tgt = tgt.substring(0, tgt.indexOf('('));
            rawcode = tgt.trim();
            matchPos = m.start();
//            System.out.println(matchPos);
        }
        if (matchPos >= 0) {
            Boolean inRefer = false;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '"')
                    inRefer = ! inRefer;
                if (i == matchPos && inRefer) {
                    return rawcode;
                }
            }
        }
//        System.out.println("key word:" + rawcode);
        return rawcode;
    }

    private static Boolean isParameters(String str) {
        Boolean validComma = true, inStr = false;
        int parenCount = 0;
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == '(')  parenCount ++;
            else if(str.charAt(i) == ')') parenCount --;
            else if (str.charAt(i) == '"') inStr = !inStr;
            else if(str.charAt(i) == ',' && !inStr) {
                if (parenCount > 0) validComma = validComma && true;    // comma in range of paren
                else if (parenCount == 0) validComma = validComma && false;  //comma out of range
            }
        }
        return  str.contains(",") && !validComma ;
    }


    /**
     * Split String with comma, str should be parameters
     * @example
     * input:"abc, a, d(b, c)"
     * output:"['abc', 'a', 'd(b,c)']"
     * @param raw
     * @return split list
     */
    public static List<String> splitStringWithComma(String raw) {
        List<String> splitList = new ArrayList<String>();
        List<Integer> splitSpots = new ArrayList<Integer>();
        int parenCount = 0;
        Boolean inStr = false;
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '(') parenCount ++;
            else if (raw.charAt(i) == ')') parenCount --;
            else if (raw.charAt(i) == '"') inStr = !inStr;
            else if (raw.charAt(i) == ',' && parenCount == 0 && !inStr) splitSpots.add(i);
        }
        int begin = 0;
        for(int i = 0; i < splitSpots.size(); i++) {
            splitList.add(raw.substring(begin, splitSpots.get(i).intValue()).trim());
            begin = splitSpots.get(i).intValue() + 1;
        }
        splitList.add(raw.substring(begin).trim());
        if(splitList.size() == 1) return new ArrayList<String>();
        return splitList;
    }

    /**
     * preprocess java line string for
     * 1. parse Double quotation marks
     * 2. parse lambda
     * 3. parse keyword FOR
     * 4. parse Overload
     * @param raw
     * @return
     */
    public static String preProcess(String raw) throws Exception {
        String proceed = raw;
        proceed = killRefer(proceed);
//        System.out.println("killRefer");
        proceed = killTooLongBrace(proceed);
//        System.out.println("kill too long brace" + proceed);
        proceed = killLambda(proceed);
//        System.out.println("kill lambda:" + proceed);
        proceed = killClosedBrace(proceed);
//        System.out.println("proceed = killClosedBrace(proceed);");
//        System.out.println(proceed);
        proceed = killForceCast(proceed);
//        System.out.println("proceed = killForceCast");
//        System.out.println(proceed);

        proceed = killOverload(proceed);
//        System.out.println("proceed = killOverload(proceed);");
        proceed = inner4out(proceed);
//        System.out.println("proceed = inner4out(proceed);");

        proceed = killSpace(proceed);
        return proceed;
    }

    public static String inner4out(String raw) {
//        System.out.println("inner4out:" + raw);

        Pattern p = Pattern.compile("\\s+for\\s*\\((.*?)\\)");
        Matcher m = p.matcher(raw);
        List<Integer> splitSpot = new ArrayList<Integer>();
        while(m.find()){
//            System.out.println("start:" + m.start());
//            System.out.println(raw.substring(m.start()));
            splitSpot.add(m.start());
        }
        List<String> splits = new ArrayList<String>();
        int begin = 0;
        for(Integer it : splitSpot) {
            splits.add(raw.substring(begin, it.intValue()));
            begin = it.intValue() + 1;
        }
        splits.add(raw.substring(begin));
        String all = "";
        for(int i = 0; i < splits.size(); i++) {
//            System.out.println("splits:" + s);
            if(i == 0) {
                all = splits.get(i);
            }else {
                all += innerForOut(splits.get(i));
            }
        }
//        System.out.println("all:\n" + all);
        return all;
    }

    private static String innerForOut(String raw) {
//        raw = innerBracketOut(raw);
//        System.out.println(raw);
        int innerCount = 0, left = raw.indexOf('('), right = -1;
        for(int i = 0; i < raw.length(); i++) {
            if(raw.charAt(i) == '(') {
                innerCount ++;
            }else if (raw.charAt(i) == ')') {
                innerCount --;
            }
            if(innerCount == 0 && i > left) {
                right = i;
                break;
            }
        }
        Boolean hasfor = raw.split("\\sfor\\(").length > 0;
        String str1 = raw.substring(left+1, right) + (hasfor?"; ":"");
        String str2 = raw.substring(0, left+1);   // remove ":"
        String str3 = raw.substring(right);
//        System.out.println(str1 + str2 + str3);
        return str1 + str2 + str3;
    }

    private static String killSpace(String raw) {
        StringBuffer sb = new StringBuffer();
//        for(int i = 0; i < raw.length();i ++) {
//            char c = raw.charAt(i);
//            if (c == '.') {
//                while(sb.charAt(sb.length()-1) == ' ');
//
//            }
//        }
        Matcher matcher = Pattern.compile("\\.(\\s+)").matcher(raw);
        return matcher.replaceFirst(".");
    }

    private static String killRefer(String raw) throws Exception {
        Boolean inRefer = false;
        Boolean bound = false;
        StringBuffer sb = new StringBuffer();
        char quoteType = '?';
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '\\') {
                if (inRefer) {
                    i++;
                    continue;
                }
            }else if (raw.charAt(i) == '"' || raw.charAt(i) == '\'') {
                if (quoteType == '?') {     // init quoteType
                    quoteType = raw.charAt(i);
                    if (inRefer) bound = true;
                    inRefer = !inRefer;
                } else if (raw.charAt(i) == quoteType) {
                    if (inRefer) bound = true;
                    inRefer = !inRefer;
                }
            } else if (!inRefer) {
                if (bound) {
                    quoteType = '?';
                    sb.append("String");
                    bound = false;
                }
                sb.append(raw.charAt(i));
            } else if (inRefer && i == raw.length()-1)
                throw new Exception("QUOTE_NOT_COMPLETED");
        }
        raw = sb.toString();
        return raw;
    }

    /*
     * Too long brack initialization may cause stackoverflow error
     * {1,2,3,4} => {int}
     * {"a", "b", "c"} => {String}
     */
    private static String killTooLongBrace(String raw) {
        // in case of RE StackOverflowError
        while(raw.contains("String, String, String, String")) {
            raw = raw.replaceAll("String, String, String, String", "String");
        }
        raw = replacePairAnno(raw, '{', '}', "{CONTENT}");
        return raw;
    }


    /*
     * To judge whether a string in brace contains invalid substring;
     * Return true if string in brace is method snippet or code snippet
     * Return false if is matrix or array initialization
     */
    private static Boolean invalidInbraceString(String raw) {
        final List<String> stopChars = Arrays.asList(
                "(", ")", ";", "->", "=",
                "exception", "return", "else", "final",
                "while", "catch", "synchronized", "exception",
                "public", "private",
                "new"
        );
        for (String word : stopChars) {
            if (raw.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private static String reduceTokens(String raw) {
        StringTokenizer st = new StringTokenizer(raw, ",");
        StringBuffer concatBf = new StringBuffer();
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            concatBf.append(token);
        }
        return concatBf.toString();
    }

    private static String killOverload(String raw) throws Exception {
//        System.out.println(raw);
        raw = killMatrix(raw);
        // begin remove new defnition
        int braceCount = 0, codeStart = 0, continuePos = 0, braceStart = 0 ;
//        System.out.println("Total length:" + raw.length());
        Matcher m = Pattern.compile("(^|\\W)new\\s+(\\w|\\.|\\<|\\>)+\\s*\\((\\w|\\s|,|\\.)*\\)\\s*\\{()").matcher(raw);
        List<String> pieces = new ArrayList<String>();
        while(m.find()) {
//            System.out.println("ffffff");
            braceStart = raw.indexOf('{', m.start());
            if (braceStart < codeStart) continue;
//            System.out.println("FOUND GROUP:" + m.group());
            for (int i = braceStart; i < raw.length(); i++) {
                if (raw.charAt(i) == '{')
                    braceCount++;
                else if (raw.charAt(i) == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        continuePos = i + 1;
//                        System.out.println("continuepoS:" + continuePos);
                        break;
                    }
                }
            }
//            System.out.println("codestart:" + codeStart + " bracestart:" +braceStart);
            pieces.add(raw.substring(codeStart, braceStart) );
            codeStart = continuePos;
        }
        String concat = "";
        for (String s : pieces) concat += s;
        concat += raw.substring(continuePos);
//        System.out.println("concate:" + concat);
        return concat;
    }

    private static String killLambda(String input) {
//        System.out.println(input);
//        String lastInput = input;
        while(input.contains("->")) {
            Matcher lambda = Pattern.compile("\\-\\>").matcher(input);
            int lambdaPos = lambda.find() ? lambda.start() : 0;
            int leftParen = 0, rightParen = 0;
            for (int i = lambdaPos, parenCount = 0; i > 0 ; i --) {
                if (input.charAt(i) == ')') {
                    parenCount ++;
                } else if (input.charAt(i) == '(') {
                    parenCount --;
                    if (parenCount < 0) {
                        leftParen = i;
                        break;
                    }
                }
            }
            for (int i = lambdaPos, parenCount = 0; i < input.length() ; i ++) {
                if (input.charAt(i) == '(') {
                    parenCount ++;
                } else if (input.charAt(i) == ')') {
                    parenCount --;
                    if (parenCount < 0) {
                        rightParen = i;
                        break;
                    }
                }
            }
            // get left "=" or and right {}
            int leftEqual = 0, rightBrace = 0, braceCount = 0;
            String beforeLmd = input.substring(0, lambdaPos + 1);
            leftEqual = Math.max(beforeLmd.lastIndexOf("return") > 0 ? beforeLmd.lastIndexOf("return") + 6: 0, beforeLmd.lastIndexOf("="));
            leftEqual = leftEqual < 0 ? 0 : leftEqual;
            for (int i = lambdaPos; i < input.length(); i++){
                if (input.charAt(i) == '{') {
                    braceCount ++;
                } else if (input.charAt(i) == '}') {
                    braceCount --;
                    if (braceCount == 0) {
                        rightBrace = i;
                        break;
                    }
                } else if (input.charAt(i) == ';' && braceCount == 0) {
                    rightBrace = i;
                    break;
                }
            }
//            System.out.println("left:" + leftParen + " right:" + rightParen + " pos:" + lambdaPos);
            if (leftParen > 0 && leftParen < rightParen)
                input = input.substring(0, leftParen + 1) + input.substring(rightParen);
            else if (leftParen * rightParen == 0) {
                input = input.substring(0, leftEqual + 1) + " BRACE " + input.substring(rightBrace +1);
            }
//            System.out.println(input);
//            if (lastInput.equals(input)) return input.replace("->", ";");
//            lastInput = input;
        }
//                    System.out.println(input);

        return input;
    }

    /*
     * xxxx{} => xxxx
     */
    public static String killClosedBrace(String input) {
        input = killString(input);
//        System.out.println(input);
        // remove all container def in  angle bracket
        input = replacePairAnno(input, '<', '>', " ");
        // remove all bracket   // TODO: whether save method call in bracket
        input = input.replaceAll("\\[(.*?)\\]", "");

//        System.out.println(input);
        return input.replace("{}", "");
    }

    /*
     * ((String) x).xxx => String.xxx
     */
    public static String killForceCast(String raw) {
//        System.out.println(raw);
        Pattern pattern = Pattern.compile("\\(\\(((\\w|\\.|\\<|\\?|\\>)+)\\)\\s*(\\w|\\.)+\\)\\.");
        Matcher matcher = pattern.matcher(raw);
        while (matcher.find()) {
//            System.out.println(matcher.group(1));
            raw = matcher.replaceFirst(matcher.group(1) + ".");
            matcher = pattern.matcher(raw);
//            System.out.println(raw);
        }

        // remove string forcast
        pattern = Pattern.compile("\\s+\\(String\\)\\.");
        matcher = pattern.matcher(raw);
        while (matcher.find()) {
//            System.out.println(matcher.group(1));
            raw = matcher.replaceFirst(" String.");
            matcher = pattern.matcher(raw);
//            System.out.println(raw);
        }
        return raw;
    }


    /*
     * new String[]{INDEX_NAME_2} => new String
     */
    public static String killString(String raw){
//        System.out.println(raw);
        raw = raw.replaceAll("\\w+\\[\\]\\s*\\{(\\w|,|\\s|\\(|\\)|\\.)+\\}", " ");
//        System.out.println(raw);
        return raw;
    }

    /*
     * triangleCount[record.getBitmask().getValue()] => triangleCount+record.getBitmask().getValue()+
     */
    public static String innerBracketOut(String raw) {
        return raw.replace("[", "+").replace("]", "+");
    }

    public static String killMatrix(String raw) throws Exception {
        abortNotClosedError(raw);
        // remove new
//        System.out.println("raw:" + raw);
        Pattern bracePtn = Pattern.compile("\\Wnew\\s+(\\w|\\.)+\\s*\\{");
        Matcher braceMch = bracePtn.matcher(raw);
        while(braceMch.find()) {
//            System.out.println("infinate loop");
            int braceCount = 0, beginPos = 0, endPos = 0;
            int pos = braceMch.start() ;
            String substr = raw.substring(pos);
            int begin = substr.indexOf('{'), end = substr.indexOf('}');
            for (int i = 0; i < substr.length(); i++) {
                if (substr.charAt(i) == '{') {
                    braceCount ++;
                    if (braceCount == 1) beginPos = i;
                } else if (substr.charAt(i) == '}') {
                    braceCount --;
                    if (braceCount == 0) endPos = i;
                }
            }
            raw = raw.substring(0, pos) + substr.substring(0, beginPos) + ".new()" + substr.substring(endPos+1);
//            System.out.println(raw);
            braceMch = bracePtn.matcher(raw);
//            System.out.println("after:" + raw);

        }
//        // remove matrix init
//        Pattern pattern = Pattern.compile("\\{(,|\\w|\\s|\\+|\\.|-)+}");
//        Matcher matcher = pattern.matcher(raw);
//        while (matcher.find()) {
////            System.out.println("group:" + matcher.group());
//            raw = matcher.replaceFirst(" BRACE ");
//            matcher = pattern.matcher(raw);
//        }
////        System.out.println(raw);
        return raw;
    }


    /**
     * Replace pair of annotation and their content with other custom string
     * @param raw
     * @return
     */
    public static String replacePairAnno(String raw, char annoLeft, char annoRight ,String replacement) {
        int pos = raw.indexOf(annoLeft);
        while (pos > 0) {
            int braceCount = 0, rightBrace = 0;
            for (int i = pos; i < raw.length(); i++) {
                if (raw.charAt(i) == annoLeft) {
                    braceCount ++;
                } else if (raw.charAt(i) == annoRight) {
                    braceCount --;
                    if (braceCount == 0) {
                        rightBrace = i +1;
                        break;
                    }
                }
            }
            if (rightBrace > 0 &&  !invalidInbraceString(raw.substring(pos, rightBrace)))
//                System.out.println(raw.substring(pos, rightBrace));
                raw = raw.substring(0, pos) + replacement + raw.substring(rightBrace);
//            System.out.println(pos + " " + rightBrace);
            pos = raw.indexOf(annoLeft, pos+1);
        }
        return raw;
    }


    public static void abortNotClosedError(String raw) throws Exception {
        int braceCount = 0;
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '{') braceCount ++;
            else if (raw.charAt(i) == '}') braceCount --;
            if (i == raw.length()-1 && braceCount > 0) {
                throw  new Exception("BRACE_NOT_CLOSED");
            }
        }
    }

    public static String join(Collection var0, String var1) {
        StringBuffer var2 = new StringBuffer();
        for(Iterator var3 = var0.iterator(); var3.hasNext(); var2.append((String)var3.next())) {
            if (var2.length() != 0) {
                var2.append(var1);
            }
        }
        return var2.toString();
    }
}

