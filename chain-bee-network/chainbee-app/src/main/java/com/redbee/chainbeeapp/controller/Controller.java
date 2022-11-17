package com.redbee.chainbeeapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redbee.chainbeeapp.dto.CreateBeeRequest;
import com.redbee.chainbeeapp.dto.FinancialUpdateRequest;
import com.redbee.chainbeeapp.dto.ManagementUpdateRequest;
import com.redbee.chainbeeapp.model.Bee;
import com.redbee.chainbeeapp.service.ChainbeeService;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/bee")
@Slf4j
public class Controller {

    private final ChainbeeService service;

    public Controller(ChainbeeService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bee> get(@PathVariable String id) throws GatewayException, JsonProcessingException {
        log.info("Searching bee with id: {}", id);
        var bee = service.getBee(id);
        return ResponseEntity.ok(bee);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<Bee>> getHistory(@PathVariable String id) throws GatewayException, JsonProcessingException {
        log.info("Searching for bee history with id: {}", id);
        return ResponseEntity.ok(service.getHistory(id));
    }

    @PostMapping()
    public ResponseEntity<Bee> create(@Valid @RequestBody CreateBeeRequest request) throws EndorseException, CommitException, SubmitException, CommitStatusException {
        log.info("Creating bee with body: {}", request.toString());
        var bee = Bee.builder()
            .id(request.getId().toString())
            .name(request.getName())
            .seniority(request.getSeniority())
            .isActive(true)
            .build();
        service.createAsset(bee);
        return ResponseEntity.status(HttpStatus.CREATED).body(bee);
    }

    @PatchMapping("/management-updates")
    public ResponseEntity managementUpdate(@Valid @RequestBody ManagementUpdateRequest request) throws EndorseException, SubmitException, CommitStatusException {
        log.info("Bee update with body {}", request.toString());
        service.asyncUpdate(Bee.builder()
                .id(request.getId())
                .manager(request.getManager())
                .assignment(request.getAssignment())
                .seniority(request.getSeniority())
                .build(),
            ChainbeeService.TX_MANAGEMENT);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/financial-updates")
    public ResponseEntity financialUpdate(@Valid @RequestBody FinancialUpdateRequest request) throws EndorseException, SubmitException, CommitStatusException {
        log.info("Bee update with body {}", request.toString());
        service.asyncUpdate(Bee.builder()
                .id(request.getId())
                .salary(request.getSalary())
                .hasMixedSalary(request.isMixedSalary())
                .build(),
            ChainbeeService.TX_FINANCIAL);
        return ResponseEntity.noContent().build();
    }
}
