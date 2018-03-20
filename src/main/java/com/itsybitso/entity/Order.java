package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Order {
    String book;
    BigDecimal price;
    BigDecimal amount;
    String oid;

    public Order(@JsonProperty("book") String book,
                 @JsonProperty("price") BigDecimal price,
                 @JsonProperty("amount") BigDecimal amount,
                 @JsonProperty("oid") String oid) {
      this.book = book;
      this.price = price;
      this.amount = amount;
      this.oid = oid;
    }

    public String getBook() {
      return book;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public BigDecimal getAmount() {
      return amount;
    }

    public String getOid() {
      return oid;
    }
  }
