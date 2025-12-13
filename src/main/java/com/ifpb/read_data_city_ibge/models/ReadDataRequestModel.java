package com.ifpb.read_data_city_ibge.models;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ReadDataRequestModel {
    @NotNull(message = "The `year` field cannot be null.")
    private Integer year;
}
