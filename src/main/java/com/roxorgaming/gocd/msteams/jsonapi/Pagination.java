package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("page_size")
    private Integer pageSize;
}
