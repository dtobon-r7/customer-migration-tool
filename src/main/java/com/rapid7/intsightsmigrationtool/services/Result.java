package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.services.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Result<T extends DTO> {

    private boolean success;
    private String message;
    private T entity;

    public Result() {
        this.success = false;
        this.message = "";
        this.entity = null;
    }

    public Result(boolean success, T entity) {
        this.success = success;
        this.message = "";
        this.entity = entity;
    }


}
