public enum OperationJson {
    CREATE_INCOME_OK("/json/operations/create-income-ok.json"),
    CREATE_EXPENSE_OK("/json/operations/create-expense-ok.json"),
    CREATE_EXPENSE_POSITIVE("/json/operations/create-expense-positive.json"),
    CREATE_INCOME_NEGATIVE("/json/operations/create-income-negative.json"),
    CREATE_ZERO_AMOUNT("/json/operations/create-zero-amount.json"),
    CREATE_DESCRIPTION_TOO_LONG("/json/operations/create-description-too-long.json"),
    CREATE_CATEGORY_FOREIGN("/json/operations/create-category-foreign.json"),
    CREATE_ACCOUNT_FOREIGN("/json/operations/create-account-foreign.json"),
    CREATE_INVALID_BODY("/json/operations/create-invalid-body.json");

    private final String path;

    OperationJson(String path) {
        this.path = path;
    }

    public String load() {
        return TestUtils.loadResource(path);
    }
}
