package qy.springframework;

import qy.client.PublicClassUtil;
import qy.test.UserServiceImpl;

public class InvokeServerTest {
    public static void main(String[] args) throws InterruptedException {
        PublicClassUtil.publishClass(new UserServiceImpl());
        while (true) {
            Thread.sleep(1000);
        }
    }
}
