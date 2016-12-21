package com.chen.yarn.common;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import com.chen.yarn.common.Event.EventType;

public class Dispatcher {
  private BlockingQueue<Event> eventQueue;
  private Thread eventHandlerThread;
  private Map<EventType, EventHandler> handlerTable;
  private volatile boolean running = false;
  public Dispatcher(){
    eventQueue = new LinkedBlockingQueue<Event>();
    handlerTable = new ConcurrentHashMap<EventType, EventHandler>();
    eventHandlerThread = new Thread(new Runnable() {
      public void run() {
        try {
          while(running){
            Event event = eventQueue.take();
            EventType type = event.getType();
            if(handlerTable.containsKey(type)){
              handlerTable.get(type).handle(event);
            }
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }
  
  public void registerHandler(EventType type, EventHandler handler){
    handlerTable.put(type, handler);
  }
  
  public void produce(Event event){
    try {
      eventQueue.put(event);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  public void start(){
    if(!running){
      this.running = true;
      eventHandlerThread.start();
    }
  }
  
  public void stop(){
    this.running = false;
  }
}
