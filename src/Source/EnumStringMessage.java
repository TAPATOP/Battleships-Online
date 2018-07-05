package Source;

public class EnumStringMessage {
    // Member variables
    private Enum enumValue;
    private String message;

    // Constructors //
    public EnumStringMessage(Enum enumValue, String message) {
        this.enumValue = enumValue;
        this.message = message;
    }

    // Methods
    public Enum getEnumValue() {
        return enumValue;
    }

    String getMessage() {
        return message;
    }
}
