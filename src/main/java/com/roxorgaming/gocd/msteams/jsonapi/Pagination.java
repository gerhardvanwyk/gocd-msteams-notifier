package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Pagination {

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("page_size")
    private Integer pageSize;
}
