package client.ui.game;

import client.core.ConnectionProvider;
import client.core.GameProvider;
import client.core.navigation.INavigationProvider;
import client.ui.BaseViewModel;
import client.ui.home.HomeView;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Card;
import models.Player;
import models.requests.DefendRequest;
import models.responses.GameState;

import java.util.List;

public class GameViewModel extends BaseViewModel {
    private GameProvider mGameProvider;

    private Property<ObservableList<Card>> mPlayerHandProperty = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    private Property<ObservableList<Card>> mOpponentHandProperty = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private Property<Player> mPlayerProperty = new SimpleObjectProperty<>(new Player());
    private Property<Player> mOpponentProperty = new SimpleObjectProperty<>(new Player());

    private Property<String> mPhaseProperty = new SimpleStringProperty();

    //Probably not going to expose this via a getter.
    private Property<GameState> mGameStateProperty = new SimpleObjectProperty<>(new GameState());

    //region Player Info

    private Property<ObservableList<Card>> mPlayerDeckProperty = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    private Property<ObservableList<Card>> mOpponentDeckProperty = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private Property<ObservableList<Card>> mPlayerFieldProperty = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    private Property<ObservableList<Card>> mOpponentFieldProperty = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private Property<String> mPlayerHealthProperty = new SimpleStringProperty();
    private Property<String> mOpponentHealthProperty = new SimpleStringProperty();

    private Property<String> mWinnerProperty = new SimpleStringProperty();
    //endregion

    private Property<Card> mSelectedPlayerCardProperty = new SimpleObjectProperty<>(new Card());

    private Property<ObservableList<String>> mGameMessagesProperty = new SimpleObjectProperty<>();
    private Property<String> mMessageProperty = new SimpleStringProperty();

    //region UI State Visibility
    private Property<Boolean> mDrawButtonDisabledProperty = new SimpleBooleanProperty(false);
    private Property<Boolean> mPlayCardButtonDisabledProperty = new SimpleBooleanProperty(false);
    private Property<Boolean> mAttackButtonDisabledProperty = new SimpleBooleanProperty(false);
    private Property<Boolean> mPassTurnButtonDisabledProperty = new SimpleBooleanProperty(false);
    private Property<Boolean> mGameControlVisibleProperty = new SimpleBooleanProperty(false);
    private Property<Boolean> mWinningDisplayBoxVisibleProperty = new SimpleBooleanProperty(false);
    //endregion

    //region Commands
    private Command mDrawCommand;
    private Command mPlayCardCommand;
    private Command mPassTurnCommand;
    private Command mAttackCommand;
    private Command mQuitGameCommand;
    private Command mSendChatCommand;
    //endregion

