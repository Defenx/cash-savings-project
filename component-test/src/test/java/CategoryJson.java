/**
 * Перечисление для загрузки заранее подготовленных JSON-файлов для тестов категорий.
 */
public enum CategoryJson {
    CREATE_OK("/json/categories/create-ok.json"),
    CREATE_WITH_NULL_NAME("/json/categories/create-with-null-name.json"),
    CREATE_WITH_INVALID_TYPE("/json/categories/create-with-invalid-type.json");

    private final String path;

    CategoryJson(String path) {
        this.path = path;
    }

    public String load() {
        return TestUtils.loadResource(path);
    }
}