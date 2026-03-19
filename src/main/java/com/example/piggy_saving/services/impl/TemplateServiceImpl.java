package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.models.enums.P2PRole;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.services.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    private final SpringTemplateEngine templateEngine;

    @Override
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    @Override
    public String resolveTemplate(TransferType type) {
        return switch (type) {
            case P2P -> "email/p2p-transfer";  // This can be a generic template or throw error
            case OWN -> "email/own-transfer";
            case CONTRIBUTION -> "email/contribution";
        };
    }

    @Override
    public String resolveP2PTemplate(P2PRole role) {
        return switch (role) {
            case SENDER -> "email/p2p-sender";     // Your sender template
            case RECEIVER -> "email/p2p-receiver"; // Your receiver template
        };
    }
}