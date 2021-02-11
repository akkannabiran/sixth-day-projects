package com.sixthday.category.api.utils

import org.junit.Test
import org.mockito.InjectMocks


class TeamsTestingUtilTest {
    @InjectMocks
    TeamsTestingUtil teamsTestingUtil;

    @Test
    public void testShouldReturnNoNameWhenInputIsNull() {
        teamsTestingUtil.provideOutput(null);

    }
}
