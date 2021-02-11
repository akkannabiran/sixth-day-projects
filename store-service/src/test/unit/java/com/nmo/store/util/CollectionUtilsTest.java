package com.sixthday.store.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CollectionUtilsTest {
    @Test
    public void shouldReturnDisjunctionOfTwoCollections() throws Exception {
        List<String> collection1 = Arrays.asList("01", "02", "03", "04");
        List<String> collection2 = Arrays.asList("02", "03");
        List<String> resultCollection = CollectionUtils.disjuntion(collection1, collection2);

        assertThat(resultCollection.size(),is(2));
        assertThat(resultCollection,is(Arrays.asList("01", "04")));
    }
}