    @Inject
    public GameViewModel(ConnectionProvider connectionProvider, INavigationProvider navigationProvider, GameProvider gameProvider) {
        super(connectionProvider, navigationProvider);
        mGameProvider = gameProvider;
        mGameStateProperty = mGameProvider.getGameStateProperty();
        onGameStateUpdated(mGameStateProperty, null, mGameStateProperty.getValue());
        mGameStateProperty.addListener(this::onGameStateUpdated);
        mGameMessagesProperty = mGameProvider.getGameMessages();

        mDrawCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                mGameProvider.drawCard();
            }
        });

        mPlayCardCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                if (mSelectedPlayerCardProperty.getValue().isDefault()) {
                    return;
                }
                mHasPlayedCard = true;
                mGameProvider.playCard(mSelectedPlayerCardProperty.getValue());
            }
        });

        mPassTurnCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                System.out.println("Passing turn.");
                mGameProvider.passTurn();
            }
        });

        mAttackCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                mGameProvider.attack(mPlayerFieldProperty.getValue());
            }
        });

        mQuitGameCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                mGameProvider.quitGame();
            }
        });

        mSendChatCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                if (mMessageProperty.getValue().isEmpty()) {
                    return;
                }

                mGameProvider.sendChat(mMessageProperty.getValue());
            }
        });
    }

    private void onGameStateUpdated(Observable observable, GameState oldVal, GameState newVal) {
        if (newVal.isDefault()) {
            //Game is over here or something went wrong.
            mNavigationProvider.navigateTo(HomeView.class);
            return;
        }

        //TODO: Handle setting player 1/2. Player should be the client. Opponent shoudl be the other guy.
        if (mConnectionProvider.getAuthenticatedUser().getValue().getUsername().equals(newVal.getPlayerOne().getUsername())) {
            updatePlayer(newVal.getPlayerOne(), newVal.getPlayerOneHand(), newVal.getPlayerOneDeck(), newVal.getPlayerOneField(), newVal.getPlayerOneHealth());
            updateOpponent(newVal.getPlayerTwo(), newVal.getPlayerTwoHand(), newVal.getPlayerTwoDeck(), newVal.getPlayerTwoField(), newVal.getPlayerTwoHealth());
        } else {
            updateOpponent(newVal.getPlayerOne(), newVal.getPlayerOneHand(), newVal.getPlayerOneDeck(), newVal.getPlayerOneField(), newVal.getPlayerOneHealth());
            updatePlayer(newVal.getPlayerTwo(), newVal.getPlayerTwoHand(), newVal.getPlayerTwoDeck(), newVal.getPlayerTwoField(), newVal.getPlayerTwoHealth());
        }

        mPhaseProperty.setValue(newVal.getState().toString());

        updateVisibleComponents(newVal);
    }


    //TODO: REmove this.
    private boolean mHasPlayedCard = false;
    private void updateVisibleComponents(GameState gameState) {
        boolean isGameOver = gameState.getStateEnum() == GameState.State.EndGame;
        boolean isDrawState = gameState.getStateEnum() == GameState.State.Draw;
        boolean isMainState = gameState.getStateEnum() == GameState.State.Main;
        boolean isDefendState = gameState.getStateEnum() == GameState.State.Defend;
        boolean isActivePlayer = gameState.getActivePlayer().getUsername().equals(mConnectionProvider.getAuthenticatedUser().getValue().getUsername());
        boolean isPlayerOne = gameState.getPlayerOne().getUsername().equals(mConnectionProvider.getAuthenticatedUser().getValue().getUsername());

        if (isDrawState) {
            mHasPlayedCard = false;
        }

        mDrawButtonDisabledProperty.setValue(isGameOver || !isActivePlayer || !isDrawState);
        mPlayCardButtonDisabledProperty.setValue(isGameOver || !isActivePlayer || !isMainState || mHasPlayedCard);
        mAttackButtonDisabledProperty.setValue(isGameOver || !isActivePlayer || !isMainState);
        mPassTurnButtonDisabledProperty.setValue(isGameOver || !isMainState);

        if (isDefendState && isActivePlayer && !isGameOver) {
            System.out.println("Showing defend dialog.");
            Platform.runLater(() -> {
                DefendDialog defendDialog = null;

                if (isPlayerOne) {
                    defendDialog = new DefendDialog(gameState.getPlayerTwoField(), gameState.getPlayerOneField());
                } else {
                    defendDialog = new DefendDialog(gameState.getPlayerOneField(), gameState.getPlayerTwoField());
                }

                DefendDialog finalDefendDialog = defendDialog;
                defendDialog.resultProperty().addListener((obs, oldVal, newVal) -> {
                    System.out.printf("Setting result property.");
                    mGameProvider.defend((DefendRequest) newVal);
                    finalDefendDialog.close();
                });
                defendDialog.show();
            });
        }

        if (isGameOver) {
            String winnerName = "";
            if (mGameStateProperty.getValue().getPlayerOneHealth() > mGameStateProperty.getValue().getPlayerTwoHealth()) {
                winnerName = mGameStateProperty.getValue().getPlayerOne().getUsername();
            } else if (mGameStateProperty.getValue().getPlayerTwoHealth() == 0) {
                winnerName = mGameStateProperty.getValue().getPlayerOne().getUsername();
            }
            mWinnerProperty.setValue("Winner: " + winnerName);
        }

        mWinningDisplayBoxVisibleProperty.setValue(isGameOver);
        mGameControlVisibleProperty.setValue(!isGameOver && isActivePlayer);
    }

    private void updatePlayer(Player player, List<Card> hand, List<Card> deck, List<Card> field, int health) {
        mPlayerProperty.setValue(player);
        mPlayerHandProperty.setValue(FXCollections.observableArrayList(hand));
        mPlayerDeckProperty.setValue(FXCollections.observableArrayList(deck));
        mPlayerFieldProperty.setValue(FXCollections.observableArrayList(field));
        mPlayerHealthProperty.setValue(String.valueOf(health));
    }

    private void updateOpponent(Player player, List<Card> hand, List<Card> deck, List<Card> field, int health) {
        mOpponentProperty.setValue(player);
        mOpponentHandProperty.setValue(FXCollections.observableArrayList(hand));
        mOpponentDeckProperty.setValue(FXCollections.observableArrayList(deck));
        mOpponentFieldProperty.setValue(FXCollections.observableArrayList(field));
        mOpponentHealthProperty.setValue(String.valueOf(health));
    }

    public Property<ObservableList<Card>> getPlayerHandProperty() {
        return mPlayerHandProperty;
    }

    public Property<ObservableList<Card>> getOpponentHandProperty() {
        return mOpponentHandProperty;
    }


    public Property<Player> getPlayerProperty() {
        return mPlayerProperty;
    }

    public Property<Player> getOpponentProperty() {
        return mOpponentProperty;
    }

    public Property<ObservableList<Card>> getPlayerDeckProperty() {
        return mPlayerDeckProperty;
    }

    public Property<ObservableList<Card>> getOpponentDeckProperty() {
        return mOpponentDeckProperty;
    }

    public Property<Boolean> getDrawButtonDisabledProperty() {
        return mDrawButtonDisabledProperty;
    }

    public Property<String> getPhaseProperty() {
        return mPhaseProperty;
    }


    public Command getDrawCommand() {
        return mDrawCommand;
    }

    public Command getPlayCardCommand() {
        return mPlayCardCommand;
    }

    public Property<Card> getSelectedPlayerCardProperty() {
        return mSelectedPlayerCardProperty;
    }

    public Property<Boolean> getPlayCardButtonDisabledProperty() {
        return mPlayCardButtonDisabledProperty;
    }

    public Property<Boolean> getGameControlVisibleProperty() {
        return mGameControlVisibleProperty;
    }

    public Command getPassTurnCommand() {
        return mPassTurnCommand;
    }

    public void setPassTurnCommand(Command passTurnCommand) {
        this.mPassTurnCommand = passTurnCommand;
    }

    public Command getAttackCommand() {
        return mAttackCommand;
    }

    public Property<Boolean> getAttackButtonDisabledProperty() {
        return mAttackButtonDisabledProperty;
    }

    public Property<ObservableList<Card>> getPlayerFieldProperty() {
        return mPlayerFieldProperty;
    }

    public Property<ObservableList<Card>> getOpponentFieldProperty() {
        return mOpponentFieldProperty;
    }

    public Property<String> getPlayerHealthProperty() {
        return mPlayerHealthProperty;
    }

    public Property<String> getOpponentHealthProperty() {
        return mOpponentHealthProperty;
    }

    public Command getQuitGameCommand() {
        return mQuitGameCommand;
    }

    public Property<Boolean> getWinningDisplayBoxVisibleProperty() {
        return mWinningDisplayBoxVisibleProperty;
    }

    public Property<String> getWinnerProperty() {
        return mWinnerProperty;
    }

    public Property<Boolean> getPassTurnButtonDisabledProperty() {
        return mPassTurnButtonDisabledProperty;
    }

    public Boolean isSpectator() {
        for (Player spectator : mGameStateProperty.getValue().getSpectatorList()) {
            if (spectator.getUsername().equals(mConnectionProvider.getAuthenticatedUser().getValue().getUsername())) {
                return true;
            }
        }
        return false;
    }

    public Property<ObservableList<String>> getGameMessagesProperty() {
        return mGameMessagesProperty;
    }

    public Command getSendChatCommand() {
        return mSendChatCommand;
    }

    public Property<String> getMessageProperty() {
        return mMessageProperty;
    }
}