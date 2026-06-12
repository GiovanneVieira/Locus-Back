package com.project.locusapi.constant;

import lombok.Getter;

@Getter
public enum RentalStatus {
    CONFIRMED("confirmed"),
    PENDING("pending"),
    DECLINED("declined"),
    CANCELLED("cancelled");

    private final String rentalString;

    RentalStatus(String rentalString){
        this.rentalString = rentalString;
    }

}
