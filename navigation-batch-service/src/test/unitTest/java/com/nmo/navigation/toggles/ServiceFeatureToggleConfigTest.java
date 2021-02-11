package com.sixthday.navigation.toggles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceFeatureToggleConfigTest {
  @Test
  public void shouldGetAndSetToggles() {
    ServiceFeatureToggleConfig serviceFeatureToggleConfig = new ServiceFeatureToggleConfig();
    
    Map<String, Boolean> toggles = serviceFeatureToggleConfig.getToggles();
    assertEquals(toggles, ServiceFeatureToggleConfig.toggles);
    
    toggles = new HashMap<String, Boolean>();
    serviceFeatureToggleConfig.setToggles(toggles);
    assertEquals(toggles, ServiceFeatureToggleConfig.toggles);
  }

  @Test
  public void shouldEnableToggleWhenEnableMethodIsCalled() {
    ServiceFeatureToggleConfig.enable("Toggle1");
    assertTrue(ServiceFeatureToggleConfig.toggles.get("Toggle1"));
  }
  
  @Test
  public void shouldDisableToggleWhenDisableMethodIsCalled() {
    ServiceFeatureToggleConfig.disable("Toggle1");
    assertFalse(ServiceFeatureToggleConfig.toggles.get("Toggle1"));
  }
}
