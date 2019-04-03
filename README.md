# Code-Parser-for-DeepCS
Use regular expression to distill method name, method API call sequence and method code snippet from given raw java code

### Function
This tool is to parse line of java code and generate parsed result that includes method name, API sequence and tokens the snippet covers.
More Detail could be seen via [Deep Code Search](https://guxd.github.io/papers/deepcs.pdf "2018 ICSE").
This paper will introduce what make up of API sequence and tokens as well as why they are important.

### Usage
Demo code:
```
    String line = (#received line of javacode)
    JavaParser parser = new JavaParser();
    ParserOutput parseResult = parser.parseOne(line);
    String methname = parseResult.getMethodname();
    List<String> APIsequence = parseResult.getApiseq();
    Set<String> tokens = parseResult.getTokens();
    parseResult.generateTestFile();
```
### Implementation
To diretly use regular expression to catch API sequence may meet with some obstacle. The most obvious obstacle is the expression in parenthesis after keyword `while` and `for`,(e.g. in code snippet `for (int i = 0; i < vector.size(); i++)`) since there are much different syntax structure from others. Another crux of this problem is that we may find nested method call in some parenthesis, like `str.replace("a", "b").trim()`. Others may be like `thread = new Thread{ @Override run(){} }`.

So for eaze to use regular expression to extract API with parenthesis feature, I first preprocess the text: 
  1. move all snippet after keyword out of (), e.g. `for (int i = 0; i < vector.size(); i++)` => `int i = 0; i < vector.size(); i++; for ()`, since this would not break sequence final consistency; 
  2. replace CallBack with method call in string, since they would be reduct to one method call;
  3. erase all string content in double quote or sincle quote cause they contribute nothing to API sequence
  
Then I use regular expression to catch method names, nested APIs and tokens with their corresponding feature regarding parenthesis. And the fact proofs that these work can nearly satisfy my requirement.
