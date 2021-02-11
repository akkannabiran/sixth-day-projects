package com.sixthday.navigation.batch.designers.processor;

import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.repository.dynamodb.DesignerIndexRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.sixthday.navigation.batch.designers.processor.DesignerIndexProcessor.BOUTIQUE_TEXT_ADORNMENT_OVERRIDE;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DesignerIndexProcessorTest {

    @Mock
    DesignerIndexRepository designerIndexRepository;
    @InjectMocks
    private DesignerIndexProcessor designerIndexProcessor;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    @Mock
    private LeftNavTreeProcessor leftNavTreeProcessor;
    @Captor
    private ArgumentCaptor<DesignerIndex> designerIndexArgumentCaptor;

    @Before
    public void before() {
        when(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId()).thenReturn("designer");
        when(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerByCategory()).thenReturn("designerByCategory");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("live")).thenReturn("live");
        when(leftNavTreeProcessor.getCategoryDocument(eq("designer"))).thenReturn(getDesignerCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("designerByCategory"))).thenReturn(getDesignerByCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("women"))).thenReturn(getWomenCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("men"))).thenReturn(getMenCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("kids"))).thenReturn(getKidsCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("sale"))).thenReturn(getSaleCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("gucci"))).thenReturn(getGucciCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("a001"))).thenReturn(getA001CategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("a002"))).thenReturn(getA002CategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("a003"))).thenReturn(getA003CategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("a004"))).thenReturn(getA004CategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("bally"))).thenReturn(getBallyCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("lancer"))).thenReturn(getLancerCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("tai"))).thenReturn(getTaiCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("9thAve"))).thenReturn(get9thAveCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("luna"))).thenReturn(getLunaCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("noneBoutique"))).thenReturn(getNonBoutiqueCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("hidden"))).thenReturn(getHiddenCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("notExists"))).thenReturn(Optional.empty());
        when(leftNavTreeProcessor.getCategoryDocument(eq("noResults"))).thenReturn(getNoResultsCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("women_designer"))).thenReturn(getWomenDesignerCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("men_designer"))).thenReturn(getMenDesignerCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("kids_designer"))).thenReturn(getKidsDesignerCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("sale_designer"))).thenReturn(getSaleDesignerCategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("cat1"))).thenReturn(Optional.empty());
        when(leftNavTreeProcessor.getCategoryDocument(eq("cat2"))).thenReturn(Optional.empty());
        when(leftNavTreeProcessor.getCategoryDocument(eq("cat3"))).thenReturn(getCat3CategoryDocument());
        when(leftNavTreeProcessor.getCategoryDocument(eq("cat4"))).thenReturn(getCat4CategoryDocument());
    }

    @Test
    public void testDesignerIndexBuiltForEverySiloDesigners() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals(allValues.get(0).getId(), "women_designer");
        assertEquals(allValues.get(1).getId(), "men_designer");
        assertEquals(allValues.get(2).getId(), "kids_designer");
        assertEquals(allValues.get(3).getId(), "sale_designer");
        assertEquals(allValues.get(4).getId(), "designer");
    }

    @Test
    public void testDesignerIndexAlternateNameForSiloDrawerAllDesigners() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();
        assertEquals(allValues.size(), 5);
        assertEquals(allValues.get(0).getName(), "Women");
        assertEquals(allValues.get(1).getName(), "Men");
    }

    @Test
    public void testDesignerIndexDefaultNameForSiloDrawerAllDesigners() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();
        assertEquals(allValues.size(), 5);
        assertEquals(allValues.get(2).getName(), "All Designer");
        assertEquals(allValues.get(3).getName(), "Sale");
    }

    @Test
    public void testDesignerIndexNameForDesignerDrawer() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertNull(allValues.get(4).getName());
    }

    @Test
    public void testDesignerByCategoryIsAvailableInAllObjects() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals(allValues.get(0).getDesignersByCategory().size(), 5);
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getId(), "designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getName(), "VIEW ALL A-Z");
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getUrl(), "//designers");
        assertEquals(allValues.get(0).getDesignersByCategory().get(1).getId(), "women_designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(1).getName(), "Women");
        assertEquals(allValues.get(0).getDesignersByCategory().get(1).getUrl(), "//women");
        assertEquals(allValues.get(0).getDesignersByCategory().get(2).getId(), "men_designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(2).getName(), "Men");
        assertEquals(allValues.get(0).getDesignersByCategory().get(2).getUrl(), "//men");
        assertEquals(allValues.get(0).getDesignersByCategory().get(3).getId(), "kids_designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(3).getName(), "All Designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(3).getUrl(), "//kids");
        assertEquals(allValues.get(0).getDesignersByCategory().get(4).getName(), "Sale");
        assertEquals(allValues.get(0).getDesignersByCategory().get(4).getUrl(), "//sale");

        assertEquals(allValues.get(1).getDesignersByCategory().size(), 5);

        assertEquals(allValues.get(2).getDesignersByCategory().size(), 5);

        assertEquals(allValues.get(3).getDesignersByCategory().size(), 5);

        assertEquals(allValues.get(4).getDesignersByCategory().size(), 5);
    }

    @Test
    public void testDesignerByIndexIsAvailableInKidsSilos() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals("All Designer", allValues.get(2).getName());
        assertEquals("kids_designer", allValues.get(2).getId());
        assertEquals(allValues.get(2).getDesignersByIndex().size(), 27);
        assertEquals(allValues.get(2).getDesignersByIndex().get(0).getName(), "A");
        assertEquals(allValues.get(2).getDesignersByIndex().get(0).getCategories().size(), 0);

        assertEquals(allValues.get(2).getDesignersByIndex().get(1).getName(), "B");
        assertEquals(allValues.get(2).getDesignersByIndex().get(1).getCategories().size(), 1);
        assertEquals(allValues.get(2).getDesignersByIndex().get(1).getCategories().get(0).getId(), "bally");
        assertEquals(allValues.get(2).getDesignersByIndex().get(1).getCategories().get(0).getName(), "Bally");
        assertEquals(allValues.get(2).getDesignersByIndex().get(1).getCategories().get(0).getUrl(), "//bally?navpath=live_designer_bally");
        assertEquals(allValues.get(2).getDesignersByIndex().get(1).getCategories().get(0).getExcludedCountries(), Collections.singletonList("AU"));
        assertNull(allValues.get(2).getDesignersByIndex().get(1).getCategories().get(0).getAdornmentTag());

        assertEquals(allValues.get(2).getDesignersByIndex().get(2).getName(), "C");
        assertEquals(allValues.get(2).getDesignersByIndex().get(2).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(3).getName(), "D");
        assertEquals(allValues.get(2).getDesignersByIndex().get(3).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(4).getName(), "E");
        assertEquals(allValues.get(2).getDesignersByIndex().get(4).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(5).getName(), "F");
        assertEquals(allValues.get(2).getDesignersByIndex().get(5).getCategories().size(), 0);

        assertEquals(allValues.get(2).getDesignersByIndex().get(6).getName(), "G");
        assertEquals(allValues.get(2).getDesignersByIndex().get(6).getCategories().size(), 1);
        assertEquals(allValues.get(2).getDesignersByIndex().get(6).getCategories().get(0).getId(), "gucci");
        assertEquals(allValues.get(2).getDesignersByIndex().get(6).getCategories().get(0).getName(), "Gucci");
        assertEquals(allValues.get(2).getDesignersByIndex().get(6).getCategories().get(0).getUrl(), "//gucci?navpath=live_designer_gucci");
        assertNull(allValues.get(2).getDesignersByIndex().get(6).getCategories().get(0).getExcludedCountries());
        assertNull(allValues.get(2).getDesignersByIndex().get(6).getCategories().get(0).getAdornmentTag());

        assertEquals(allValues.get(2).getDesignersByIndex().get(7).getName(), "H");
        assertEquals(allValues.get(2).getDesignersByIndex().get(7).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(8).getName(), "I");
        assertEquals(allValues.get(2).getDesignersByIndex().get(8).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(9).getName(), "J");
        assertEquals(allValues.get(2).getDesignersByIndex().get(9).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(10).getName(), "K");
        assertEquals(allValues.get(2).getDesignersByIndex().get(10).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(11).getName(), "L");
        assertEquals(allValues.get(2).getDesignersByIndex().get(11).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(12).getName(), "M");
        assertEquals(allValues.get(2).getDesignersByIndex().get(12).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(13).getName(), "N");
        assertEquals(allValues.get(2).getDesignersByIndex().get(13).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(14).getName(), "O");
        assertEquals(allValues.get(2).getDesignersByIndex().get(14).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(15).getName(), "P");
        assertEquals(allValues.get(2).getDesignersByIndex().get(15).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(16).getName(), "Q");
        assertEquals(allValues.get(2).getDesignersByIndex().get(16).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(17).getName(), "R");
        assertEquals(allValues.get(2).getDesignersByIndex().get(17).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(18).getName(), "S");
        assertEquals(allValues.get(2).getDesignersByIndex().get(18).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(19).getName(), "T");
        assertEquals(allValues.get(2).getDesignersByIndex().get(19).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(20).getName(), "U");
        assertEquals(allValues.get(2).getDesignersByIndex().get(20).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(21).getName(), "V");
        assertEquals(allValues.get(2).getDesignersByIndex().get(21).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(22).getName(), "W");
        assertEquals(allValues.get(2).getDesignersByIndex().get(22).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(23).getName(), "X");
        assertEquals(allValues.get(2).getDesignersByIndex().get(23).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(24).getName(), "Y");
        assertEquals(allValues.get(2).getDesignersByIndex().get(24).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(25).getName(), "Z");
        assertEquals(allValues.get(2).getDesignersByIndex().get(25).getCategories().size(), 0);
        assertEquals(allValues.get(2).getDesignersByIndex().get(26).getName(), "#");
        assertEquals(allValues.get(2).getDesignersByIndex().get(26).getCategories().size(), 0);
    }

    @Test
    public void testDesignerByIndexIsAvailableInDesignersSilos() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);

        assertNull(allValues.get(4).getName());
        assertEquals("designer", allValues.get(4).getId());
        assertEquals(allValues.get(4).getDesignersByIndex().size(), 27);
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getName(), "A");
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().size(), 0);

        assertEquals(allValues.get(4).getDesignersByIndex().get(1).getName(), "B");
        assertEquals(allValues.get(4).getDesignersByIndex().get(1).getCategories().size(), 1);
        assertEquals(allValues.get(4).getDesignersByIndex().get(1).getCategories().get(0).getId(), "bally");
        assertEquals(allValues.get(4).getDesignersByIndex().get(1).getCategories().get(0).getName(), "Bally");
        assertEquals(allValues.get(4).getDesignersByIndex().get(1).getCategories().get(0).getUrl(), "//bally?navpath=live_designer_bally");
        assertNull(allValues.get(4).getDesignersByIndex().get(1).getCategories().get(0).getAdornmentTag());
        assertEquals(allValues.get(4).getDesignersByIndex().get(1).getCategories().get(0).getExcludedCountries(), Collections.singletonList("AU"));

        assertEquals(allValues.get(4).getDesignersByIndex().get(2).getName(), "C");
        assertEquals(allValues.get(4).getDesignersByIndex().get(2).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(3).getName(), "D");
        assertEquals(allValues.get(4).getDesignersByIndex().get(3).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(4).getName(), "E");
        assertEquals(allValues.get(4).getDesignersByIndex().get(4).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(5).getName(), "F");
        assertEquals(allValues.get(4).getDesignersByIndex().get(5).getCategories().size(), 0);

        assertEquals(allValues.get(4).getDesignersByIndex().get(6).getName(), "G");
        assertEquals(allValues.get(4).getDesignersByIndex().get(6).getCategories().size(), 1);
        assertEquals(allValues.get(4).getDesignersByIndex().get(6).getCategories().get(0).getId(), "gucci");
        assertEquals(allValues.get(4).getDesignersByIndex().get(6).getCategories().get(0).getName(), "Gucci");
        assertEquals(allValues.get(4).getDesignersByIndex().get(6).getCategories().get(0).getUrl(), "//gucci?navpath=live_designer_gucci");
        assertNull(allValues.get(4).getDesignersByIndex().get(6).getCategories().get(0).getAdornmentTag());
        assertNull(allValues.get(4).getDesignersByIndex().get(6).getCategories().get(0).getExcludedCountries());

        assertEquals(allValues.get(4).getDesignersByIndex().get(7).getName(), "H");
        assertEquals(allValues.get(4).getDesignersByIndex().get(7).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(8).getName(), "I");
        assertEquals(allValues.get(4).getDesignersByIndex().get(8).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(9).getName(), "J");
        assertEquals(allValues.get(4).getDesignersByIndex().get(9).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(10).getName(), "K");
        assertEquals(allValues.get(4).getDesignersByIndex().get(10).getCategories().size(), 0);

        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getName(), "L");
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().size(), 2);
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(0).getId(), "lancer");
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(0).getName(), "Lancer");
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(0).getUrl(), "//lancer?navpath=live_designer_lancer");
        assertNull(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(0).getAdornmentTag());
        assertNull(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(0).getExcludedCountries());
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(1).getId(), "luna");
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(1).getName(), "Luna");
        assertEquals(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(1).getUrl(), "//luna?navpath=live_designer_luna");
        assertNull(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(1).getAdornmentTag());
        assertNull(allValues.get(4).getDesignersByIndex().get(11).getCategories().get(1).getExcludedCountries());

        assertEquals(allValues.get(4).getDesignersByIndex().get(12).getName(), "M");
        assertEquals(allValues.get(4).getDesignersByIndex().get(12).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(13).getName(), "N");
        assertEquals(allValues.get(4).getDesignersByIndex().get(13).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(14).getName(), "O");
        assertEquals(allValues.get(4).getDesignersByIndex().get(14).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(15).getName(), "P");
        assertEquals(allValues.get(4).getDesignersByIndex().get(15).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(16).getName(), "Q");
        assertEquals(allValues.get(4).getDesignersByIndex().get(16).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(17).getName(), "R");
        assertEquals(allValues.get(4).getDesignersByIndex().get(17).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(18).getName(), "S");
        assertEquals(allValues.get(4).getDesignersByIndex().get(18).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(19).getName(), "T");
        assertEquals(allValues.get(4).getDesignersByIndex().get(19).getCategories().size(), 1);
        assertEquals(allValues.get(4).getDesignersByIndex().get(19).getCategories().get(0).getId(), "tai");
        assertEquals(allValues.get(4).getDesignersByIndex().get(19).getCategories().get(0).getName(), "Tai");
        assertEquals(allValues.get(4).getDesignersByIndex().get(19).getCategories().get(0).getUrl(), "//tai?navpath=live_designer_tai");
        assertNull(allValues.get(4).getDesignersByIndex().get(19).getCategories().get(0).getAdornmentTag());
        assertNull(allValues.get(4).getDesignersByIndex().get(19).getCategories().get(0).getExcludedCountries());

        assertEquals(allValues.get(4).getDesignersByIndex().get(20).getName(), "U");
        assertEquals(allValues.get(4).getDesignersByIndex().get(20).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(21).getName(), "V");
        assertEquals(allValues.get(4).getDesignersByIndex().get(21).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(22).getName(), "W");
        assertEquals(allValues.get(4).getDesignersByIndex().get(22).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(23).getName(), "X");
        assertEquals(allValues.get(4).getDesignersByIndex().get(23).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(24).getName(), "Y");
        assertEquals(allValues.get(4).getDesignersByIndex().get(24).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(25).getName(), "Z");
        assertEquals(allValues.get(4).getDesignersByIndex().get(25).getCategories().size(), 0);
        assertEquals(allValues.get(4).getDesignersByIndex().get(26).getName(), "#");
        assertEquals(allValues.get(4).getDesignersByIndex().get(26).getCategories().size(), 1);
        assertEquals(allValues.get(4).getDesignersByIndex().get(26).getCategories().get(0).getId(), "9thAve");
        assertEquals(allValues.get(4).getDesignersByIndex().get(26).getCategories().get(0).getName(), "9ThAve");
        assertEquals(allValues.get(4).getDesignersByIndex().get(26).getCategories().get(0).getUrl(), "//9thAve?navpath=live_designer_9thAve");
        assertNull(allValues.get(4).getDesignersByIndex().get(26).getCategories().get(0).getAdornmentTag());
        assertNull(allValues.get(4).getDesignersByIndex().get(26).getCategories().get(0).getExcludedCountries());
    }

    @Test
    public void testDesignerByIndexIsAvailableInWomen() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals("Women", allValues.get(0).getName());
        assertEquals("women_designer", allValues.get(0).getId());

        assertEquals(allValues.get(0).getDesignersByIndex().size(), 27);
        assertEquals(allValues.get(0).getDesignersByIndex().get(0).getName(), "A");
        assertEquals(allValues.get(0).getDesignersByIndex().get(0).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(1).getName(), "B");
        assertEquals(allValues.get(0).getDesignersByIndex().get(1).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(2).getName(), "C");
        assertEquals(allValues.get(0).getDesignersByIndex().get(2).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(3).getName(), "D");
        assertEquals(allValues.get(0).getDesignersByIndex().get(3).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(4).getName(), "E");
        assertEquals(allValues.get(0).getDesignersByIndex().get(4).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(5).getName(), "F");
        assertEquals(allValues.get(0).getDesignersByIndex().get(5).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(6).getName(), "G");
        assertEquals(allValues.get(0).getDesignersByIndex().get(6).getCategories().size(), 1);
        assertEquals(allValues.get(0).getDesignersByIndex().get(6).getCategories().get(0).getId(), "gucci");
        assertEquals(allValues.get(0).getDesignersByIndex().get(6).getCategories().get(0).getName(), "Gucci");
        assertEquals(allValues.get(0).getDesignersByIndex().get(6).getCategories().get(0).getUrl(), "//gucci?navpath=live_designer_gucci");
        assertEquals(allValues.get(0).getDesignersByIndex().get(6).getCategories().get(0).getAdornmentTag(), "NEW");
        assertNull(allValues.get(0).getDesignersByIndex().get(6).getCategories().get(0).getExcludedCountries());

        assertEquals(allValues.get(0).getDesignersByIndex().get(7).getName(), "H");
        assertEquals(allValues.get(0).getDesignersByIndex().get(7).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(8).getName(), "I");
        assertEquals(allValues.get(0).getDesignersByIndex().get(8).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(9).getName(), "J");
        assertEquals(allValues.get(0).getDesignersByIndex().get(9).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(10).getName(), "K");
        assertEquals(allValues.get(0).getDesignersByIndex().get(10).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getName(), "L");
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().size(), 2);
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(0).getId(), "lancer");
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(0).getName(), "Lancer");
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(0).getUrl(), "//lancer?navpath=live_designer_lancer");
        assertNull(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(0).getAdornmentTag());
        assertNull(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(0).getExcludedCountries());
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(1).getId(), "luna");
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(1).getName(), "Luna");
        assertEquals(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(1).getUrl(), "//cat4?navpath=live_designer_luna_cat3_cat4");
        assertNull(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(1).getAdornmentTag());
        assertNull(allValues.get(0).getDesignersByIndex().get(11).getCategories().get(1).getExcludedCountries());

        assertEquals(allValues.get(0).getDesignersByIndex().get(12).getName(), "M");
        assertEquals(allValues.get(0).getDesignersByIndex().get(12).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(13).getName(), "N");
        assertEquals(allValues.get(0).getDesignersByIndex().get(13).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(14).getName(), "O");
        assertEquals(allValues.get(0).getDesignersByIndex().get(14).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(15).getName(), "P");
        assertEquals(allValues.get(0).getDesignersByIndex().get(15).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(16).getName(), "Q");
        assertEquals(allValues.get(0).getDesignersByIndex().get(16).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(17).getName(), "R");
        assertEquals(allValues.get(0).getDesignersByIndex().get(17).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(18).getName(), "S");
        assertEquals(allValues.get(0).getDesignersByIndex().get(18).getCategories().size(), 0);

        assertEquals(allValues.get(0).getDesignersByIndex().get(19).getName(), "T");
        assertEquals(allValues.get(0).getDesignersByIndex().get(19).getCategories().size(), 1);
        assertEquals(allValues.get(0).getDesignersByIndex().get(19).getCategories().get(0).getId(), "tai");
        assertEquals(allValues.get(0).getDesignersByIndex().get(19).getCategories().get(0).getName(), "Tai");
        assertEquals(allValues.get(0).getDesignersByIndex().get(19).getCategories().get(0).getUrl(), "//cat3?navpath=live_designer_tai_cat3");
        assertNull(allValues.get(0).getDesignersByIndex().get(19).getCategories().get(0).getAdornmentTag());
        assertNull(allValues.get(0).getDesignersByIndex().get(19).getCategories().get(0).getExcludedCountries());

        assertEquals(allValues.get(0).getDesignersByIndex().get(20).getName(), "U");
        assertEquals(allValues.get(0).getDesignersByIndex().get(20).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(21).getName(), "V");
        assertEquals(allValues.get(0).getDesignersByIndex().get(21).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(22).getName(), "W");
        assertEquals(allValues.get(0).getDesignersByIndex().get(22).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(23).getName(), "X");
        assertEquals(allValues.get(0).getDesignersByIndex().get(23).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(24).getName(), "Y");
        assertEquals(allValues.get(0).getDesignersByIndex().get(24).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(25).getName(), "Z");
        assertEquals(allValues.get(0).getDesignersByIndex().get(25).getCategories().size(), 0);
        assertEquals(allValues.get(0).getDesignersByIndex().get(26).getName(), "#");
        assertEquals(allValues.get(0).getDesignersByIndex().get(26).getCategories().size(), 0);
    }

    @Test
    public void testDesignerByIndexIsAvailableInMen() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals("Men", allValues.get(1).getName());
        assertEquals("men_designer", allValues.get(1).getId());

        assertEquals(allValues.get(1).getDesignersByIndex().size(), 27);
        assertEquals(allValues.get(1).getDesignersByIndex().get(0).getName(), "A");
        assertEquals(allValues.get(1).getDesignersByIndex().get(0).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(1).getName(), "B");
        assertEquals(allValues.get(1).getDesignersByIndex().get(1).getCategories().size(), 1);
        assertEquals(allValues.get(1).getDesignersByIndex().get(1).getCategories().get(0).getId(), "bally");
        assertEquals(allValues.get(1).getDesignersByIndex().get(1).getCategories().get(0).getName(), "Bally");
        assertEquals(allValues.get(1).getDesignersByIndex().get(1).getCategories().get(0).getUrl(), "//bally?navpath=live_designer_bally");
        assertNull(allValues.get(1).getDesignersByIndex().get(1).getCategories().get(0).getAdornmentTag());
        assertEquals(allValues.get(1).getDesignersByIndex().get(1).getCategories().get(0).getExcludedCountries(), Collections.singletonList("AU"));

        assertEquals(allValues.get(1).getDesignersByIndex().get(2).getName(), "C");
        assertEquals(allValues.get(1).getDesignersByIndex().get(2).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(3).getName(), "D");
        assertEquals(allValues.get(1).getDesignersByIndex().get(3).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(4).getName(), "E");
        assertEquals(allValues.get(1).getDesignersByIndex().get(4).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(5).getName(), "F");
        assertEquals(allValues.get(1).getDesignersByIndex().get(5).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(6).getName(), "G");
        assertEquals(allValues.get(1).getDesignersByIndex().get(6).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(7).getName(), "H");
        assertEquals(allValues.get(1).getDesignersByIndex().get(7).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(8).getName(), "I");
        assertEquals(allValues.get(1).getDesignersByIndex().get(8).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(9).getName(), "J");
        assertEquals(allValues.get(1).getDesignersByIndex().get(9).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(10).getName(), "K");
        assertEquals(allValues.get(1).getDesignersByIndex().get(10).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(11).getName(), "L");
        assertEquals(allValues.get(1).getDesignersByIndex().get(11).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(12).getName(), "M");
        assertEquals(allValues.get(1).getDesignersByIndex().get(12).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(13).getName(), "N");
        assertEquals(allValues.get(1).getDesignersByIndex().get(13).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(14).getName(), "O");
        assertEquals(allValues.get(1).getDesignersByIndex().get(14).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(15).getName(), "P");
        assertEquals(allValues.get(1).getDesignersByIndex().get(15).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(16).getName(), "Q");
        assertEquals(allValues.get(1).getDesignersByIndex().get(16).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(17).getName(), "R");
        assertEquals(allValues.get(1).getDesignersByIndex().get(17).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(18).getName(), "S");
        assertEquals(allValues.get(1).getDesignersByIndex().get(18).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(19).getName(), "T");
        assertEquals(allValues.get(1).getDesignersByIndex().get(19).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(20).getName(), "U");
        assertEquals(allValues.get(1).getDesignersByIndex().get(20).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(21).getName(), "V");
        assertEquals(allValues.get(1).getDesignersByIndex().get(21).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(22).getName(), "W");
        assertEquals(allValues.get(1).getDesignersByIndex().get(22).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(23).getName(), "X");
        assertEquals(allValues.get(1).getDesignersByIndex().get(23).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(24).getName(), "Y");
        assertEquals(allValues.get(1).getDesignersByIndex().get(24).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(25).getName(), "Z");
        assertEquals(allValues.get(1).getDesignersByIndex().get(25).getCategories().size(), 0);
        assertEquals(allValues.get(1).getDesignersByIndex().get(26).getName(), "#");
        assertEquals(allValues.get(1).getDesignersByIndex().get(26).getCategories().size(), 0);
    }

    @Test
    public void testDesignerByIndexIsAvailableInaSale() {
        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals("Sale", allValues.get(3).getName());
        assertEquals("sale_designer", allValues.get(3).getId());

        assertEquals(allValues.get(3).getDesignersByIndex().size(), 27);
        assertEquals(allValues.get(3).getDesignersByIndex().get(0).getName(), "A");
        assertEquals(allValues.get(3).getDesignersByIndex().get(0).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(1).getName(), "B");
        assertEquals(allValues.get(3).getDesignersByIndex().get(1).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(2).getName(), "C");
        assertEquals(allValues.get(3).getDesignersByIndex().get(2).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(3).getName(), "D");
        assertEquals(allValues.get(3).getDesignersByIndex().get(3).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(4).getName(), "E");
        assertEquals(allValues.get(3).getDesignersByIndex().get(4).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(5).getName(), "F");
        assertEquals(allValues.get(3).getDesignersByIndex().get(5).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(6).getName(), "G");
        assertEquals(allValues.get(3).getDesignersByIndex().get(6).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(7).getName(), "H");
        assertEquals(allValues.get(3).getDesignersByIndex().get(7).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(8).getName(), "I");
        assertEquals(allValues.get(3).getDesignersByIndex().get(8).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(9).getName(), "J");
        assertEquals(allValues.get(3).getDesignersByIndex().get(9).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(10).getName(), "K");
        assertEquals(allValues.get(3).getDesignersByIndex().get(10).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(11).getName(), "L");
        assertEquals(allValues.get(3).getDesignersByIndex().get(11).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(12).getName(), "M");
        assertEquals(allValues.get(3).getDesignersByIndex().get(12).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(13).getName(), "N");
        assertEquals(allValues.get(3).getDesignersByIndex().get(13).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(14).getName(), "O");
        assertEquals(allValues.get(3).getDesignersByIndex().get(14).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(15).getName(), "P");
        assertEquals(allValues.get(3).getDesignersByIndex().get(15).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(16).getName(), "Q");
        assertEquals(allValues.get(3).getDesignersByIndex().get(16).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(17).getName(), "R");
        assertEquals(allValues.get(3).getDesignersByIndex().get(17).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(18).getName(), "S");
        assertEquals(allValues.get(3).getDesignersByIndex().get(18).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(19).getName(), "T");
        assertEquals(allValues.get(3).getDesignersByIndex().get(19).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(20).getName(), "U");
        assertEquals(allValues.get(3).getDesignersByIndex().get(20).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(21).getName(), "V");
        assertEquals(allValues.get(3).getDesignersByIndex().get(21).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(22).getName(), "W");
        assertEquals(allValues.get(3).getDesignersByIndex().get(22).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(23).getName(), "X");
        assertEquals(allValues.get(3).getDesignersByIndex().get(23).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(24).getName(), "Y");
        assertEquals(allValues.get(3).getDesignersByIndex().get(24).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(25).getName(), "Z");
        assertEquals(allValues.get(3).getDesignersByIndex().get(25).getCategories().size(), 0);
        assertEquals(allValues.get(3).getDesignersByIndex().get(26).getName(), "#");
        assertEquals(allValues.get(3).getDesignersByIndex().get(26).getCategories().size(), 0);
    }

    @Test
    public void testGetDesignerIndex() {
        when(designerIndexRepository.get("cat1")).thenReturn(new DesignerIndex());
        assertNotNull(designerIndexProcessor.getDesignerIndex("cat1"));
        verify(designerIndexRepository).get(anyString());
    }

    @Test
    public void testDesignerByCategoryIsEmptyWhenNoChildrenIsPresent() {
        when(leftNavTreeProcessor.getCategoryDocument(eq("designerByCategory"))).thenReturn(Optional.of(CategoryDocument
                .builder()
                .id("designerByCategory")
                .name("DesignerByCategory")
                .build()));

        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(1)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 1);
        assertEquals(allValues.get(0).getDesignersByCategory().size(), 1);
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getId(), "designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getName(), "VIEW ALL A-Z");
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getUrl(), "//designers");
    }

    @Test
    public void testDesignerByCategoryIsNotFound() {
        when(leftNavTreeProcessor.getCategoryDocument(eq("designerByCategory"))).thenReturn(Optional.empty());

        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(1)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 1);
        assertEquals(allValues.get(0).getDesignersByCategory().size(), 1);
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getId(), "designer");
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getName(), "VIEW ALL A-Z");
        assertEquals(allValues.get(0).getDesignersByCategory().get(0).getUrl(), "//designers");
    }

    @Test
    public void testDesignerBoutiqueTextAdornments() {
        when(leftNavTreeProcessor.getCategoryDocument(eq("designer"))).thenReturn(getDesignerCategoryDocumentAdornments());

        designerIndexProcessor.buildDesignerIndex();

        verify(designerIndexRepository, times(5)).save(designerIndexArgumentCaptor.capture());

        List<DesignerIndex> allValues = designerIndexArgumentCaptor.getAllValues();

        assertEquals(allValues.size(), 5);
        assertEquals(allValues.get(4).getDesignersByIndex().isEmpty(), false);
        assertEquals(allValues.get(4).getDesignersByIndex().size(), 27);
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().size(), 4);
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(0).getId(), "a001");
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(0).getAdornmentTag(), null);
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(1).getId(), "a002");
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(1).getAdornmentTag(), BOUTIQUE_TEXT_ADORNMENT_OVERRIDE);
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(2).getId(), "a003");
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(2).getAdornmentTag(), "NEW");
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(3).getId(), "a004");
        assertEquals(allValues.get(4).getDesignersByIndex().get(0).getCategories().get(3).getAdornmentTag(), BOUTIQUE_TEXT_ADORNMENT_OVERRIDE);
    }

    @Test
    public void testFalseWhenCategoryDocumentIsNull() {
        boolean isRebuildDesignerIndex = designerIndexProcessor.isRebuildDesignerIndex(CategoryDocument.builder().build());
        assertFalse(isRebuildDesignerIndex);
    }

    @Test
    public void testTrueWhenParentMatchesWithDesigner() {
        boolean isRebuildDesignerIndex = designerIndexProcessor.isRebuildDesignerIndex(
                CategoryDocument
                        .builder()
                        .parents(Collections.singletonMap("designer", 1))
                        .build()
        );
        assertTrue(isRebuildDesignerIndex);
    }

    @Test
    public void testTrueWhenParentMatchesWithDesignerByCategory() {
        boolean isRebuildDesignerIndex = designerIndexProcessor.isRebuildDesignerIndex(
                CategoryDocument
                        .builder()
                        .parents(Collections.singletonMap("designerByCategory", 1))
                        .build()
        );
        assertTrue(isRebuildDesignerIndex);
    }

    @Test
    public void testTrueWhenCategoryIsBoutique() {
        boolean isRebuildDesignerIndex = designerIndexProcessor.isRebuildDesignerIndex(
                CategoryDocument
                        .builder()
                        .boutique(true)
                        .build()
        );
        assertTrue(isRebuildDesignerIndex);
    }

    @Test
    public void testTrueWhenCategoryIsBoutiqueChild() {
        boolean isRebuildDesignerIndex = designerIndexProcessor.isRebuildDesignerIndex(
                CategoryDocument
                        .builder()
                        .boutiqueChild(true)
                        .parents(Collections.singletonMap("someParent", 1))
                        .build()
        );
        assertTrue(isRebuildDesignerIndex);
    }

    private Optional<CategoryDocument> getDesignerCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("designer").name("Designer").canonicalUrl("//designers").children(Arrays.asList("gucci", "bally", "9thAve", "lancer", "tai", "luna")).build());
    }

    private Optional<CategoryDocument> getDesignerCategoryDocumentAdornments() {
        return Optional.of(CategoryDocument.builder().id("designer").name("Designer").canonicalUrl("//designers").children(Arrays.asList("a001", "a002", "a003", "a004")).build());
    }

    private Optional<CategoryDocument> getDesignerByCategoryDocument() {
        return Optional.of(CategoryDocument
                .builder()
                .id("designerByCategory")
                .name("DesignerByCategory")
                .children(Arrays.asList("women_designer", "men_designer", "kids_designer", "sale_designer"))
                .build());
    }

    private Optional<CategoryDocument> getWomenDesignerCategoryDocument() {
        return Optional.of(CategoryDocument
                .builder()
                .id("women_designer")
                .name("All Designer")
                .canonicalUrl("//women")
                .children(Collections.singletonList("women"))
                .contextualProperties(Collections.singletonList(
                        ContextualProperty
                                .builder()
                                .parentId("designerByCategory")
                                .desktopAlternateName("Women")
                                .build()))
                .build());
    }

    private Optional<CategoryDocument> getMenDesignerCategoryDocument() {
        return Optional.of(CategoryDocument
                .builder()
                .id("men_designer")
                .name("All Designer")
                .canonicalUrl("//men")
                .children(Collections.singletonList("men"))
                .contextualProperties(Collections.singletonList(ContextualProperty.builder().parentId("designerByCategory").desktopAlternateName("Men").build()))
                .build());
    }

    private Optional<CategoryDocument> getKidsDesignerCategoryDocument() {
        return Optional.of(CategoryDocument
                .builder()
                .id("kids_designer")
                .name("All Designer")
                .canonicalUrl("//kids")
                .children(Collections.singletonList("kids"))
                .build());
    }

    private Optional<CategoryDocument> getSaleDesignerCategoryDocument() {
        return Optional.of(CategoryDocument
                .builder()
                .id("sale_designer")
                .name("Sale")
                .canonicalUrl("//sale")
                .children(Collections.singletonList("sale"))
                .build());
    }

    private Optional<CategoryDocument> getWomenCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("women").name("Women").children(Arrays.asList("gucci", "lancer", "tai", "luna", "notExists")).build());
    }

    private Optional<CategoryDocument> getMenCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("men").name("Men").children(Arrays.asList("bally", "noneBoutique")).build());
    }

    private Optional<CategoryDocument> getKidsCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("kids").name("Kids").children(Arrays.asList("gucci", "bally", "hidden")).build());
    }

    private Optional<CategoryDocument> getSaleCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("sale").name("Sale").children(Collections.singletonList("noResults")).build());
    }

    private Optional<CategoryDocument> getLancerCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("lancer").name("Lancer").boutique(true).canonicalUrl("//lancer").children(Collections.singletonList("cat2")).children(Collections.singletonList("cat4")).contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("women").driveToSubcategoryId("cat2:cat3").build()
        )).build());
    }

    private Optional<CategoryDocument> getTaiCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("tai").name("Tai").boutique(true).canonicalUrl("//tai").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("women").driveToSubcategoryId("cat3").build()
        )).children(Collections.singletonList("cat3")).build());
    }

    private Optional<CategoryDocument> getLunaCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("luna").name("Luna").boutique(true).canonicalUrl("//luna").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("women").driveToSubcategoryId("cat3:cat4").build()
        )).children(Collections.singletonList("cat3")).build());
    }

    private Optional<CategoryDocument> getCat4CategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("cat4").name("Cat4").canonicalUrl("//cat4").build());
    }

    private Optional<CategoryDocument> getCat3CategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("cat3").name("Cat3").children(Collections.singletonList("cat4")).canonicalUrl("//cat3").build());
    }

    private Optional<CategoryDocument> getGucciCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("gucci").name("Gucci").boutique(true).canonicalUrl("//gucci").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("women").boutiqueTextAdornments("NEW").driveToSubcategoryId("cat1:cat2").build()
        )).children(Collections.singletonList("cat1")).build());
    }

    private Optional<CategoryDocument> getA001CategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("a001").name("A001").boutique(true).canonicalUrl("//a001").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("designer").boutiqueTextAdornmentsOverride(false).build())).children(Collections.singletonList("cat1")).build());
    }

    private Optional<CategoryDocument> getA002CategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("a002").name("A002").boutique(true).canonicalUrl("//a002").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("designer").boutiqueTextAdornments("NEW").boutiqueTextAdornmentsOverride(true).build())).children(Collections.singletonList("cat1")).build());
    }

    private Optional<CategoryDocument> getA003CategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("a003").name("A003").boutique(true).canonicalUrl("//a003").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("designer").boutiqueTextAdornments("NEW").boutiqueTextAdornmentsOverride(false).build())).children(Collections.singletonList("cat1")).build());
    }

    private Optional<CategoryDocument> getA004CategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("a004").name("A004").boutique(true).canonicalUrl("//a004").contextualProperties(Collections.singletonList(
                ContextualProperty.builder().parentId("designer").boutiqueTextAdornmentsOverride(true).build())).children(Collections.singletonList("cat1")).build());
    }

    private Optional<CategoryDocument> getBallyCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("bally").name("Bally").boutique(true).canonicalUrl("//bally").excludedCountries(Collections.singletonList("AU")).build());
    }

    private Optional<CategoryDocument> get9thAveCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("9thAve").name("9ThAve").boutique(true).canonicalUrl("//9thAve").build());
    }

    private Optional<CategoryDocument> getNonBoutiqueCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("noneBoutique").name("NoneBoutique").boutique(false).canonicalUrl("//noneBoutique").build());
    }

    private Optional<CategoryDocument> getHiddenCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("hidden").name("Hidden").boutique(true).hidden(true).canonicalUrl("//hidden").build());
    }

    private Optional<CategoryDocument> getNoResultsCategoryDocument() {
        return Optional.of(CategoryDocument.builder().id("noResults").name("NoResults").boutique(true).noResults(true).canonicalUrl("//noResults").build());
    }
}