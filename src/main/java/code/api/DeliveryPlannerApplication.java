package code.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "code")
public class DeliveryPlannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryPlannerApplication.class, args);
    }
}
