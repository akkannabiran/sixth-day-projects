package com.sixthday.navigation.api.models.response;

import com.sixthday.navigation.api.models.SisterSite;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandLinks {
    private List<SisterSite> sisterSites;
}
