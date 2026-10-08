package com.ztech.restaurant.testfolder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * In-memory data for one seeded merchant. Resets on restart.
 */
@Component
public class RestaurantStore {
    private final Map<String, Models.Menu> menus = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Models.OrderDetail>> orders = new ConcurrentHashMap<>();

    public RestaurantStore(@Value("${restaurant.seed.merchant-id}") String merchantId) {
        var mods = List.of(new Models.Modifier("mod_milk", "Extra milk", 1000, "ETB"),
                new Models.Modifier("mod_spicy", "Extra spicy", 0, "ETB"));
        var drinks = new Models.MenuCategory("cat_drinks", "Drinks", 1, List.of(
                new Models.MenuItem("itm_macchiato", "cat_drinks", "Macchiato", "Espresso with milk foam", 6000, "ETB", true, List.of("mod_milk")),
                new Models.MenuItem("itm_juice", "cat_drinks", "Fresh juice", "Seasonal fruit", 8000, "ETB", true, List.of())));
        var mains = new Models.MenuCategory("cat_mains", "Mains", 2, List.of(
                new Models.MenuItem("itm_firfir", "cat_mains", "Firfir", "Injera with spiced sauce", 18000, "ETB", true, List.of("mod_spicy")),
                new Models.MenuItem("itm_tibs", "cat_mains", "Beef tibs", "Sauteed beef", 32000, "ETB", false, List.of("mod_spicy"))));
        menus.put(merchantId, new Models.Menu("menu_1", merchantId, "Main menu", true, List.of(drinks, mains), mods));

        Instant now = Instant.now();
        Map<String, Models.OrderDetail> o = new LinkedHashMap<>();
        o.put("ord_1001", order("ord_1001", "itm_macchiato", "Macchiato", 2, Models.OrderStatus.RECEIVED, now.minus(12, ChronoUnit.MINUTES)));
        o.put("ord_1002", order("ord_1002", "itm_firfir", "Firfir", 1, Models.OrderStatus.PREPARING, now.minus(25, ChronoUnit.MINUTES)));
        o.put("ord_1003", order("ord_1003", "itm_juice", "Fresh juice", 3, Models.OrderStatus.READY, now.minus(40, ChronoUnit.MINUTES)));
        orders.put(merchantId, new ConcurrentHashMap<>(o));
    }

    private Models.OrderDetail order(String id, String itemId, String name, int qty, Models.OrderStatus s, Instant at) {
        return new Models.OrderDetail(id, "br_1", "t_" + id.substring(4), s,
                List.of(new Models.OrderLine(itemId, name, qty, List.of(), null)), at, at);
    }

    public Models.Menu menu(String merchantId) {
        Models.Menu m = menus.get(merchantId);
        if (m == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "RESTAURANT_NOT_FOUND", "No menu for this merchant.");
        return m;
    }

    public List<Models.OrderSummary> listOrders(String merchantId) {
        return orders.getOrDefault(merchantId, Map.of()).values().stream()
                .sorted(Comparator.comparing(Models.OrderDetail::placedAt).reversed())
                .map(Models.OrderDetail::summary).toList();
    }

    public Models.OrderDetail advance(String merchantId, String orderId, Models.OrderStatus requested) {
        Map<String, Models.OrderDetail> mine = orders.getOrDefault(merchantId, Map.of());
        if (!mine.containsKey(orderId))
            throw new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found.");
        return ((ConcurrentHashMap<String, Models.OrderDetail>) mine).compute(orderId, (id, cur) -> {
            if (requested == null || requested != cur.status().next())
                throw new ApiException(HttpStatus.CONFLICT, "INVALID_ORDER_TRANSITION",
                        "Cannot move order from " + cur.status() + " to " + requested + ".");
            return cur.withStatus(requested);
        });
    }
}
