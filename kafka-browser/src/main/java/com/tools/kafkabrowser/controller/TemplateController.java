package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.Template;
import com.tools.kafkabrowser.model.TemplateDto;
import com.tools.kafkabrowser.model.TemplateSummaryDto;
import com.tools.kafkabrowser.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateRepository templateRepository;

    @GetMapping
    public List<TemplateSummaryDto> listTemplates() {
        return templateRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(TemplateSummaryDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public Template getTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
    }

    @PostMapping
    public ResponseEntity<Template> createTemplate(@RequestBody TemplateDto dto) {
        Template template = new Template();
        template.setName(dto.name());
        template.setTopicName(dto.topicName());
        template.setSchemaSubject(dto.schemaSubject());
        template.setJsonContent(dto.jsonContent());
        Template saved = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Template updateTemplate(@PathVariable Long id, @RequestBody TemplateDto dto) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
        template.setName(dto.name());
        template.setTopicName(dto.topicName());
        template.setSchemaSubject(dto.schemaSubject());
        template.setJsonContent(dto.jsonContent());
        return templateRepository.save(template);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found");
        }
        templateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
