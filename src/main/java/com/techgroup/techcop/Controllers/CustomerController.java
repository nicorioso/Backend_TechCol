package com.techgroup.techcop.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Customer")
public class CustomerController {

    @GetMapping
    public ResponseEntity<?> hola() {
        return ResponseEntity.ok("Hola");
    }
}
