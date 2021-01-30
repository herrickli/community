package com.cheng.community;

import com.cheng.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Test
    public void testMail(){
        mailClient.sendMail("2372125790@qq.com", "test", "test");
    }
}
