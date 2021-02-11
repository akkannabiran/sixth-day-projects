package com.sixthday.store.config;

import com.sixthday.store.toggles.Features;
import com.toggler.core.utils.FeatureToggleRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class GotWWWBeanConfigurationTest {

    @Mock
    private RealGotWWWConfig realGotWWWConfig;
    @Mock
    private StubGotWWWConfig stubGotWWWConfig;
    @InjectMocks
    private GotWWWBeanConfiguration gotWWWBeanConfiguration;
    @Rule
    public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();

    @Test
    public void shouldReturnStubGotWWWConfigBeanIfToggleIsTrue() {
        featureToggleRepository.enable(Features.STUB_GOT_WWW);

        GotWWWConfig gotWWWConfig = gotWWWBeanConfiguration.getGotWWWConfig();

        assertThat(stubGotWWWConfig, is(gotWWWConfig));
    }

    @Test
    public void shouldReturnRealGotWWWRepositoryBeanIfToggleIsFalse() {
        featureToggleRepository.disable(Features.STUB_GOT_WWW);

        GotWWWConfig gotWWWConfig = gotWWWBeanConfiguration.getGotWWWConfig();

        assertThat(realGotWWWConfig, is(gotWWWConfig));
    }
}