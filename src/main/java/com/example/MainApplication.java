package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.example.camel.routes.AggregateEventsCamelRoute;
import com.example.domain.Event;

/**
 * PoC about Apache Camel Aggregate
 * @author iarroyoescobar
 *
 */
@ComponentScan("com.example")
@Component
public class MainApplication {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
		CamelContext camelContext = new DefaultCamelContext(new ApplicationContextRegistry(ctx));

		try {
			camelContext.start();
			AggregateEventsCamelRoute.init(camelContext);

			ProducerTemplate template = camelContext.createProducerTemplate();

			CustomThread thread1 = new CustomThread(template);
			thread1.addEvent(new Event("event1"));
			Thread.sleep(100);

			CustomThread thread2 = new CustomThread(template);
			thread2.addEvent(new Event("event2"));
			Thread.sleep(100);

			CustomThread thread3 = new CustomThread(template);
			thread3.addEvent(new Event("event3"));
			Thread.sleep(100);

			thread2.run();
			thread3.run();
			thread1.run();

			Thread.sleep(10000);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static class CustomThread extends Thread {

		private ProducerTemplate producerTemplate;
		private List<Event> events = new ArrayList<>();

		public CustomThread(ProducerTemplate template) {
			this.producerTemplate = template;
		}

		public CustomThread(ProducerTemplate producerTemplate, List<Event> events) {
			this.producerTemplate = producerTemplate;
			this.events.addAll(events);
		}

		public void addEvent(Event event) {
			this.events.add(event);
		}

		@Override
		public void run() {

			Map<String, Object> headers = new HashMap<>();
			headers.put("correlationId", "same-correlation-id");
			events.forEach(event -> {
				producerTemplate.sendBodyAndHeaders(AggregateEventsCamelRoute.ROUTE_DIRECT_URI, event, headers);
				try {
					Thread.sleep(1350);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

		}
	}

}
