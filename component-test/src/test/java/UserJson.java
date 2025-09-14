public enum UserJson {
    SIGN_UP_OK("/json/users/sign_up_ok.json"),
    SIGN_UP_DUPE("/json/users/sign_up_dupe.json"),
    SIGN_UP_INVALID("/json/users/sign_up_invalid.json");

    private final String path;

    UserJson(String path) {
        this.path = path;
    }

    public String load() {
        return TestUtils.loadResource(path);
    }
}
