/*
 * Copyright 2011 Blazebit
 */
package com.blazebit.text;

import com.blazebit.reflection.ReflectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

/**
 * @author Christian Beikov
 */
public final class FormatUtils {

    private static final Map<Class<? extends Serializable>, SerializableFormat<? extends Serializable>> parseableTypes = new HashMap<Class<? extends Serializable>, SerializableFormat<? extends Serializable>>();

    static {
        parseableTypes.put(Integer.TYPE, new IntegerFormat());
        parseableTypes.put(Long.TYPE, new LongFormat());
        parseableTypes.put(Double.TYPE, new DoubleFormat());
        parseableTypes.put(Float.TYPE, new FloatFormat());
        parseableTypes.put(Boolean.TYPE, new BooleanFormat());
        parseableTypes.put(Character.TYPE, new CharacterFormat());
        parseableTypes.put(Byte.TYPE, new ByteFormat());
        parseableTypes.put(Short.TYPE, new ShortFormat());
        parseableTypes.put(Integer.class, new IntegerFormat());
        parseableTypes.put(Long.class, new LongFormat());
        parseableTypes.put(Double.class, new DoubleFormat());
        parseableTypes.put(Float.class, new FloatFormat());
        parseableTypes.put(Boolean.class, new BooleanFormat());
        parseableTypes.put(Character.class, new CharacterFormat());
        parseableTypes.put(Byte.class, new ByteFormat());
        parseableTypes.put(Short.class, new ShortFormat());
        parseableTypes.put(String.class, new StringFormat());
        parseableTypes.put(BigDecimal.class, new BigDecimalFormat());
        parseableTypes.put(Date.class, new DateFormat());
        parseableTypes.put(Calendar.class, new CalendarFormat());
        parseableTypes.put(Locale.class, new LocaleFormat());
        parseableTypes.put(TimeZone.class, new TimeZoneFormat());
        parseableTypes.put(Currency.class, new CurrencyFormat());
        parseableTypes.put(Class.class, new ClassFormat());
    }

    private static class ParserContextImpl implements ParserContext {

        private final Map<String, Object> contextMap = new HashMap<String, Object>();

        public Object getAttribute(String name) {
            return contextMap.get(name);
        }

        public void setAttribute(String name, Object value) {
            contextMap.put(name, value);
        }
    }

    private FormatUtils() {
    }

    /**
     * Returns the available serializable formatters.
     *
     * @return The available serializable formatters.
     */
    public static Map<Class<? extends Serializable>, SerializableFormat<? extends Serializable>> getAvailableFormatters() {
        return Collections.unmodifiableMap(parseableTypes);
    }

    /**
     * This method checks if the given type is a parseable type. Parseable types
     * are types that can be converted from a string to an object. Every type
     * that is parseable has a #{@link SerializableFormat} implementation that
     * can be used for conversion. <br/>
     * For the following types implementations are already mapped to their
     * classes:
     * <ul>
     * <li>primitive int and #{@link Integer}</li>
     * <li>primitive long and #{@link Long}</li>
     * <li>primitive double and #{@link Double}</li>
     * <li>primitive float and #{@link Float}</li>
     * <li>primitive boolean and #{@link Boolean}</li>
     * <li>primitive char and #{@link Character}</li>
     * <li>primitive byte and #{@link Byte}</li>
     * <li>primitive short and #{@link Short}</li>
     * <li>#{@link String}</li>
     * <li>#{@link BigDecimal}</li>
     * <li>#{@link BigInteger}</li>
     * <li>#{@link Date}</li>
     * <li>#{@link Calendar}</li>
     * <li>#{@link Locale}</li>
     * <li>#{@link TimeZone}</li>
     * <li>#{@link Currency}</li>
     * <li>#{@link Class}</li>
     * </ul>
     *
     * @param type The type to be checked if parseable
     * @return true if the type is parseable, otherwise false
     */
    public static boolean isParseableType(Class<?> type) {
        return parseableTypes.containsKey(type);
    }

    /**
     * Returns the parsed object of the given type for the given string.
     * Invoking this method is equal to invoking #
     * {@link ReflectionUtils#getParsedValue(java.lang.Class, java.lang.String, java.text.DateFormat, java.text.DateFormat)}
     * and the 3rd argument <code>DateFormat.getDateTimeInstance()</code>
     *
     * @param returnType The type to which the value should be parsed
     * @param value      The string that should be parsed
     * @return The parsed object
     * @throws ParseException Is thrown when the string can not be parsed
     * @see ReflectionUtils#isParseableType(java.lang.Class)
     * @see ReflectionUtils#getParsedValue(java.lang.Class, java.lang.String,
     * java.text.DateFormat, java.text.DateFormat)
     */
    public static <T extends Serializable> T getParsedValue(
            Class<T> returnType, String value) throws ParseException {
        return getParsedValue(returnType, value,
                java.text.DateFormat.getDateTimeInstance());
    }

    /**
     * Returns the parsed object of the given type for the given string. Based
     * on the registered #{@link SerializableFormat}s an instance is choosen
     * based on the class object. The parsing of the string value may fail,
     * throwing a ParseException.
     *
     * @param returnType    The type to which the value should be parsed
     * @param value         The string that should be parsed
     * @param dateFormatter The date format which should be used for string to
     *                      date/calendar conversion
     * @return The parsed object
     * @throws ParseException Is thrown when the string can not be parsed
     * @see ReflectionUtils#isParseableType(java.lang.Class)
     */
    public static <T extends Serializable> T getParsedValue(
            Class<T> returnType, String value,
            java.text.DateFormat dateFormatter) throws ParseException {
        SerializableFormat<T> formatter = (SerializableFormat<T>) parseableTypes
                .get(returnType);

        if (formatter == null) {
            throw new IllegalArgumentException("Unknown return type");
        }

        ParserContextImpl ctx = new ParserContextImpl();
        ctx.setAttribute("format", dateFormatter);

        return formatter.parse(value, ctx);
    }

    public static <T extends Serializable> String getFormattedValue(
            Class<T> type, T object) {
        return getFormattedValue(type, object,
                java.text.DateFormat.getDateTimeInstance());
    }

    public static <T extends Serializable> String getFormattedValue(
            Class<T> type, T object, java.text.DateFormat dateFormatter) {
        SerializableFormat<T> formatter = (SerializableFormat<T>) parseableTypes
                .get(type);

        if (formatter == null) {
            throw new IllegalArgumentException("Unknown return type");
        }

        ParserContextImpl ctx = new ParserContextImpl();
        ctx.setAttribute("format", dateFormatter);

        return formatter.format(object, ctx);
    }
}
