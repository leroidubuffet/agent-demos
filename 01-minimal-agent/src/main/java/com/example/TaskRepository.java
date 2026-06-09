package com.example;

import java.util.List;

public interface TaskRepository {
    Task findById(Long id);
    List<Task> findAll();
    Task save(Task task);
    void deleteById(Long id);
}
