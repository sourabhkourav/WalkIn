package com.walkin.config;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class PageRequestFactory {
    public Pageable create(int page, int size, String sort, String direction, Set<String> allowedSorts) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        if (!allowedSorts.contains(sort)) throw new IllegalArgumentException("Unsupported sort field: " + sort);
        Sort.Direction sortDirection;
        try { sortDirection = Sort.Direction.fromString(direction); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("direction must be asc or desc"); }
        return PageRequest.of(page, size, Sort.by(sortDirection, sort));
    }
}
