package com.tools.kafkabrowser.repository;

import com.tools.kafkabrowser.model.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
