package dev.vepo.maestro.framework.sample;

import java.time.Instant;

import java.util.Objects;

import org.eclipse.jnosql.databases.mongodb.mapping.ObjectIdConverter;

import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity("quote")
public class Quote {
    @Id("id")
    @Convert(ObjectIdConverter.class)
    private String id;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Quote other = (Quote) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return String.format("Quote [id=%s, name=%s, price=%.2f, timestamp=%s]",
                             id, name, price, timestamp);
    }

}
