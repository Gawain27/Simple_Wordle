package usr.gwn27;

enum Colors {
    RESET("\u001B[0m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    YELLOW_BOLD("\033[1;33m");
    /*
    GREEN_BACK("\u001B[42m"),
    WHITE_BACK("\u001B[47m"),
    YELLOW_UNDER("\033[4;33m");
    */

    private final String color_code;
    public static final String erase ="\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
    public String get_color_code() {
        return this.color_code;
    }

    Colors(String color_code) {
        this.color_code = color_code;
    }
}
