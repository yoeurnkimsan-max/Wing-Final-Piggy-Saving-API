package com.example.piggy_saving.services;

import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.models.enums.TransferType;

import java.util.Map;

public interface TemplateService {
    String renderTemplate(String templateName, Map<String, Object> variables);
    String resolveTemplate(TransferType type);
}
