package com.ptit.cinema.controller;

import com.ptit.cinema.model.Phim;
import com.ptit.cinema.repository.PhimRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/phim")
public class PhimController {
    private final PhimRepository repository;

    public PhimController(PhimRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Phim> all(@RequestParam(required = false) String q) {
        return (q == null || q.isBlank()) ? repository.findAll() : repository.search(q);
    }

    @PostMapping
    public ResponseEntity<Phim> create(@RequestBody Phim p) {
        repository.insert(p);
        return ResponseEntity.created(URI.create("/api/phim/" + p.maPhim())).body(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody Phim p) {
        return repository.update(id, p) == 0
                ? ResponseEntity.notFound().build()
                : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return repository.delete(id) == 0
                ? ResponseEntity.notFound().build()
                : ResponseEntity.noContent().build();
    }
}
