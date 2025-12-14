package dev.mcoldibelli.biked;

import dev.mcoldibelli.biked.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class BikedApplication {

  public static void main(String[] args) {
    SpringApplication.run(BikedApplication.class, args);
  }

}
