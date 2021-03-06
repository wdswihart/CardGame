package di;

import client.core.ConnectionProvider;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import models.Player;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestConnectionProviderImpl {

    private static ConnectionProvider sInstance;

    public static ConnectionProvider getInstance() {
        if (sInstance == null) {
            sInstance = mock(ConnectionProvider.class);
            when(sInstance.getAuthenticatedUser()).thenReturn(mAuthenticatedUser);
            when(sInstance.getCreatedUser()).thenReturn(mCreatedUser);
        }
        return sInstance;
    }

    private static ObjectProperty<Player> mAuthenticatedUser = new SimpleObjectProperty<>();
    private static ObjectProperty<Player> mCreatedUser = new SimpleObjectProperty<>();


    public static void resetInstance() {
        sInstance = null;
        mAuthenticatedUser = new SimpleObjectProperty<>();
        mCreatedUser = new SimpleObjectProperty<>();
    }
}
