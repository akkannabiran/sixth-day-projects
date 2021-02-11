package com.sixthday.navigation.elasticsearch.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.sixthday.model.serializable.designerindex.DesignerByCategory;
import com.sixthday.model.serializable.designerindex.DesignerCategory;
import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.model.serializable.designerindex.DesignersByIndex;
import com.sixthday.navigation.NavigationBatchApplication;
import com.sixthday.navigation.repository.IDesignerIndexRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NavigationBatchApplication.class})
@DirtiesContext
public class DesignerRepositoryIntegrationTest {
    
    @Autowired
    private IDesignerIndexRepository designerIndexRepository;
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Test
    public void shouldReturnSavedDesignerIndex() {
        DesignerIndex actual = null;
        try {
            DesignerIndex expected = createDesignerIndexItem("designer1");
            designerIndexRepository.save(expected);

            actual = designerIndexRepository.get(expected.getId());

            assertThat(actual.getId(), is(expected.getId()));
            assertThat(actual.getDesignersByCategory().size(), is(expected.getDesignersByCategory().size()));
            assertThat(actual.getDesignersByCategory().get(0).getId(), is(expected.getDesignersByCategory().get(0).getId()));
            assertThat(actual.getDesignersByIndex().size(), is(expected.getDesignersByIndex().size()));
            assertThat(actual.getDesignersByIndex().get(0).getCategories().size(), is(expected.getDesignersByIndex().get(0).getCategories().size()));
        } finally {
            if (actual != null) {
                deleteDesignerIndexFromTable(actual);
            }
        }
    }

    @Test
    public void shouldReturnUpdatedDesignerIndexWithoutRemovedElements() {
        DesignerIndex actual = null;
        try {
            DesignerIndex expected = createDesignerIndexItem("designer1");
            designerIndexRepository.save(expected);

            expected.setName(expected.getName() + "_MODIFIED");
            expected.getDesignersByCategory().get(0).setName("NEW_NAME");
            expected.getDesignersByCategory().get(0).setUrl(null);
            expected.getDesignersByCategory().remove(1);
            expected.getDesignersByIndex().get(0).getCategories().get(0).getExcludedCountries().clear();
            expected.getDesignersByIndex().get(0).getCategories().get(1).getExcludedCountries().add("newCountry");
            designerIndexRepository.save(expected);

            actual = designerIndexRepository.get(expected.getId());

            assertThat(actual.getName(), is(expected.getName()));
            assertThat(actual.getDesignersByCategory().size(), is(expected.getDesignersByCategory().size()));
            assertThat(actual.getDesignersByCategory().get(0).getName(), is(expected.getDesignersByCategory().get(0).getName()));
            assertThat(actual.getDesignersByCategory().get(0).getUrl(), nullValue());
            assertThat(actual.getDesignersByIndex().size(), is(expected.getDesignersByIndex().size()));
            assertThat(actual.getDesignersByIndex().get(0).getCategories().get(0).getExcludedCountries().size(),
                is(expected.getDesignersByIndex().get(0).getCategories().get(0).getExcludedCountries().size()));
            assertThat(actual.getDesignersByIndex().get(0).getCategories().get(1).getExcludedCountries().size(),
                is(expected.getDesignersByIndex().get(0).getCategories().get(1).getExcludedCountries().size()));

        } finally {
            if (actual != null) {
                deleteDesignerIndexFromTable(actual);
            }
        }
    }

    private DesignerIndex createDesignerIndexItem(String prefix) {
        String designerIndex1Id = prefix + "_id";

        DesignerByCategory designerByCategory11 = DesignerByCategory.builder()
            .id(prefix + "_byCategory1_id")
            .name(prefix + "_byCategory1_name")
            .url(prefix + "_byCategory1_url")
            .build();
        DesignerByCategory designerByCategory12 = DesignerByCategory.builder()
            .id(prefix + "_byCategory2_id")
            .name(prefix + "_byCategory2_name")
            .url(prefix + "_byCategory2_url")
            .build();
        List<DesignerByCategory> designerByCategoryList = new ArrayList<>();
        designerByCategoryList.add(designerByCategory11);
        designerByCategoryList.add(designerByCategory12);

        List<String> excludedCountries1 = new ArrayList<>(2);
        excludedCountries1.add("excludedCountry_1");
        excludedCountries1.add("excludedCountry_2");
        DesignerCategory designerCategory1 = DesignerCategory.builder()
            .id(prefix + "_category1_id")
            .adornmentTag(prefix + "_category1_adornmentTag")
            .excludedCountries(excludedCountries1)
            .name(prefix + "_category1_name")
            .url(prefix + "_category1_url")
            .build();

        List<String> excludedCountries2 = new ArrayList<>(2);
        excludedCountries2.add("excludedCountry_3");
        excludedCountries2.add("excludedCountry_4");
        DesignerCategory designerCategory2 = DesignerCategory.builder()
            .id(prefix + "_category2_id")
            .adornmentTag(prefix + "_category2_adornmentTag")
            .excludedCountries(excludedCountries2)
            .name(prefix + "_category2_name")
            .url(prefix + "_category2_url")
            .build();

        List<DesignerCategory> designerCategories = new ArrayList<>(2);
        designerCategories.add(designerCategory1);
        designerCategories.add(designerCategory2);
        DesignersByIndex designersByIndex = DesignersByIndex.builder()
            .name(prefix + "_byIndex1_name")
            .categories(designerCategories)
            .build();

        List<DesignersByIndex> designersByIndexList = new ArrayList<>(1);
        designersByIndexList.add(designersByIndex);

        return DesignerIndex.builder()
            .id(designerIndex1Id)
            .name(prefix + "_name")
            .designersByCategory(designerByCategoryList)
            .designersByIndex(designersByIndexList)
            .build();
    }
    
    private void deleteDesignerIndexFromTable(DesignerIndex itemForDelete) {
        dynamoDBMapper.delete(itemForDelete);
    }
}
