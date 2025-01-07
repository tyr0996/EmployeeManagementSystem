package hu.martin.ems.core.controller;

import com.google.gson.Gson;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.core.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<T extends BaseEntity, S extends BaseService<T, R>, R extends BaseRepository<T, Long>> {
    protected S service;

    @Autowired
    protected Gson gson;

    public BaseController(S service){
        this.service = service;
    }

    @GetMapping(path = "/findAll", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<T> allElements = service.findAll(withDeleted);
        return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
    }

    @GetMapping(path = "/findAllWithGraph", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAllWithGraph(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<T> allElements = service.findAllWithGraph(withDeleted);
        return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
    }


    @GetMapping(path = "/findAllByIds", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") List<Long> ids) {
        List<T> allElements = service.findAllByIds(ids);
        return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
    }

    @PutMapping(path = "/restore", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> restore(@RequestBody T entity) {
        service.restore(entity);
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }

    @PutMapping(path = "/delete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> delete(@RequestBody T entity) {
        service.delete(entity);
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }

    @DeleteMapping(path = "/permanentlyDelete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> permanentlyDelete(@RequestParam(value = "id") Long entityId) {
        service.permanentlyDelete(entityId);
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }

    @PostMapping(path = "/save", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> save(@RequestBody T entity) {
        return new ResponseEntity<>(gson.toJson(service.save(entity)), HttpStatus.OK);
    }

    @PutMapping(path = "/update", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> update(@RequestBody T entity) {
        T e = service.update(entity);
        return new ResponseEntity<>(gson.toJson(e), HttpStatus.OK);
    }

    @DeleteMapping(path = "/clearDatabaseTable", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> clearDatabaseTable() {
        service.clearDatabaseTable();
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }

    @DeleteMapping(path = "/forcePermanentlyDelete")
    public void forcePermanentlyDelete(@RequestParam(value = "id") Long entityId){
        service.forcePermanentlyDelete(entityId);
    }
}