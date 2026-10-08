package com.ztech.restaurant.testfolder;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/merchants/{merchantId}")
public class RestaurantController {
    private final RestaurantStore store;
    public RestaurantController(RestaurantStore store) { this.store = store; }

    @GetMapping("/menu")
    public Models.Menu getMenu(@PathVariable String merchantId, @AuthenticationPrincipal Jwt jwt) {
        assertSameMerchant(merchantId, jwt);
        return store.menu(merchantId);
    }

    @GetMapping("/orders")
    public List<Models.OrderSummary> listOrders(@PathVariable String merchantId, @AuthenticationPrincipal Jwt jwt) {
        assertSameMerchant(merchantId, jwt);
        return store.listOrders(merchantId);
    }

    @PatchMapping("/orders/{orderId}")
    public Models.OrderDetail advanceOrderStatus(@PathVariable String merchantId, @PathVariable String orderId,
                                                 @RequestBody Models.OrderStatusUpdate body, @AuthenticationPrincipal Jwt jwt) {
        assertSameMerchant(merchantId, jwt);
        return store.advance(merchantId, orderId, body.status());
    }

    /** Second isolation layer: the path merchant must equal the token's merchant_id. */
    private void assertSameMerchant(String merchantId, Jwt jwt) {
        String claim = jwt.getClaimAsString("merchant_id");
        if (claim == null || !claim.equals(merchantId))
            throw new ApiException(HttpStatus.FORBIDDEN, "RESTAURANT_ACCESS_DENIED", "Access to this merchant is denied.");
    }
}
