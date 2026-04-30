package ru.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EwmMainServerTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodTest() {
        EwmMainServer.main(new String[] {});
    }
}
