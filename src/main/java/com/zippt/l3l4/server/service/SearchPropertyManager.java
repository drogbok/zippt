package com.zippt.l3l4.server.service;

import com.zippt.l3l4.common.command.Commands.SearchPropertyCommand;
import com.zippt.l3l4.common.result.Results.PropertySearchResult;
import java.util.List;

public class SearchPropertyManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;

    public SearchPropertyManager(DataStore store, AuthenticationManager authenticationManager) {
        this.store = store;
        this.authenticationManager = authenticationManager;
    }

    public PropertySearchResult search(SearchPropertyCommand command) {
        authenticationManager.validateBuyer(command.buyerId());
        List<String> propertyIds = store.properties().stream()
                .filter(property -> command.conditionInput() == null
                        || command.conditionInput().region() == null
                        || command.conditionInput().region().equals(property.getRegion()))
                .map(property -> property.getPropertyId())
                .toList();
        return new PropertySearchResult(propertyIds);
    }
}

