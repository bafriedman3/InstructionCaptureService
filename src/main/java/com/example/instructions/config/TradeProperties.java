package com.example.instructions.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "trade")
public class TradeProperties {
    private int maxAccountNumSize;
    private long maxFileSize;
    private int maxSecIdSize;
    private int recordSize;
    private Fields fields = new Fields();

    public static class Fields {
        private String tradeId;
        private String acctNum;
        private String secId;
        private String tradeType;
        private String quantity;
        private String timestamp;

        public String getTradeId() {
            return tradeId;
        }

        public void setTradeId(String tradeId) {
            this.tradeId = tradeId;
        }

        public String getAcctNum() {
            return acctNum;
        }

        public void setAcctNum(String acctNum) {
            this.acctNum = acctNum;
        }

        public String getSecId() {
            return secId;
        }

        public void setSecId(String secId) {
            this.secId = secId;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    // getters and setters
    public Fields getFields() {
        return fields;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public int getMaxSecIdSize() {
        return maxSecIdSize;
    }

    public void setMaxSecIdSize(int maxSecIdSize) {
        this.maxSecIdSize = maxSecIdSize;
    }
    public int getMaxAccountNumSize() { return maxAccountNumSize; }
    public void setMaxAccountNumSize(int maxAccountNumSize) { this.maxAccountNumSize = maxAccountNumSize; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
}