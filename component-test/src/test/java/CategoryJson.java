public enum CategoryJson {
    CREATE_INCOME("/json/categories/create-income.json"),
    CREATE_EXPENSE("/json/categories/create-expense.json");

    private final String path;

    CategoryJson(String path) {
        this.path = path;
    }

    public String load() {
        return TestUtils.loadResource(path);
    }
}
