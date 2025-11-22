package qy.springframework;

import qy.client.RemoteClassFinder;
import qy.test.UserService;

public class InvokeClientTest {
    public static void main(String[] args){
        UserService userService = null;
        try {
            userService = RemoteClassFinder.create(UserService.class);
        } catch (InterruptedException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println(userService.getUserId());
    }
}
