package com.minicodex.verify;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VerifyObservation {


    private boolean success;


    private List<String> errors;


}