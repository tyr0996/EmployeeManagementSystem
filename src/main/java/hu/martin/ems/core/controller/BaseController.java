package hu.martin.ems.core.controller;

import com.google.gson.Gson;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.core.service.BaseService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@PermitAll
public abstract class BaseController<T extends BaseEntity, S extends BaseService<T, R>, R extends BaseRepository<T, Long>> {
    protected S service;

    @Autowired
    protected Gson gson;

    public BaseController(S service){
        this.service = service;
    }

    @GetMapping(path = "/findAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        return new ResponseEntity<>(gson.toJson(service.findAll(withDeleted)), HttpStatus.OK);
    }

    @GetMapping(path = "/findById", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findById(@RequestParam(required = true) Long id){
        return new ResponseEntity<>(gson.toJson(service.findById(id)), HttpStatus.OK);
    }

    @PutMapping(path = "/restore", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> restore(@RequestBody T entity) {
        return new ResponseEntity<>(gson.toJson(service.restore(entity)), HttpStatus.OK);
    }

    @PutMapping(path = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete(@RequestBody T entity) {
        return new ResponseEntity<>(gson.toJson(service.delete(entity)), HttpStatus.OK);
    }

    @DeleteMapping(path = "/permanentlyDelete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> permanentlyDelete(@RequestParam(value = "id") Long entityId) {
        return new ResponseEntity<>(gson.toJson(service.permanentlyDelete(entityId)), HttpStatus.OK);
    }

    @PostMapping(path = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> save(@RequestBody T entity) {
        return new ResponseEntity<>(gson.toJson(service.save(entity)), HttpStatus.OK);
    }

    @PutMapping(path = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> update(@RequestBody T entity) {
        return new ResponseEntity<>(gson.toJson(service.update(entity)), HttpStatus.OK);
    }
}