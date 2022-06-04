package com.webfluxclient.models;

import lombok.Data;

import java.util.Date;

@Data
public class Product {
    private String id;
    private String name;
    private Double price;
    private Date createAt;
    private String picture;
    private Category category;
}
