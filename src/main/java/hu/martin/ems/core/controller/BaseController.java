package hu.martin.ems.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    protected ObjectMapper om;

    public BaseController(S service){
        this.service = service;
    }

    @GetMapping(path = "/findAll", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<T> allElements = service.findAll(withDeleted);
        try{
            return new ResponseEntity<>(om.writeValueAsString(allElements), HttpStatus.OK);
        }
        catch(JsonProcessingException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<String> save(@RequestBody T entity) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.save(entity)), HttpStatus.OK);
    }

    @PutMapping(path = "/update", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> update(@RequestBody T entity) throws JsonProcessingException {
        T e = service.update(entity);
        return new ResponseEntity<>(om.writeValueAsString(e), HttpStatus.OK);
    }
}