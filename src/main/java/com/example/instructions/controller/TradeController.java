package com.example.instructions.controller;

import com.example.instructions.service.TradeService;
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

    @PostMapping("/upload")
    public Map<String, Integer> upload(@RequestParam("file") MultipartFile file) throws Exception {
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


