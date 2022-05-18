package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private Path currentPath;
    private Path userRootPath;
    private Path selectedCopyFile;
    private Path selectedCutFile;

    public AbstractMessageHandler() {
        currentPath = Paths.get("ServerStorage");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        switch (message.getMessageType()) {
            case AUTHENTICATION_REQUEST:
                AuthenticationRequest authenticationRequest = (AuthenticationRequest) message;
                if(AuthService.authentication(authenticationRequest.getLogin(), authenticationRequest.getPassword()) == null) {
                    ctx.writeAndFlush(new UserInfo("Account not exist."));
                    break;
                } else {
                    userRootPath = Paths.get(currentPath + "/" + authenticationRequest.getLogin());
                    ctx.writeAndFlush(new AuthenticationComplete(userRootPath.toString()));
                    ctx.writeAndFlush(new FilesList(userRootPath));
                    break;
                }
            case ADD_ACCOUNT:
                AddAccount addAccount = (AddAccount) message;
                if(AuthService.checkAccount(addAccount.getLogin()) == null) {
                    userRootPath = Paths.get(currentPath + "/" + addAccount.getLogin());
                    AuthService.addAccount(addAccount.getLogin(), addAccount.getPassword(), userRootPath.toString());
                    Files.createDirectory(userRootPath);
                    ctx.writeAndFlush(new RegistrationComplete(userRootPath.toString()));
                   ctx.writeAndFlush(new FilesList(userRootPath));
                } else {
                    ctx.writeAndFlush(new UserInfo("Account already exist"));
                }
                break;
            case CHANGE_DIRECTORY:
                ChangeDirectory changeDirectory = (ChangeDirectory) message;
                Path pathRoot = Paths.get(changeDirectory.getDirName());
                if(Files.isDirectory(pathRoot)) {
                    userRootPath = pathRoot;
                    ctx.writeAndFlush(new FilesList(userRootPath));
                    ctx.writeAndFlush(new UserInfo(userRootPath.toString()));
                } else {
                    try {
                        File file = new File(pathRoot.toString());
                        Desktop.getDesktop().open(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHANGE_DIRECTORY_UP:
                if(!userRootPath.getParent().equals(currentPath)) {
                    userRootPath = userRootPath.getParent();
                    ctx.writeAndFlush(new FilesList(userRootPath));
                    ctx.writeAndFlush(new UserInfo(userRootPath.toString()));
                }
                break;
            case CREATE_FOLDER:
                CreateFolder createFolder = (CreateFolder) message;
                Files.createDirectory(userRootPath.resolve(createFolder.getFolderName()));
               ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case CREATE_FILE:
                CreateFile createFile = (CreateFile) message;
                Files.createFile(userRootPath.resolve(createFile.getFileName()));
                ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case FILE_REQUEST:
                FileRequest req = (FileRequest) message;
                ctx.writeAndFlush(new FileMessage(userRootPath.resolve(req.getFileName())));
                break;
            case DELETE_REQUEST:
                DeleteRequest del = (DeleteRequest) message;
                Files.delete(userRootPath.resolve(del.getFileName()));
                ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case COPY_REQUEST:
                CopyRequest copyRequest = (CopyRequest) message;
                selectedCopyFile = userRootPath.resolve(copyRequest.getFileName());
                break;
            case PASTE_REQUEST:
                PasteRequest pasteRequest = (PasteRequest) message;
                if(pasteRequest.getType().equals("Copy")) {
                    Files.copy(selectedCopyFile, userRootPath.resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    ctx.writeAndFlush(new FilesList(userRootPath));
                }
                if(pasteRequest.getType().equals("Cut")) {
                    Files.move(selectedCutFile, userRootPath.resolve(selectedCutFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    ctx.writeAndFlush(new FilesList(userRootPath));
                }
                break;
            case CUT_REQUEST:
                CutRequest cutRequest = (CutRequest) message;
                selectedCutFile = userRootPath.resolve(cutRequest.getFileName());
                break;
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) message;
                Files.write(userRootPath.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                ctx.writeAndFlush(new FilesList(userRootPath));
                break;
            case REFRESH_CLIENT_VIEW:
                RefreshClientView refreshClientView = (RefreshClientView) message;
                ctx.writeAndFlush(new RefreshClientView());
                break;
        }
    }
}