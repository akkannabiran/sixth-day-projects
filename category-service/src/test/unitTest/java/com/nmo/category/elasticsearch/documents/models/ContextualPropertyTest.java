package com.sixthday.category.elasticsearch.documents.models;

import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ContextualPropertyTest {
    @InjectMocks
    private ContextualProperty contextualProperty;

    @Test
    public void shouldAssertContextualProperties() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");

        contextualProperty = ContextualProperty.builder()
                .childCategoryOrder(childCategoryOrder)
                .desktopAlternateName("some desktop alternate name")
                .driveToSubcategoryId("some drive to sub category ID")
                .mobileAlternateName("some mobile alternate name")
                .parentId("parentCategoryId")
                .redText(false)
                .build();

        assertThat(contextualProperty.getChildCategoryOrder(), is(childCategoryOrder));
        assertThat(contextualProperty.getDesktopAlternateName(), is("some desktop alternate name"));
        assertThat(contextualProperty.getDriveToSubcategoryId(), is("some drive to sub category ID"));
        assertThat(contextualProperty.getMobileAlternateName(), is("some mobile alternate name"));
        assertThat(contextualProperty.getParentId(), is("parentCategoryId"));
        assertThat(contextualProperty.isRedText(), is(false));
    }

    @Test
    public void shouldUseHtmlUnescapeToFormatDesktopAlternateName() {
        ContextualProperty contextualProperty = ContextualProperty.builder()
                .desktopAlternateName("Herm&#232;s")
                .build();

        assertThat(contextualProperty.getDesktopAlternateName(), is("Hermès"));
    }
}
