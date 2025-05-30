package com.di.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;

public abstract class PageImplMixin<T> extends PageImpl<T> {
    @JsonCreator
    public PageImplMixin(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("sort") Sort sort
    ) {
        super(content, PageRequest.of(number, size, sort), totalElements);
    }
}

