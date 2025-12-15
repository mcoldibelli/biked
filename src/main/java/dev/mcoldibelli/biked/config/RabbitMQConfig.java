package dev.mcoldibelli.biked.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String TELEMETRY_QUEUE = "telemetry.queue";
  public static final String TELEMETRY_EXCHANGE = "telemetry.exchange";
  public static final String TELEMETRY_ROUTING_KEY = "telemetry.data";

  @Bean
  public Queue telemetryQueue() {
    return new Queue(TELEMETRY_QUEUE, true);
  }

  @Bean
  public TopicExchange telemetryExchange() {
    return new TopicExchange(TELEMETRY_EXCHANGE);
  }

  @Bean
  public Binding telemetryBinding(Queue telemetryQueue, TopicExchange telemetryExchange) {
    return BindingBuilder
        .bind(telemetryQueue)
        .to(telemetryExchange)
        .with(TELEMETRY_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

}
