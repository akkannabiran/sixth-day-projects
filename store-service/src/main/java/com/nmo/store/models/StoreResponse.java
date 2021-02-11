package com.sixthday.store.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreResponse {
  List<String> storeNumbers = new ArrayList<>();
}
