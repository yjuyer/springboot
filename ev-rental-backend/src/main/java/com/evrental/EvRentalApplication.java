package com.evrental;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 新能源汽车租赁管理平台 - 启动类
 *
 * @author ev-rental
 */
@SpringBootApplication
@MapperScan("com.evrental.**.mapper")
@EnableScheduling
public class EvRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvRentalApplication.class, args);
        System.out.println("===== 新能源汽车租赁管理平台启动成功 =====");
        System.out.println("===== API文档: http://localhost:8080/doc.html =====");
    }

    /**
     * 启动时打印 admin123 的真实 BCrypt 哈希，
     * 方便在数据库中直接用此值 UPDATE 管理员密码。
     * 用完可以删除此 Bean。
     */
    @Bean
    public CommandLineRunner printAdminHash() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode("admin123");
            System.out.println("==================================================");
            System.out.println("[Init] BCrypt hash for 'admin123':");
            System.out.println(hash);
            System.out.println("请用以下 SQL 重置 admin 密码：");
            System.out.println("UPDATE sys_user SET password = '" + hash + "' WHERE username = 'admin';");
            System.out.println("==================================================");
        };
    }
}
