package dev.vepo.maestro.framework.sample;

/**
 * Stock Price from Yahoo Finance.
 */
public record StockPrice(String id,
                         double price,
                         long time,
                         String currency,
                         String exchange,
                         String quoteType,
                         String marketHours,
                         double changePercent,
                         String dayVolume,
                         double dayHigh,
                         double dayLow,
                         double change,
                         double openPrice,
                         String lastSize,
                         String priceHint,
                         String vol24hr,
                         String volAllCurrencies,
                         String fromcurrency,
                         double circulatingSupply,
                         double marketcap) {
}
