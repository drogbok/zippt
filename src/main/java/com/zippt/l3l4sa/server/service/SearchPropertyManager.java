package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.command.Commands.SearchPropertyCommand;
import com.zippt.l3l4sa.common.result.Results.PropertySearchResult;
import java.util.List;

public class SearchPropertyManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;

    public SearchPropertyManager(DataStore store, AuthenticationManager authenticationManager) {
        this.store = store;
        this.authenticationManager = authenticationManager;
    }

    public PropertySearchResult search(SearchPropertyCommand command) {
        long startedAt = System.nanoTime();
        authenticationManager.validateBuyer(command.buyerId());
        String region = command.conditionInput() == null ? null : command.conditionInput().region();
        List<String> propertyIds = region == null
                ? store.properties().stream().map(property -> property.getPropertyId()).toList()
                : store.findPropertyIdsByRegion(region);
        store.addOperationLog(new OperationLog(
                "SEARCH_PROPERTY",
                command.buyerId(),
                region == null ? "ALL" : region,
                "SUCCESS(" + propertyIds.size() + ")",
                System.nanoTime() - startedAt
        ));
        return new PropertySearchResult(propertyIds);
    }
}


