package com.example.instructions.controller;

import com.example.instructions.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService service;

    public TradeController(TradeService service) {
        this.service = service;
    }

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload CSV or JSON file containing trades")
    public Map<String, Integer> upload(
            @Parameter(description = "CSV or JSON file", required = true)
            @RequestParam("file") MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if(fileName == null) {
            throw new IllegalArgumentException("Filename is null");
        }
        if(fileName.endsWith(".csv")) {
            return service.processTradesFromCsv(file);
        }
        else if(fileName.endsWith(".json")) {
            return service.processTradesFromJson(file);
        }
        else {
            throw new IllegalArgumentException("file " + fileName + "is not supported "
                    + " only .csv and .json files are supported");
        }
    }
}


