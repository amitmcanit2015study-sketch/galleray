package com.amitbharat.gallery.data.models;

import java.io.Serializable;

public class FilterOptions implements Serializable {
    public enum TypeFilter {
        ALL, IMAGES, VIDEOS, GIF, RAW, LARGE, RECENT, FAVORITES
    }

    public enum SortOrder {
        DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC
    }

    private String query = "";
    private TypeFilter typeFilter = TypeFilter.ALL;
    private SortOrder sortOrder = SortOrder.DATE_DESC;
    private long minSize = 0;
    private long maxSize = Long.MAX_VALUE;

    public FilterOptions() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public TypeFilter getTypeFilter() { return typeFilter; }
    public void setTypeFilter(TypeFilter typeFilter) { this.typeFilter = typeFilter; }

    public SortOrder getSortOrder() { return sortOrder; }
    public void setSortOrder(SortOrder sortOrder) { this.sortOrder = sortOrder; }

    public long getMinSize() { return minSize; }
    public void setMinSize(long minSize) { this.minSize = minSize; }

    public long getMaxSize() { return maxSize; }
    public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
}
