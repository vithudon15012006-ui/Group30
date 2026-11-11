package com.ptit.cinema.controller;

import com.ptit.cinema.model.KhachHang;
import com.ptit.cinema.repository.KhachHangRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangController {
    private final KhachHangRepository repository;

    public KhachHangController(KhachHangRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<KhachHang> all(@RequestParam(required = false) String q) {
        return (q == null || q.isBlank()) ? repository.findAll() : repository.search(q);
    }

    @PostMapping
    public ResponseEntity<KhachHang> create(@RequestBody KhachHang k) {
        repository.insert(k);
        return ResponseEntity.created(URI.create("/api/khach-hang/" + k.maKhachHang())).body(k);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody KhachHang k) {
        return repository.update(id, k) == 0
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
