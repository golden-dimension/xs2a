package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @PutMapping(path = "/{encrypted-basket-id}/status/{status}")
    @ApiOperation(value = "Update transaction status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Object> updateTransactionStatus(
        @ApiParam(name = "encrypted-basket-id",
            value = "Encrypted basket ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-basket-id") String encryptedBasketId,
        @ApiParam(value = "The following code values are permitted 'ACTC', 'RCVD', 'RJCT', 'CANC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "AcceptedTechnicalValidation, Received, Rejected, Canceled, PartiallyAcceptedTechnicalCorrect",
            required = true)
        @PathVariable("status") String transactionStatus);

    @PutMapping(path = "/{encrypted-basket-id}/multilevel-sca")
    @ApiOperation(value = "Update requirement for multilevel SCA for baskets")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Object> updateMultilevelScaRequired(
        @ApiParam(name = "encrypted-basket-id", value = "Encrypted basket ID", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("encrypted-basket-id") String encryptedBasketId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca);
}
