package com.nisimsoft.auth_system.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nisimsoft.auth_system.dtos.requests.SaveCorpRequest;
import com.nisimsoft.auth_system.dtos.requests.UpdateCorpRequest;
import com.nisimsoft.auth_system.dtos.responses.user.CorporationResponseDTO;
import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;
import com.nisimsoft.auth_system.repositories.CorporationRepository;
import com.nisimsoft.auth_system.responses.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CorporationController {

    @Autowired
    private CorporationRepository corporationRepository;

    @PostMapping("/corporation")
    public ResponseEntity<?> saveCorporation(@Valid @RequestBody SaveCorpRequest request) {

        Corporation corporation = new Corporation();

        corporation.setDbEngine(NSCorpDBEngineEnum.valueOf(request.getDbEngine()));
        corporation.setDbName(request.getDbName());
        corporation.setHost(request.getHost());
        corporation.setName(request.getName());
        corporation.setUsername(request.getUsername());
        corporation.setPassword(request.getPassword());

        corporationRepository.save(corporation);
        CorporationResponseDTO responseDTO = new CorporationResponseDTO(corporation.getId(), corporation.getName());

        return new Response(
                "Corporación guardada realizada exitosamente", responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/corporation")
    public ResponseEntity<?> updateCorporation(@Valid @RequestBody UpdateCorpRequest request) {

        Corporation corporation = corporationRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Corporación no encontrada"));

        corporation.setDbEngine(NSCorpDBEngineEnum.valueOf(request.getDbEngine()));
        corporation.setDbName(request.getDbName());
        corporation.setHost(request.getHost());
        corporation.setName(request.getName());
        corporation.setUsername(request.getUsername());
        corporation.setPassword(request.getPassword());

        corporationRepository.save(corporation);

        CorporationResponseDTO responseDTO = new CorporationResponseDTO(request.getId(), corporation.getName());

        return new Response(
                "Corporación actualizada realizada exitosamente", responseDTO, HttpStatus.OK);
    }
}
