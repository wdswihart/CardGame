package client.ui.login;

import client.core.ConnectionProvider;
import client.core.navigation.INavigationProvider;
import client.ui.BaseViewModel;
import client.ui.home.HomeView;
import client.ui.createaccount.CreateAccountView;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

public class LoginViewModel extends BaseViewModel {
    // METHODS:

    private Property<String> mUsernameProperty = new SimpleStringProperty("");
    private Property<String> mPasswordProperty = new SimpleStringProperty("");

    private Command mLoginCommand;
    private Command mShowCreateAccountViewCommand;

    // CONSTRUCTORS:

    @Inject
    public LoginViewModel(ConnectionProvider connectionProvider, INavigationProvider navigationProvider) {
        super(connectionProvider, navigationProvider);
        mConnectionProvider.getAuthenticatedUser().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isDefault()) {
                mNavigationProvider.navigateTo(HomeView.class);
                mErrorProperty.setValue("");
            }
            else if ((oldVal == null || (oldVal.isDefault()) && newVal.isDefault())) {
                //If we are returned a default player, and our old player is default or null, show error.
                mErrorProperty.setValue("Invalid username or password.");
            }
            else {
                //TODO: Logout, it doesn't go here actually, but somewhere else. Maybe the BaseViewModel.
                mErrorProperty.setValue("");
            }
        });

        mLoginCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                try {
                    mConnectionProvider.loginUser(mUsernameProperty.getValue(), mPasswordProperty.getValue());
                }
                catch (Exception e) {
                    mErrorProperty.setValue("Error connecting to server.");
                }
            }
        });

        mShowCreateAccountViewCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                mNavigationProvider.navigateTo(CreateAccountView.class);
            }
        });
    }

    public Property<String> getUsernameProperty() {
        return mUsernameProperty;
    }

    public Property<String> getPasswordProperty() {
        return mPasswordProperty;
    }


    public Command getLoginCommand() {
        return mLoginCommand;
    }

    public Command getShowCreateAccountViewCommand() { return mShowCreateAccountViewCommand; }
}
