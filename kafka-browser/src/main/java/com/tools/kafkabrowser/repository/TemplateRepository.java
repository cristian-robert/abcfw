package com.tools.kafkabrowser.repository;

import com.tools.kafkabrowser.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findAllByOrderByUpdatedAtDesc();
    List<Template> findByCollectionIdOrderByUpdatedAtDesc(Long collectionId);
    List<Template> findByCollectionIsNullOrderByUpdatedAtDesc();
    int countByCollectionId(Long collectionId);
}
