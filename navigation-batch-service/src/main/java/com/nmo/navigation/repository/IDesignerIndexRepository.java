package com.sixthday.navigation.repository;

import com.sixthday.model.serializable.designerindex.DesignerIndex;

public interface IDesignerIndexRepository {
  
  DesignerIndex get(String key);
  
  void save(DesignerIndex designerIndex);
  
}
