package Source;

public class EnumStringMessage {
    public EnumStringMessage(Enum enumValue, String message) {
        this.enumValue = enumValue;
        this.message = message;
    }

    public Enum getEnumValue() {
        return enumValue;
    }

    String getMessage() {
        return message;
    }

    // MEMBER VARIABLES
    private Enum enumValue;
    private String message;
}
