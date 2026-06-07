/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.repo;

import java.util.List;

/**
 * Lightweight value-object holding one page of results together with the total row count.
 * Used by paginated repository methods so callers can render "Page X of Y" indicators
 * without issuing a separate count query themselves.
 *
 * @param items    the rows for the current page (size <= pageSize)
 * @param total    total number of rows matching the query across all pages
 * @param page     zero-indexed page number returned (clamped to [0, totalPages-1])
 * @param pageSize maximum rows per page used for the query
 */
public record Page<T>(List<T> items, int total, int page, int pageSize) {
    /** Total number of pages (at least 1, even when total == 0, for UI display). */
    public int totalPages() {
        return total == 0 ? 1 : (int) Math.ceil(total / (double) pageSize);
    }
}
