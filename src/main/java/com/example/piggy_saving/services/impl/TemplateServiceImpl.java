package com.example.piggy_saving.services.impl;

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

        return switch (type){
            case TransferType.P2P -> "email/p2p";
            case TransferType.OWN ->  "email/own";
            case TransferType.CONTRIBUTION ->  "email/contribution";
        };


//        return "";
    }
}
