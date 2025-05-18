package com.gabriel.enums;

import java.nio.file.Path;

public enum FilePath {
    FILE_PATH("D:\\Downloads\\reactApp\\project2Sem\\dados\\dados_xlsx\\");

    private final String filePath;

    FilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
