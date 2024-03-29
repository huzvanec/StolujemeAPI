package cz.jeme.programu.stolujemeapi.db;

import org.jetbrains.annotations.Range;

public interface Entry {
    @Range(from = 1, to = 16_777_215) int id();
}

