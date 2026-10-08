package com.ztech.restaurant.testfolder;

import java.time.Instant;
import java.util.List;

/** Shapes mirror openApi.yaml so the real service can replace this stub without BFF/plugin changes. */
public final class Models {
    private Models() {}

    public record MenuItem(String id, String categoryId, String name, String description,
                           long priceMinor, String currency, boolean available, List<String> modifierIds) {}
    public record MenuCategory(String id, String name, int displayOrder, List<MenuItem> items) {}
    public record Modifier(String id, String name, long priceMinor, String currency) {}
    public record Menu(String id, String merchantId, String name, boolean active,
                       List<MenuCategory> categories, List<Modifier> modifiers) {}

    public enum OrderStatus {
        RECEIVED, PREPARING, READY, SERVED, CANCELLED;

        /** Next forward status, or null if terminal. */
        public OrderStatus next() {
            return switch (this) {
                case RECEIVED -> PREPARING;
                case PREPARING -> READY;
                case READY -> SERVED;
                default -> null;
            };
        }
    }

    public record OrderSummary(String orderId, OrderStatus status, Instant placedAt) {}
    public record OrderLine(String menuItemId, String menuItemName, int quantity, List<String> modifierIds, String note) {}
    public record OrderDetail(String orderId, String branchId, String tableId, OrderStatus status,
                              List<OrderLine> lines, Instant placedAt, Instant updatedAt) {
        public OrderDetail withStatus(OrderStatus s) {
            return new OrderDetail(orderId, branchId, tableId, s, lines, placedAt, Instant.now());
        }
        public OrderSummary summary() { return new OrderSummary(orderId, status, placedAt); }
    }
    public record OrderStatusUpdate(OrderStatus status) {}
}
