package parser;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Util {


    public static void main(String[] args) {
        String test = "@Override public void onScanFinished(List<BleDevice> bleDeviceList){\t  if (mBleScanPresenter.ismNeedConnect()) {\t    final BleScanAndConnectCallback callback=(BleScanAndConnectCallback)mBleScanPresenter.getBleScanPresenterImp();\t    if (bleDeviceList == null || bleDeviceList.size() < 1) {\t      if (callback != null) {\t        callback.onScanFinished(null);\t      }\t    }\t else {\t      if (callback != null) {\t        callback.onScanFinished(bleDeviceList.get(0));\t      }\t      final List<BleDevice> list=bleDeviceList;\t      new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){\t        @Override public void run(){\t          BleManager.getInstance().connect(list.get(0),callback);\t        }\t      }\t,100);\t    }\t  }\t else {\t    BleScanCallback callback=(BleScanCallback)mBleScanPresenter.getBleScanPresenterImp(new arraylist() {@overload public static fuck(){ int i = 0;}});\t    if (callback != null) {\t      callback.onScanFinished(bleDeviceList);\t    }\t  }\t}";
        System.out.println("INPUT:" +test);
//        List<String> ls = splitStringWithParen(test);
//        for(String s : ls) {
//           System.out.println(s);
//        }
//        System.out.println( killOverload(test));
        killOverload(test);
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
        Boolean inRefer = false;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '(' && ! inRefer) {
                countParen++;
                if (countParen == 1)
                    start = i;
            } else if (str.charAt(i) == '"'){
              inRefer = ! inRefer;
            } else if (str.charAt(i) == ')' && !inRefer) {
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
        Pattern p = Pattern.compile("([A-Z]|[a-z]|$|_|\\.|\\d)+\\s*\\(");
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
     * 1. parse keyword FOR
     * 2. parse Overload
     * 3. parse Double quotation marks
     * @param raw
     * @return
     */
    public static String preProcess(String raw) {
        String proceed = raw;
        proceed = killOverload(proceed);
        proceed = killRefer(proceed);
        proceed = inner4out(proceed);
        return proceed;
    }

    public static String inner4out(String raw) {
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
        Boolean hasfor = raw.split("for").length > 0;
        String str1 = raw.substring(left+1, right) + (hasfor?"; ":"");
        String str2 = raw.substring(0, left+1);
        String str3 = raw.substring(right);
        return str1 + str2 + str3;
    }

    private static String killRefer(String raw) {
        Boolean inRefer = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '"')
                inRefer = ! inRefer;
            else if (!inRefer) {
                sb.append(raw.charAt(i));
            }
        }
        return sb.toString();
    }

    private static String killOverload(String raw) {
        int braceCount = 0, codeStart = 0, continuePos = 0, braceStart = 0 ;
//        System.out.println("Total length:" + raw.length());
        Matcher m = Pattern.compile("(^|\\W)new\\s+(\\w|\\.)+\\s*\\(\\)\\s*\\{()").matcher(raw);
        List<String> pieces = new ArrayList<String>();
        while(m.find()) {
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
                        System.out.println("continuepoS:" + continuePos);
                        break;
                    }
                }
            }
            System.out.println("codestart:" + codeStart + " bracestart:" +braceStart);
            pieces.add(raw.substring(codeStart, braceStart) );
            codeStart = continuePos;
        }
        String concat = "";
        for (String s : pieces) concat += s;
        concat += raw.substring(continuePos);
//        System.out.println("concate:" + concat);
        return concat;
    }

}
