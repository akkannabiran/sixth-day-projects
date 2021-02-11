package com.sixthday.navigation.batch.designers;

import au.com.dius.pact.provider.junit.*;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.LeftNavUtilityController;
import com.sixthday.navigation.batch.designers.processor.DesignerIndexProcessor;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRestPactRunner.class)
@Provider("navigation-batch-service")
@Consumer("ctp-svc")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}",
        authentication = @PactBrokerAuth(username = "${pactbroker.username}", password = "${pactbroker.password}"))
public class DesignerIndexContractTest {
    @TestTarget
    public MockMvcTarget mockMvcTarget = new MockMvcTarget();

    @Mock
    private DesignerIndexProcessor designerIndexProcessor;
    @Mock
    private LeftNavTreeProcessor leftNavTreeProcessor;
    @Mock
    private LeftNavRepository leftNavRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final LeftNavUtilityController leftNavUtilityController = new LeftNavUtilityController(leftNavTreeProcessor, designerIndexProcessor, leftNavRepository);
        mockMvcTarget.setControllers(leftNavUtilityController);
    }

    @State("DesignerIndex")
    public void shouldReturnOkResponseWithCategoryTemplateTypePWhenValidCategoryIdIsSpecifiedInRequestParam() {
        final String categoryId = "cat000149";
        DesignerIndex designerIndex = DesignerIndexContractData.getDesignerIndex();
        when(designerIndexProcessor.getDesignerIndex(eq(categoryId))).thenReturn(designerIndex);
    }
}
