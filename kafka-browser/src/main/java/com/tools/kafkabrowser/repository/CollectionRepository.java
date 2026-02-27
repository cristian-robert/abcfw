package com.tools.kafkabrowser.repository;

import com.tools.kafkabrowser.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByOrderByUpdatedAtDesc();
}
