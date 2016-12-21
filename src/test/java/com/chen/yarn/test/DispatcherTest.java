package com.chen.yarn.test;

import com.chen.yarn.common.Dispatcher;
import com.chen.yarn.common.Event;
import com.chen.yarn.common.Event.EventType;
import com.chen.yarn.common.EventHandler;

public class DispatcherTest {
	public static void main(String[] args) {
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.registerHandler(EventType.TEST, new EventHandler(){
			public void handle(Event event) {
				System.out.println(event);
			}
		});
		dispatcher.start();
		for(int i = 0; i < 10; i++){
			dispatcher.produce(new Event(EventType.TEST));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
