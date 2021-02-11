package com.sixthday.navigation.api.services;

import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.mappers.BrandLinksMapper;
import com.sixthday.navigation.api.models.response.BrandLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrandLinksService {
    private CategoryRepository categoryRepository;
    private BrandLinksMapper brandLinksMapper;

    @Autowired
    public BrandLinksService(final CategoryRepository categoryRepository, final BrandLinksMapper brandLinksMapper) {
        this.categoryRepository = categoryRepository;
        this.brandLinksMapper = brandLinksMapper;
    }

    public BrandLinks getBrandLinks() {
        return brandLinksMapper.map(categoryRepository.getBrandLinks());
    }
}
