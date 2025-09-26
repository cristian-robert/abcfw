package com.spring.befwlc.feature_methods;


import com.spring.befwlc.context.ApplicationContextProvider;
import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.context.ScenarioContextKeys;
import com.spring.befwlc.utils.DateUtils;
import com.spring.befwlc.utils.TransactionDataUtils;


public enum StaticFeatureMethod implements StaticMethod {

    $FULL_UUID( "Generates UUID of format '<UUID string of 32 characters>'") {
        public String value() { return TransactionDataUtils.defaultUUID(); }
    },

    $SHORT_UUID("Generates UUID of format '<+>UUID string of 32 characters without '-' separator characters>'") {
        public String value() { return TransactionDataUtils.UUIDWithoutSeparator(); }
    },

    $5_RANDOM_CHARS("Generates string of 5 random characters") {
        public String value() { return TransactionDataUtils.defaultUUID().substring(0, 5); }
    },

    $MS_UUID( "Generates UUID of format 'MS<UUID string of 32 characters without '-' separator character>'") {
        public String value() { return TransactionDataUtils.MSUUID(); }
    },

    $SYS_SHORT_DATE( "Generates date of format '%s'", DateUtils.SHORT_DATE_FORMAT) {
        public String value() { return DateUtils.shortDateString(); }
    },

    $SYS_FULL_OFFSET_DATE( "Generates date of format '%s'", DateUtils.FULL_DATE_AND_OFFSET_FORMAT) {
        public String value() { return DateUtils.fullSysOffsetDate(); }
    },

    $SYS_FULL_DATE_PLUS_ONE_HOUR( "Generates current timestamp plus one hour of format '%s'", DateUtils.FULL_DATE_AND_OFFSET_FORMAT) {
        public String value() { return DateUtils.fullSysDatePlusOneHour(); }
    },

    $SYS_FULL_DATE( "Generates current timestamp of format '%s'", DateUtils.FULL_DATE_FORMAT) {
        public String value() { return DateUtils.fullSysDate(); }
    },

    $SYS_ISO_DATE( "Generates current timestamp of format '%s'", DateUtils.ISO_DATE_FORMAT) {
        public String value() { return DateUtils.fullIsoDate(); }
    },

    $MISSING( "Removes the field from the payload template") {
        public String value() { return this.name(); }
    },

    $EMPTY_STRING( "Generates empty string value") {
        public String value() { return ""; }
    },

    $SPOT_SECRET( "Generates SPOT secret") {
        public String value() { return "@bracadabra"; }
    },

    $COMMA( "Returns ,") {
        public String value() { return ","; }
    },

    $BLANK_SPACE( "Returns blank space string") {
        public String value() { return " "; }
    },

    $TASR_REF( "Returns 12 digit number") {
        public synchronized String value() { return DateUtils.sysDateOfFormat("MMddHmmssSSSSS").toString(); }
    },

    $LAST_PAYLOAD( "Last payload used") {
        public String value() {
            return ApplicationContextProvider.getBean(ScenarioContext.class)
                    .get(ScenarioContextKeys.LAST_PAYLOAD, String.class);
        }
    };

    private final String description;

    StaticFeatureMethod(final String description, final Object... args) {
        this.description = args != null && args.length > 0 ? String.format(description, args) : description;
    }

    public String description() { return description; }
}