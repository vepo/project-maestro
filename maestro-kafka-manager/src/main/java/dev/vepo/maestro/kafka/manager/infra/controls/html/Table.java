package dev.vepo.maestro.kafka.manager.infra.controls.html;

import java.util.Objects;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;

@Tag("table")
public class Table extends HtmlContainer implements ClickNotifier<Table>, HasOrderedComponents {

    public enum CellClass {
        DEFAULT, NUMERIC
    }

    protected static record ColSpan(int i) {

        public void apply(HtmlContainer cell) {
            if (i > 1) {
                cell.getElement().setAttribute("colspan", String.valueOf(i));
            }
        }
    }

    protected static record RowSpan(int i) {

        public void apply(HtmlContainer cell) {
            if (i > 1) {
                cell.getElement().setAttribute("rowspan", String.valueOf(i));
            }
        }
    }

    protected static RowSpan rowSpan(int i) {
        return new RowSpan(i);
    }

    protected static ColSpan colspan(int i) {
        return new ColSpan(i);
    }

    private Thead thead;
    private Tbody tbody;
    private Tr currentHeaderRow;
    private Tr currentBodyRow;
    private boolean even;
    private int rowSize;
    private int rowCounter;

    /**
     * Creates a new empty div.
     */
    public Table() {
        super();
        even = false;
        rowSize = 0;
        rowCounter = 0;
        getElement().setAttribute("theme", "padding spacing");
        addClassName("custom-table");
        setWidthFull();
        thead = new Thead();
        tbody = new Tbody();
        add(thead, tbody);
    }

    protected void clear() {
        clearHeader();
        clearBody();
    }

    protected void clearHeader() {
        thead.removeAll();
        currentHeaderRow = null;
    }

    protected void clearBody() {
        tbody.removeAll();
        currentBodyRow = null;
    }

    protected Table addHeader(String header, RowSpan rowSpan) {
        return addHeader(header, rowSpan, colspan(1), false);
    }

    protected Table addHeader(String header) {
        return addHeader(header, rowSpan(1), colspan(1), false);
    }

    protected Table addHeader(String header, RowSpan rowSpan, ColSpan colspan) {
        return addHeader(header, rowSpan, colspan, false);
    }

    protected Table addCell(String value, boolean grow, RowSpan rowSpan, ColSpan colspan, CellClass cellClass, boolean lastCell) {
        return addCell(new Text(value), grow, rowSpan, colspan, cellClass, lastCell);
    }

    protected Table addCell(Component value, boolean grow, RowSpan rowSpan, ColSpan colspan, CellClass cellClass, boolean lastCell) {
        if (Objects.isNull(currentBodyRow)) {
            currentBodyRow = new Tr();
            if (rowSize == 0) {
                rowSize = rowSpan.i();
            }
            if (even) {
                currentBodyRow.addClassName("even");
            }
            tbody.add(currentBodyRow);
        }
        var cell = new Td(grow);
        if (cellClass != CellClass.DEFAULT) {
            cell.addClassName(cellClass.name().toLowerCase());
        }
        cell.add(value);
        colspan.apply(cell);
        rowSpan.apply(cell);
        currentBodyRow.add(cell);
        if (lastCell) {
            rowCounter++;
            if (rowCounter >= rowSize) {
                rowSize = 0;
                rowCounter = 0;
                even = !even;
            }
            currentBodyRow = null;
        }
        return this;
    }

    protected Table addCell(Component value, RowSpan rowSpan, boolean lastCell) {
        return addCell(value, false, rowSpan, colspan(1), CellClass.DEFAULT, lastCell);
    }

    protected Table addCell(Component value, RowSpan rowSpan, CellClass cellClass, boolean lastCell) {
        return addCell(value, false, rowSpan, colspan(1), cellClass, lastCell);
    }

    protected Table addCell(String value, RowSpan rowSpan, boolean lastCell) {
        return addCell(value, false, rowSpan, colspan(1), CellClass.DEFAULT, lastCell);
    }

    protected Table addCell(String value, RowSpan rowSpan, ColSpan colspan) {
        return addCell(value, false, rowSpan, colspan, CellClass.DEFAULT, false);
    }

    protected Table addCell(String value) {
        return addCell(value, false, rowSpan(1), colspan(1), CellClass.DEFAULT, false);
    }

    protected Table addCell(Component value) {
        return addCell(value, false, rowSpan(1), colspan(1), CellClass.DEFAULT, false);
    }

    protected Table addHeader(String header, RowSpan rowSpan, boolean lastCell) {
        return addHeader(header, rowSpan, colspan(1), lastCell);
    }

    protected Table addCell(String value, boolean lastCell) {
        return addCell(value, false, rowSpan(1), colspan(1), CellClass.DEFAULT, lastCell);
    }

    protected Table addCell(String value, CellClass cellClass, boolean lastCell) {
        return addCell(value, false, rowSpan(1), colspan(1), cellClass, lastCell);
    }

    protected Table addCell(String value, CellClass cellClass) {
        return addCell(value, false, rowSpan(1), colspan(1), cellClass, false);
    }

    protected Table addCell(Component value, boolean lastCell) {
        return addCell(value, false, rowSpan(1), colspan(1), CellClass.DEFAULT, lastCell);
    }

    protected Table addCell(String value, RowSpan rowSpan) {
        return addCell(value, false, rowSpan, colspan(1), CellClass.DEFAULT, false);
    }

    protected Table addCell(String value, RowSpan rowSpan, CellClass cellClass) {
        return addCell(value, false, rowSpan, colspan(1), cellClass, false);
    }

    protected Table addCell(String value, boolean grow, RowSpan rowSpan) {
        return addCell(value, grow, rowSpan, colspan(1), CellClass.DEFAULT, false);
    }

    protected Table addHeader(String header, RowSpan rowSpan, ColSpan colspan, boolean lastCell) {
        if (Objects.isNull(currentHeaderRow)) {
            currentHeaderRow = new Tr();
            thead.add(currentHeaderRow);

        }
        var headerCell = new Th();
        headerCell.setText(header);
        colspan.apply(headerCell);
        rowSpan.apply(headerCell);
        currentHeaderRow.add(headerCell);
        if (lastCell) {
            currentHeaderRow = null;
        }
        return this;
    }
}
