package com.minicodex.verify;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
public class VerifyResult {


    private boolean success;


    @Builder.Default
    private List<String> errors =
            new ArrayList<>();



    public static VerifyResult success(){

        return VerifyResult.builder()
                .success(true)
                .build();

    }



    public static VerifyResult failed(
            List<String> errors
    ){

        return VerifyResult.builder()
                .success(false)
                .errors(errors)
                .build();

    }


}