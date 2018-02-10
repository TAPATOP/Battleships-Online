package Source.Game;

public class EnumStringMessage {
    public EnumStringMessage(Enum enumValue, String message){
        this.enumValue = enumValue;
        this.message = message;
    }

    public Enum getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(Enum enumValue) {
        this.enumValue = enumValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // MEMBER VARIABLES
    Enum enumValue;
    String message;
}
