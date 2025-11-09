package com.example.instructions.service;

import com.example.instructions.model.CanonicalTrade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeServiceTest {
    @Autowired
    private TradeService tradeService;

    @Test
    void optionalValidateTrade() {
        CanonicalTrade trade = new CanonicalTrade("Trade123", "ACCT12345",
                "AAPL", "Buy", 2000, "2025-11-08T12:25:00Z");
        var opt = tradeService.optionalValidateTrade(trade);
        assertTrue(opt.isPresent(), "Trade is validated");

        CanonicalTrade trade1 = new CanonicalTrade("Trade123", "ACCT12345",
                "AAPL++", "Buy", 2000, "2025-11-08T12:25:00Z");
        var opt1 = tradeService.optionalValidateTrade(trade1);
        assertTrue(opt1.isEmpty(), "Trade is invalid");

        CanonicalTrade trade2 = new CanonicalTrade("Trade123", "ACCT12345",
                "AAPL", "Cancel", 2000, "2025-11-08T12:25:00Z");
        var opt2 = tradeService.optionalValidateTrade(trade2);
        assertTrue(opt2.isEmpty(), "Trade is invalid");
    }

    @Test
    void testProcessTradesFromCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trades.csv",
                "text/csv",
                ("trade_id,account_number,security_id,trade_type,quantity,timestamp\n" +
                        "TRADE1234,ACCT1234,AAPL,Buy,300,2025-11-06T12:35:00Z\n" +
                        "badTrade\n" +
                        "TRADE456,ACCT4321,MSFT,Sell,500,2025-11-06T13:25:00Z").getBytes()
        );

        Map<String, Integer> result = tradeService.processTradesFromCsv(file);

        assertEquals(2, result.get("success"));
        assertEquals(1, result.get("fail"));
    }
}