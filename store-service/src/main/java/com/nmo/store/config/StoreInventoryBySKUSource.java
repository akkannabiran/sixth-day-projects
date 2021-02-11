package com.sixthday.store.config;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface StoreInventoryBySKUSource {
  @Output("storeInventoryBySKU")
  MessageChannel storeInventoryBySKUChannel();
}
