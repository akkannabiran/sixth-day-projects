package com.sixthday.navigation.api.mappers;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.models.SisterSite;
import com.sixthday.navigation.api.models.response.BrandLinks;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BrandLinksMapper {
    public BrandLinks map(Map<CategoryDocument, List<CategoryDocument>> brandLinksCategories) {
        BrandLinks brandLinks = new BrandLinks();
        brandLinks.setSisterSites(brandLinksCategories.entrySet().stream().map(siterSiteCategoryEntry -> {
            CategoryDocument sisterSiteCategory = siterSiteCategoryEntry.getKey();
            List<CategoryDocument> sisterSiteTopCategories = siterSiteCategoryEntry.getValue();
            SisterSite sisterSite = new SisterSite();
            sisterSite.setName(sisterSiteCategory.getName());
            sisterSite.setUrl(sisterSiteCategory.getLongDescription());
            sisterSite.setTopCategories(sisterSiteTopCategories.stream().map(child ->
                    new SisterSite.TopCategory(child.getName(), child.getLongDescription())).collect(Collectors.toList()));
            return sisterSite;
        }).collect(Collectors.toList()));
        return brandLinks;
    }
}
