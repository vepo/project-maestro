package dev.vepo.maestro.framework.sample;

import java.time.Instant;

import java.util.Objects;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity("quote")
public class Quote {
    @Id("id")
    private Long id;

    @Column
    private String name;

    @Column
    private double price;

    @Column
    private Instant timestamp;

    public Quote() {
    }

    public Quote(String name, double price, Instant timestamp) {
        this.name = name;
        this.price = price;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Quote other = (Quote) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
