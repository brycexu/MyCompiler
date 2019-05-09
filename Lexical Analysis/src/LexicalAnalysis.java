import java.io.*;

public class LexicalAnalysis {

    public static void main(String[] args) throws Exception {
        // 从文件中读取测试程序
        File file = new File("src/Files/test.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        File result = new File("src/Files/Errors.dyd");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(result));
        File error = new File("src/Files/error.err");
        BufferedWriter bufferedWriter1 = new BufferedWriter(new FileWriter(error));
        String in = "";
        String str;
        // 字符串 in 用来存储测试程序的内容
        while ((str = bufferedReader.readLine()) != null) {
            // 在每行的末尾添加 EOLN 代表换行
            if (!in.equals(""))
                in += " EOLN ";
            in += str;
        }
        // 在程序的末尾添加 EOF 代表测试程序结束
        in += " EOF ";
        // 字符串 token 用来存放单词符号
        String token = "";
        // 布尔值 flag 用来判断当前字符和下一字符能否构成2位运算符或关系符
        Boolean flag = false;
        // int值 line 用来记录当前行号
        int line = 1;
        // 字符数组 chars 用来存放字符串 in 的每个字符值
        char[] chars = in.toCharArray();
        // 依次读取 chars 中的每个字符值
        for (int i = 0; i < chars.length; i++) {
            // 如果之前已经将2位运算符或关系符返回,则跳过这一步
            if (flag) {
                flag = false;
                continue;
            }
            // 字符值 ch 记录当前一步的字符值
            char ch = chars[i];
            // 如果当前字符值 ch 是字母或者数字,将它连接到 token
            if (Character.isLetter(ch) || Character.isDigit(ch)) {
                token = token + ch;
            }
            else if (!token.equals("")) {
                // 关键字 begin
                if (token.equals("begin")) {
                    System.out.println("           begin 01");
                    bufferedWriter.write("           begin 01\n");
                }
                // 关键字 end
                else if (token.equals("end")) {
                    System.out.println("             end 02");
                    bufferedWriter.write("             end 02\n");
                }
                // 关键字 integer
                else if (token.equals("integer")) {
                    System.out.println("         integer 03");
                    bufferedWriter.write("         integer 03\n");
                }
                // 关键字 if
                else if (token.equals("if")) {
                    System.out.println("              if 04");
                    bufferedWriter.write("              if 04\n");
                }
                // 关键字 then
                else if (token.equals("then")) {
                    System.out.println("            then 05");
                    bufferedWriter.write("            then 05\n");
                }
                // 关键字 else
                else if (token.equals("else")) {
                    System.out.println("            else 06");
                    bufferedWriter.write("            else 06\n");
                }
                // 关键字 function
                else if (token.equals("function")) {
                    System.out.println("        function 07");
                    bufferedWriter.write("        function 07\n");
                }
                // 关键字 read
                else if (token.equals("read")) {
                    System.out.println("            read 08");
                    bufferedWriter.write("            read 08\n");
                }
                // 关键字 write
                else if (token.equals("write")) {
                    System.out.println("           write 09");
                    bufferedWriter.write("           write 09\n");
                }
                // 关键字 EOLN
                else if (token.equals("EOLN")) {
                    line++;
                    System.out.println("            EOLN 24");
                    bufferedWriter.write("            EOLN 24\n");
                }
                // 关键字 EOF
                else if (token.equals("EOF")) {
                    System.out.println("             EOF 25");
                    bufferedWriter.write("             EOF 25");
                }
                else {
                    int spaceNumber = 16 - token.length();
                    // 标识符
                    if (Character.isLetter(token.charAt(0))) {
                        for (int j = 0; j < spaceNumber; j++) {
                            System.out.print(" ");
                            bufferedWriter.write(" ");
                        }
                        System.out.println(token + " 10");
                        bufferedWriter.write(token + " 10\n");
                    }
                    // 常量
                    if (Character.isDigit(token.charAt(0))) {
                        // 出错处理:非法标识符
                        boolean checkError = false;
                        for (int m = 0; m < token.length(); m++)
                            if (!Character.isDigit(token.charAt(m)))
                                checkError = true;
                        if (checkError) {
                            System.out.println("ERROR! LINE:" + line + "  非法标识符");
                            bufferedWriter1.write("ERROR! LINE:" + line + "  非法标识符\n");
                        } else {
                            for (int j = 0; j < spaceNumber; j++) {
                                System.out.print(" ");
                                bufferedWriter.write(" ");
                            }
                            System.out.println(token + " 11");
                            bufferedWriter.write(token + " 11\n");
                        }
                    }
                }
                // token 清空
                token = "";
                // retract 回退
                i = i - 1;
            }
            // 运算符 =
            else if (ch == '=') {
                System.out.println("               = 12");
                bufferedWriter.write("               = 12\n");
            }
            // 运算符 -
            else if (ch == '-') {
                System.out.println("               - 18");
                bufferedWriter.write("               - 18\n");
            }
            // 运算符 *
            else if (ch == '*') {
                System.out.println("               * 19");
                bufferedWriter.write("               * 19\n");
            }
            // 界符 (
            else if (ch == '(') {
                System.out.println("               ( 21");
                bufferedWriter.write("               ( 21\n");
            }
            // 界符 )
            else if (ch == ')') {
                System.out.println("               ) 22");
                bufferedWriter.write("               ) 22\n");
            }
            // 界符 ;
            else if (ch == ';') {
                System.out.println("               ; 23");
                bufferedWriter.write("               ; 23\n");
            }
            else if (ch == '<') {
                // ch2 记录下一位字符
                char ch2 = chars[i+1];
                // 2位界符 <>
                if (ch2 == '>') {
                    System.out.println("              <> 13");
                    bufferedWriter.write("              <> 13\n");
                    flag = true;
                }
                // 2位运算符 <=
                else if (ch2 == '=') {
                    System.out.println("              <= 14");
                    bufferedWriter.write("              <= 14\n");
                    flag = true;
                }
                // 1位运算符 <
                else {
                    System.out.println("               < 15");
                    bufferedWriter.write("               < 15\n");
                }
            }
            else if (ch == '>') {
                // ch2 记录下一位字符
                char ch2 = chars[i+1];
                // 2位运算符 >=
                if (ch2 == '=') {
                    System.out.println("              >= 16");
                    bufferedWriter.write("              >= 16\n");
                    flag = true;
                }
                // 1位运算符 >
                else {
                    System.out.println("               > 17");
                    bufferedWriter.write("               > 17\n");
                }
            }
            else if (ch == ':') {
                // ch2 记录下一位字符
                char ch2 = chars[i+1];
                // 2位运算符 :=
                if (ch2 == '=') {
                    System.out.println("              := 20");
                    bufferedWriter.write("              := 20\n");
                    flag = true;
                }
                // 出错处理:冒号不匹配
                else {
                    System.out.println("ERROR! LINE:" + line + "  冒号不匹配");
                    bufferedWriter1.write("ERROR! LINE:" + line + "  冒号不匹配\n");
                }
            }
            // 空格
            else if (ch == ' ') {
                continue;
            }
            // 出错处理:非法字符
            else {
                System.out.println("ERROR! LINE:" + line + "  非法字符");
                bufferedWriter1.write("ERROR! LINE:" + line + "  非法字符\n");
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
        bufferedWriter1.close();
    }
}
