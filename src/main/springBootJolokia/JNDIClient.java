package springBootJolokia;

import javax.naming.Context;
import javax.naming.InitialContext;

// 参考 https://blog.csdn.net/chenchaofuck1/article/details/51558995

public class JNDIClient {
    public static void main(String[] args) throws Exception {
        String uri = "rmi://127.0.0.1:1389/Object";
        Context ctx = new InitialContext();
        ctx.lookup(uri);
    }
}