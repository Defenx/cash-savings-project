public enum AccountJson {
    CREATE_OK("/json/accounts/create-ok.json"),
    CREATE_WITHOUT_TITLE("/json/accounts/create-without-title.json"),
    CREATE_INVALID_ENUM("/json/accounts/create-invalid-enum.json"),
    CREATE_WITH_CURRENCY_NOT_IN_ENUM("/json/accounts/create-with-currency-not-in-enum.json"),
    CREATE_MISSING_CURRENCY("/json/accounts/create-missing-currency.json"),
    CREATE_AMOUNT_SCALE_3("/json/accounts/create-amount-scale-3.json"),
    CREATE_AMOUNT_INTEGER_TOO_LONG("/json/accounts/create-amount-integer-too-long.json"),
    CREATE_TITLE_WRONG_TYPE("/json/accounts/create-title-wrong-type.json"),
    CREATE_TITLE_VALIDATE_LENGTH("/json/accounts/create-title-validate-length.json");

    private final String path;

    AccountJson(String path) {
        this.path = path;
    }

    public String load() {
        return TestUtils.loadResource(path);
    }
}