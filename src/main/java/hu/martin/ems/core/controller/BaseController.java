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

//    @GetMapping(path = "/findAll", produces = StaticDatas.Produces.JSON)
    @RequestMapping(path = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<T> allElements = service.findAll(withDeleted);
        if(allElements != null){
            return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/findAllWithGraph", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAllWithGraph(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<T> allElements = service.findAllWithGraph(withDeleted);
        if(allElements != null){
            return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path = "/findAllByIds", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAllByIds(@RequestParam(required = false, defaultValue = "false") List<Long> ids) {
        List<T> allElements = service.findAllByIds(ids);
        if(allElements != null){
            return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(path = "/restore", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> restore(@RequestBody T entity) {
        T restoredEntity = service.restore(entity);
        if(restoredEntity != null){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(path = "/delete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> delete(@RequestBody T entity) {
        T deleted = service.delete(entity);
        if(deleted != null){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(path = "/permanentlyDelete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> permanentlyDelete(@RequestParam(value = "id") Long entityId) {
        T permanentlyDeleted = service.permanentlyDelete(entityId);
        if(permanentlyDeleted != null){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/save", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> save(@RequestBody T entity) {
        T saved = service.save(entity);
        if(entity != null){
            return new ResponseEntity<>(gson.toJson(saved), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(path = "/update", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> update(@RequestBody T entity) {
        T e = service.update(entity);
        if(e != null){
            return new ResponseEntity<>(gson.toJson(e), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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