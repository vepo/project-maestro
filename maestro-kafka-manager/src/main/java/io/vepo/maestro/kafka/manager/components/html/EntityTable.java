package io.vepo.maestro.kafka.manager.components.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.vaadin.flow.component.Component;

public class EntityTable<T> extends Table {

    public class ColumnBuilder {
        private String header;
        private Function<T, String> provider;
        private Function<T, Component> componentProvider;

        public ColumnBuilder() {
            super();
        }

        public ColumnBuilder withHeader(String header) {
            this.header = header;
            return this;
        }

        public ColumnBuilder withValue(Function<T, String> provider) {
            this.provider = provider;
            if (Objects.nonNull(this.componentProvider)) {
                throw new IllegalArgumentException("You can't use both value and component provider");
            }
            return this;
        }

        public ColumnBuilder withComponent(Function<T, Component> provider) {
            this.componentProvider = provider;
            if (Objects.nonNull(this.provider)) {
                throw new IllegalArgumentException("You can't use both value and component provider");
            }
            return this;
        }

        public EntityTable<T> build() {
            columns.add(new ColumnDefintion<>(header, provider, componentProvider));
            return EntityTable.this;
        }
    }

    private record ColumnDefintion<J>(String name,
                                      Function<J, String> provider,
                                      Function<J, Component> componentProvider) {
    }

    private List<ColumnDefintion<T>> columns;
    private List<T> values;

    public EntityTable(List<T> values) {
        super();
        this.columns = new ArrayList<>();
        this.values = values;
    }

    public ColumnBuilder addColumn(String name) {
        return new ColumnBuilder().withHeader(name);
    }

    public EntityTable<T> bind() {
        columns.forEach(column -> addHeader(column.name));

        ColumnDefintion<T> lastColumn = columns.getLast();
        values.forEach(value -> columns.forEach(column -> {
            if (Objects.nonNull(column.componentProvider)) {
                addCell(column.componentProvider.apply(value), lastColumn.equals(column));
            } else {
                addCell(column.provider().apply(value), lastColumn.equals(column));
            }
        }));
        return this;
    }

    public void update(List<T> values) {
        this.values = values;
        clear();
        bind();
    }

}
