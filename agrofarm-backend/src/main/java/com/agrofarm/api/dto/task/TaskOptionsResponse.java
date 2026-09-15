package com.agrofarm.api.dto.task;

import java.util.List;

public record TaskOptionsResponse(
        List<Option> farms,
        List<Option> fields,
        List<Option> supervisors,
        List<Option> workers
) {

    public record Option(
            Long id,
            String name
    ) {
    }
}