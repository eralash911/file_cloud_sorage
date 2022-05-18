package model;

// Типы сообщений
public enum MessageType {
    FILE,
    FILE_REQUEST,
    FILES_LIST,
    ADD_ACCOUNT,
    AUTHENTICATION_COMPLETE,
    AUTHENTICATION_REQUEST,
    USER_INFO,
    FILE_MESSAGE,
    CURRENT_USER_ROOT_PATH,
    DELETE_REQUEST,
    CHANGE_DIRECTORY,
    CHANGE_DIRECTORY_UP,
    REGISTRATION_COMPLETE,
    CREATE_FOLDER,
    REFRESH_CLIENT_VIEW,
    CREATE_FILE,
    COPY_REQUEST,
    PASTE_REQUEST,
    CUT_REQUEST;
}