package com.ztech.restaurant.testfolder;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/merchants/{merchantId}")
@Tag(name = "Restaurant", description = "Restaurant menu and order management")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {
    private final RestaurantStore store;

    public RestaurantController(RestaurantStore store) {
        this.store = store;
    }

    @GetMapping("/menu")
    @Operation(summary = "Get restaurant menu", description = "Returns the menu belonging to the specified merchant")
    @ApiResponse(responseCode = "200", description = "Menu retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Access to this merchant is denied")
    public Models.Menu getMenu(@PathVariable String merchantId, @AuthenticationPrincipal Jwt jwt) {
        assertSameMerchant(merchantId, jwt);
        return store.menu(merchantId);
    }

    @GetMapping("/orders")
    @Operation(summary = "List restaurant orders", description = "Returns orders belonging to the specified merchant")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Access to this merchant is denied")
    public List<Models.OrderSummary> listOrders(
            @Parameter(description = "Unique merchant identifier")
            @PathVariable String merchantId,
            @AuthenticationPrincipal Jwt jwt) {
        assertSameMerchant(merchantId, jwt);
        return store.listOrders(merchantId);
    }

    @PatchMapping("/orders/{orderId}")
    @Operation(summary = "Advance order status", description = "Updates an order to its next permitted status")
    @ApiResponse(responseCode = "200", description = "Order status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Access to this merchant is denied")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public Models.OrderDetail advanceOrderStatus(
            @Parameter(description = "Unique merchant identifier")
            @PathVariable String merchantId, @PathVariable String orderId,
            @Parameter(description = "Unique order identifier")
            @RequestBody Models.OrderStatusUpdate body,
            @AuthenticationPrincipal Jwt jwt) {
        assertSameMerchant(merchantId, jwt);
        return store.advance(merchantId, orderId, body.status());
    }

    /**
     * Second isolation layer: the path merchant must equal the token's merchant_id.
     */
    private void assertSameMerchant(String merchantId, Jwt jwt) {
        String claim = jwt.getClaimAsString("merchant_id");
        if (claim == null || !claim.equals(merchantId))
            throw new ApiException(HttpStatus.FORBIDDEN, "RESTAURANT_ACCESS_DENIED", "Access to this merchant is denied.");
    }
}
