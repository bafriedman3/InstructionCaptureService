package com.example.instructions.service;

// TradeService.java
import com.example.instructions.cache.CanonicalTradeCache;
import com.example.instructions.config.TradeProperties;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.example.instructions.model.TradeType;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@Service
public class TradeService {
    private final TradeProperties props;
    private final KafkaPublisher publisher;
    private final CanonicalTradeCache cache;
    private AtomicInteger successCSV = new AtomicInteger(0),
            failCSV = new AtomicInteger(0),
            successJson = new AtomicInteger(0),
            failJson = new AtomicInteger(0);

    public TradeService(TradeProperties props, KafkaPublisher publisher,
                        CanonicalTradeCache cache) {
        this.props = props;
        this.publisher = publisher;
        this.cache = cache;
    }

    public Map<String, Integer> processTradesFromCsv(MultipartFile file) throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            successCSV.set(0);
            failCSV.set(0);
            CSVParser parser = CSVParser.parse(
                    reader,
                    CSVFormat.DEFAULT.builder()
                            .setHeader()          // first row is a header
                            .setSkipHeaderRecord(true)
                            .build()
            );
            StreamSupport.stream(parser.spliterator(), false)
                    .map(this::optionalValidateRecord)
                    .flatMap(Optional::stream)
                    .map(this::createCanonicalTrade)
                    .peek(canTrade -> cache.put(canTrade.tradeId(), canTrade))
                    .map(TradeTransformer::createPlatformTrade)
                    .forEach(this::publishTrade);
        }
        int total = successCSV.get() + failCSV.get();
        return Map.of("success", successCSV.get(), "fail", failCSV.get(),
                "total", total);
    }

    public Map<String, Integer> processTradesFromJson(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        successJson.set(0);
        failJson.set(0);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.lines().forEach(line -> {
                try {
                    // parse each line as a separate CanonicalTrade
                    CanonicalTrade trade = mapper.readValue(line, CanonicalTrade.class);

                    // validate and publish if valid
                    Optional<CanonicalTrade> optTrade = optionalValidateTrade(trade);
                    optTrade.ifPresentOrElse(t -> {
                        successJson.incrementAndGet();
                        cache.put(t.tradeId(), t);
                        publishTrade(TradeTransformer.createPlatformTrade(t));
                    }, ()->failJson.incrementAndGet());
                } catch (JsonProcessingException e) {
                    // malformed JSON line, skip and log
                    System.out.println("Skipping malformed JSON line " + e.getMessage());
                    failJson.incrementAndGet();
                }

            });
        }
        int total = successJson.get() + failJson.get();
        return Map.of("success", successJson.get(), "fail", failJson.get(),
                "total", total);
    }


    public Optional<CanonicalTrade> optionalValidateTrade(CanonicalTrade trade) {
        try {
            validateCanTrade(trade);
            return Optional.of(trade);
        }
        catch(Exception e) {
            System.out.println("Exception processing record with trade_id " +
                    trade.tradeId() + " message: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<CSVRecord> optionalValidateRecord(CSVRecord record) {
        try {
            validateRecord(record);
            successCSV.incrementAndGet();
            return Optional.of(record);
        }
        catch(Exception e) {
            System.out.println("Exception processing record with trade_id " +
                    record.get(props.getFields().getTradeId()) + " message: " + e.getMessage());
            failCSV.incrementAndGet();
            return Optional.empty();
        }
    }

    private void validateNonStrFields(String amt, String timeStamp) {
        try {
            Integer.parseInt(amt);
        } catch (Exception e) {
            throw new NumberFormatException("The amount: " + amt + " is not an integer");
        }
        try {
            Instant.parse(timeStamp);
        } catch (Exception e) {
            throw new DateTimeException("DateTime " + timeStamp + " is of incorrect format");
        }
    }

    private void validateRecord(CSVRecord record) {
        if (record.size() != props.getRecordSize()) {
            throw new IllegalArgumentException("Size of records should be " + props.getRecordSize()
                    + ", got "
                    + record.size());
        }
        String amtStr = record.get(props.getFields().getQuantity());
        String tsStr = record.get(props.getFields().getTimestamp());
        validateNonStrFields(amtStr, tsStr);
        CanonicalTrade canTrade = new CanonicalTrade(record.get(props.getFields().getTradeId()),
                record.get(props.getFields().getAcctNum()),
                record.get(props.getFields().getSecId()), record.get(props.getFields().getTradeType()),
                Integer.parseInt(amtStr), record.get(props.getFields().getTimestamp()));

        validateCanTrade(canTrade);
    }

    private void validateCanTrade(CanonicalTrade canTrade) {
        if (!canTrade.tradeId().matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("Trade id for trade " + canTrade.tradeId() +
                    " is incorrect: only letters and digits are allowed");
        }
        if (canTrade.accountNumber().length() > props.getMaxAccountNumSize()) {
            throw new IllegalArgumentException("Account number length for trade" + canTrade.tradeId()
                    +" is beyond allowed "
                    + "actual: " + canTrade.accountNumber().length() + " max allowed: "
                    + props.getMaxAccountNumSize());
        }
        if (!canTrade.accountNumber().matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("Account no for trade " + canTrade.tradeId() +
                    " is incorrect: only letters and digits are allowed");
        }
        String sec = canTrade.securityId();
        if (sec.length() > props.getMaxSecIdSize()) {
            throw new IllegalArgumentException("Security id length is beyond allowed "
                    + "actual: " + sec.length() + " max allowed: " + props.getMaxSecIdSize());
        }
        if (!sec.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("Security id format is incorrect: only letters and " +
                    "digits are allowed");
        }
        String type = canTrade.tradeType();
        if (!TradeType.validType(type)) {
            throw new IllegalArgumentException("Trade Type " + type + " is not recognized");
        }
    }

    private CanonicalTrade createCanonicalTrade(CSVRecord record) {
        return new CanonicalTrade(record.get(props.getFields().getTradeId()), record.get(props.getFields().getAcctNum()),
                record.get(props.getFields().getSecId()), record.get(props.getFields().getTradeType()),
                Integer.parseInt(record.get(props.getFields().getQuantity())),
                record.get(props.getFields().getTimestamp()));
    }

    private void publishTrade(PlatformTrade trade) {
        try {
            publisher.publish(trade);
        }
        catch(Exception e) {
            System.out.println("Exception publishing trade :" + trade + " message: " + e.getMessage());
        }
    }
}
