package Source.Game;

public class EnumStringMessage {
    public EnumStringMessage(Enum enumValue, String message){
        this.enumValue = enumValue;
        this.message = message;
    }

    public Enum getEnumValue() {
        return enumValue;
    }

    public String getMessage() {
        return message;
    }

    // MEMBER VARIABLES
    private Enum enumValue;
    private String message;
}
