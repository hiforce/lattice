package org.hiforce.lattice.test;

import org.hifforce.lattice.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Rocky Yu
 * @since 2022/9/24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LatticeTestStarter.class)
@AutoConfigureMockMvc
public class ErrorMessageTest {

    @Test
    public void test_error_code_01() {
        Message message = Message.code("LATTICE-CORE-RT-0014");

        System.out.println(message);

    }
}
