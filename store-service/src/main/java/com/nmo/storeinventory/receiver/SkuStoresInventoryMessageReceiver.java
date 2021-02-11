package com.sixthday.storeinventory.receiver;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sixthday.store.handlers.SkuStoresInventoryMessageHandler;
import com.rabbitmq.client.Channel;

@Component
public class SkuStoresInventoryMessageReceiver implements ChannelAwareMessageListener {
  
  @Autowired
  private SkuStoresInventoryMessageHandler handler;
  
  public void onMessage(Message message, Channel channel) {
    handler.handle(message);
  }
  
}
