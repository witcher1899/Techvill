package utils;


import java.math.BigDecimal;


//утилитарный класс для приведения значений в запросе к единому типу данных для assert
public final class TypeParsing {

    private TypeParsing() {
    }

    public static BigDecimal parse(double value) {
        return new BigDecimal(BigDecimal.valueOf(value).stripTrailingZeros().toPlainString());
    }

    public static BigDecimal parse(String value) {
        return new BigDecimal(value);
    }
}
