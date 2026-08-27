package com.walkin.config;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PageRequestFactoryTests {
    private final PageRequestFactory factory = new PageRequestFactory();

    @Test void buildsValidatedPageRequest() {
        var page = factory.create(1, 25, "email", "desc", Set.of("email"));
        assertEquals(1, page.getPageNumber()); assertEquals(25, page.getPageSize());
        assertTrue(page.getSort().getOrderFor("email").isDescending());
    }
    @Test void rejectsOversizedPagesAndUnknownSortProperties() {
        assertThrows(IllegalArgumentException.class, () -> factory.create(0, 101, "email", "asc", Set.of("email")));
        assertThrows(IllegalArgumentException.class, () -> factory.create(0, 20, "passwordHash", "asc", Set.of("email")));
    }
}
