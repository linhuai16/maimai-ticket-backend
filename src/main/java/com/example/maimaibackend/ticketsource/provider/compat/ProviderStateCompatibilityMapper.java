package com.example.maimaibackend.ticketsource.provider.compat;

import com.example.maimaibackend.ticketsource.provider.enums.*;

/** 将丰富的统一状态收敛为当前本地/鸿蒙已经使用的状态，不反向暴露 Provider 状态。 */
public final class ProviderStateCompatibilityMapper {
    private ProviderStateCompatibilityMapper() {}

    public static LocalSaleState toLocalSaleState(ProjectStatus status) {
        if (status == null) return LocalSaleState.OFFLINE;
        return switch (status) {
            case PENDING_SALE, PRESALE -> LocalSaleState.PRESALE;
            case ON_SALE -> LocalSaleState.ON_SALE;
            case SOLD_OUT -> LocalSaleState.SOLD_OUT;
            case CANCELLED, ENDED -> LocalSaleState.ENDED;
            case DRAFT, SUSPENDED, UNKNOWN -> LocalSaleState.OFFLINE;
        };
    }

    public static LocalSaleState toLocalSaleState(TicketProductSaleStatus status) {
        if (status == null) return LocalSaleState.OFFLINE;
        return switch (status) {
            case ON_SALE -> LocalSaleState.ON_SALE;
            case SOLD_OUT -> LocalSaleState.SOLD_OUT;
            case SALE_REMINDER, STOCK_REGISTRATION -> LocalSaleState.PRESALE;
            case NOT_ON_SALE, SUSPENDED, UNKNOWN -> LocalSaleState.OFFLINE;
        };
    }

    public static FrontendOrderState toFrontendOrderState(ProviderOrderStatus status) {
        if (status == null) return FrontendOrderState.CANCELED;
        return switch (status) {
            case CREATING, RESERVED -> FrontendOrderState.WAIT_PAY;
            case PAID, ISSUING, ISSUED, PARTIALLY_ISSUED -> FrontendOrderState.WAIT_USE;
            case REFUNDING -> FrontendOrderState.REFUNDING;
            case REFUNDED -> FrontendOrderState.REFUND_SUCCESS;
            case CANCELLED, EXPIRED, FAILED, UNKNOWN -> FrontendOrderState.CANCELED;
        };
    }

    public static FrontendTicketState toFrontendTicketState(ProviderTicketStatus status) {
        if (status == null) return FrontendTicketState.ERROR;
        return switch (status) {
            case GENERATING -> FrontendTicketState.GENERATING;
            case UNUSED -> FrontendTicketState.UNUSED;
            case USED -> FrontendTicketState.CHECKED;
            case VOIDED, EXPIRED -> FrontendTicketState.EXPIRED;
            case ERROR -> FrontendTicketState.ERROR;
        };
    }

    public static boolean isTradeable(ProjectStatus project, TicketProductSaleStatus product, StockState stock) {
        return project == ProjectStatus.ON_SALE
                && product == TicketProductSaleStatus.ON_SALE
                && stock != StockState.SOLD_OUT
                && stock != StockState.UNKNOWN;
    }
}
