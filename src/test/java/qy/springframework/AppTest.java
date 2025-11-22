package qy.springframework;

import org.junit.Test;
import qy.client.PublicClassUtil;
import qy.client.RemoteClassFinder;
import qy.test.BlogService;
import qy.test.BlogServiceImpl;
import qy.test.UserService;
import qy.test.UserServiceImpl;

import javax.swing.*;

public class AppTest {

    @Test
    public void invokeServer1() throws InterruptedException {
        PublicClassUtil.publishClass(new UserServiceImpl());
        while (true) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void invokeServer2() throws InterruptedException {
        PublicClassUtil.publishClass(new BlogServiceImpl());
        while (true) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void invokeClient1() throws InterruptedException, ClassNotFoundException {
        UserService userService = RemoteClassFinder.create(UserService.class);
        System.out.println(userService.getUserId());

        BlogService blogService = RemoteClassFinder.create(BlogService.class);
        System.out.println(blogService.getName(2));
    }
}
