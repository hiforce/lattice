package org.hiforce.lattice.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2020/6/25
 */
@Configuration
@SpringBootApplication
@ImportResource(value = {"classpath*:spring/spring-*.xml"})
public class LatticeTestStarter {

    public static void main(String[] args) {
        SpringApplication.run(LatticeTestStarter.class, args);
    }
}
