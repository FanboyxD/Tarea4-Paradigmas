package iquick.client;

public enum PlatformType {
    NORMAL(1),
    SPECIAL(22);
    
    private final int value;
    
    PlatformType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static PlatformType fromValue(int value) {
        for (PlatformType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return NORMAL; // Valor por defecto
    }
}