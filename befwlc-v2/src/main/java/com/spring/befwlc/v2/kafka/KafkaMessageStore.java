package com.spring.befwlc.v2.kafka;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class KafkaMessageStore {
    private final List<ObjectNode> records = new CopyOnWriteArrayList<>();
    public void add(ObjectNode record) { records.add(record); }
    public List<ObjectNode> getAll() { return new ArrayList<>(records); }
    public boolean remove(ObjectNode record) { return records.remove(record); }
    public void clear() { records.clear(); }
    public int size() { return records.size(); }
}
