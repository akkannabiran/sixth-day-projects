package com.sixthday.navigation.repository;

import com.sixthday.navigation.domain.SiloNavTree;
import org.springframework.data.repository.CrudRepository;

public interface SiloNavTreeRepository extends CrudRepository<SiloNavTree, String> {

    SiloNavTree findById(final String id);

}
