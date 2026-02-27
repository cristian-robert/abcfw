package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.AppSetting;
import com.tools.kafkabrowser.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final AppSettingRepository settingRepository;

    @GetMapping
    public Map<String, String> getAll() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(AppSetting::getKey, s -> s.getValue() != null ? s.getValue() : ""));
    }

    @PutMapping("/{key}")
    public Map<String, String> put(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.getOrDefault("value", "");
        settingRepository.save(new AppSetting(key, value));
        return Map.of(key, value);
    }
}
