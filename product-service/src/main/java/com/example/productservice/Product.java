package com.example.productservice;

import lombok.Builder;

@Builder
record Product(long id,
               String name,
               double price,
               int stock,
               String category) {}