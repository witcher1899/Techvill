package test_data.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum OrderType {

    CERTIFICATE(null, "website"),
    WATER(null, "website"),
    DARKKITCHEN(80, "darkkitchen_retail"),
    VKUSVILL_GO(78, "vkusvill_go"),
    VKUSVILL_COOKS(79, "vkusvill_cooks"),
    MICROMARCET(96, "micromarket"),
    VKUSVILCHIK(24, "vkusvillchik");

    private final Integer creationSource;
    private final String value;

    public static OrderType getCreationSource(int creationSource) {
        for (OrderType type : values()) {
            if (type.creationSource == creationSource) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported creation source: " + creationSource);
    }
}
