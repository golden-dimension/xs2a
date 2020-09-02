package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasket;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketCreationResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "api/v1/signing-baskets")
public interface CmsSigningBasketApi {
    @PostMapping
    @ApiOperation(value = "Create signing-basket")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = CmsSigningBasketCreationResponse.class),
        @ApiResponse(code = 204, message = "No Content")
    })
    ResponseEntity<Object> createSigningBasket(@RequestBody CmsSigningBasket request);

    @GetMapping
    @ApiOperation(value = "Get consents and payments")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = CmsSigningBasketConsentsAndPaymentsResponse.class),
        @ApiResponse(code = 204, message = "No Content")
    })
    ResponseEntity<Object> getConsentsAndPayments(@RequestParam List<String> consents, @RequestParam List<String> payments);
}
