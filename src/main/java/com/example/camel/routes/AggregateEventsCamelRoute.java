package com.example.camel.routes;

import java.util.Calendar;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;

import com.example.domain.Event;

/**
 * 
 * @author iarroyoescobar
 *
 */
public class AggregateEventsCamelRoute {

	public static final String ROUTE_DIRECT_URI = "direct:aggregateEventCamelRoute";
	public static final String ROUTE_SEDA_URI = "seda:aggregateEventCamelRoute";

	public static void init(CamelContext camelContext) throws Exception {

		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from(ROUTE_DIRECT_URI).routeId("aggregateEvent.route").aggregate(header("correlationId"), new SampleAggregationStrategy(log))
						.completionTimeout(1000).process(e -> {
							Event event = e.getIn().getBody(Event.class);
							if (log.isInfoEnabled()) {
								log.info("Final Event Name >> " + event.getName());
							}
						}).end();
			}
		});

	}

	public static class SampleAggregationStrategy implements AggregationStrategy {

		private Logger log;
		public SampleAggregationStrategy(Logger log) {
			super();
			this.log=log;
		}
		
		@Override
		public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {

			if(oldExchange!=null){
				Event oldEvent = oldExchange.getIn().getBody(Event.class);
				log.info("Old event name >> " + oldEvent.getName());
				log.info("Old event date >> " + oldEvent.getDate().getTime());
				Event newEvent = newExchange.getIn().getBody(Event.class);
				log.info("New event name >> " + newEvent.getName());
				log.info("New event date >> " + newEvent.getDate().getTime());
				Calendar oldCalendar = Calendar.getInstance();
				oldCalendar.setTime(oldEvent.getDate());
				Calendar newCalendar = Calendar.getInstance();
				newCalendar.setTime(newEvent.getDate());
				
				if(oldCalendar.after(newCalendar)){
					return oldExchange;
				}
				
			}
			return newExchange;
		}
	}

}
