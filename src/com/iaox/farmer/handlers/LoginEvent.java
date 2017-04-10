package com.iaox.farmer.handlers;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.constants.ResponseCode;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.listener.LoginResponseCodeListener;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;

public final class LoginEvent extends Event implements LoginResponseCodeListener {

    private final String username, password;

    public LoginEvent(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @ Override
    public final int execute() throws InterruptedException {
        if (getClient().isLoggedIn() && getLobbyButton() == null) {
            setFinished();
        } else if (getLobbyButton() != null) {
            clickLobbyButton();
        } else if (isOnWorldSelectorScreen()) {
            cancelWorldSelection();
        } else if (!isPasswordEmpty()) {
            clickCancelLoginButton();
        } else {
            login();
        }
        return random(100, 150);
    }

    private boolean isOnWorldSelectorScreen() {
        return getColorPicker().isColorAt(50, 50, Color.BLACK);
    }

    private void cancelWorldSelection() {
        if (getMouse().click(new RectangleDestination(getBot(), 712, 8, 42, 8))) {
            new ConditionalSleep(3000) {
                @ Override
                public boolean condition() throws InterruptedException {
                    return !isOnWorldSelectorScreen();
                }
            }.sleep();
        }
    }

    private boolean isPasswordEmpty() {
        return !getColorPicker().isColorAt(350, 274, Color.WHITE);
    }

    private boolean clickCancelLoginButton() {
        return getMouse().click(new RectangleDestination(getBot(), 398, 308, 126, 27));
    }

    private void login() {
        switch (getClient().getLoginUIState()) {
            case 0:
                clickExistingUsersButton();
                break;
            case 1:
                clickLoginButton();
                break;
            case 2:
                enterUserDetails();
                break;
            case 3:
                clickTryAgainButton();
                break;
        }
    }

    private void clickExistingUsersButton() {
        getMouse().click(new RectangleDestination(getBot(), 400, 280, 120, 20));
    }

    private void clickLoginButton() {
        getMouse().click(new RectangleDestination(getBot(), 240, 310, 120, 20));
    }

    private void enterUserDetails() {
        if (!getKeyboard().typeString(username)) {
            setFailed();
            return;
        }

        if (!getKeyboard().typeString(password)) {
            setFailed();
            return;
        }

        new ConditionalSleep(30_000) {
            @ Override
            public boolean condition() throws InterruptedException {
                return getLobbyButton() != null || getClient().getLoginUIState() == 3 || isDisabledMessageVisible();
            }
        }.sleep();

        if (!getClient().isLoggedIn()) {
            setFailed();
        }
    }

    private boolean clickTryAgainButton() {
        return getMouse().click(new RectangleDestination(getBot(), 318, 262, 130, 26));
    }

    private boolean isDisabledMessageVisible() {
        return getColorPicker().isColorAt(483, 205, Color.YELLOW);
    }

    private void clickLobbyButton() {
        if (getLobbyButton().interact()) {
            new ConditionalSleep(10_000) {
                @ Override
                public boolean condition() throws InterruptedException {
                    return getLobbyButton() == null;
                }
            }.sleep();
        }
    }

    private RS2Widget getLobbyButton() {
        return getWidgets().getWidgetContainingText("CLICK HERE TO PLAY");
    }

    @ Override
    public final void onResponseCode(final int responseCode) throws InterruptedException {
        if(ResponseCode.isDisabledError(responseCode)) {
            log("Login failed, account is disabled");
            setFailed();
            return;
        }

        if(ResponseCode.isConnectionError(responseCode)) {
            log("Connection error, attempts exceeded");
            setFailed();
            return;
        }
    }
}