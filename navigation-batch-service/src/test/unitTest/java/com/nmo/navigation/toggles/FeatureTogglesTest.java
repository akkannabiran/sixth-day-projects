package com.sixthday.navigation.toggles;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeatureTogglesTest {
  private FeatureToggles featureToggles =  new FeatureToggles();
  @Before
  public void setup() {
    ServiceFeatureToggleConfig serviceFeatureToggleConfig = new ServiceFeatureToggleConfig();
    Map<String, Boolean> toggles = new HashMap<>();
    toggles.put("TT", Boolean.TRUE);
    toggles.put("TF", Boolean.FALSE);
    toggles.put("TN", null);
    serviceFeatureToggleConfig.setToggles(toggles);
  }
  
  @Test
  public void shouldReturnTrueIfToggleIsEnabled() {
    assertTrue(featureToggles.isEnabled("TT"));
  }
  
  @Test
  public void shouldReturnFalseIfToggleIsDisabled() {
    assertFalse(featureToggles.isEnabled("TF"));
  }
  
  @Test
  public void shouldReturnFalseIfToggleIsMisconfigured() {
    assertFalse(featureToggles.isEnabled("TN"));
  }
  
  @Test
  public void shouldReturnFalseIfToggleIsNotConfigured() {
    assertFalse(featureToggles.isEnabled("TU"));
  }
}
