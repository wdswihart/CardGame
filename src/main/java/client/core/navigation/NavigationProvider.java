package client.core.navigation;

import com.sun.tools.classfile.Type;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import client.ui.DraggableView.DraggableView;
import client.ui.DraggableView.DraggableViewModel;

import java.util.Stack;

public class NavigationProvider implements INavigationProvider {
    private static Stack<Parent> sRootStack = new Stack<>();
    private static Stage sStage;
    private static NavigationProvider sInstance = new NavigationProvider();

    private static Class sPreviousClass = null;

    private NavigationProvider() {
    }

    public static void setStage(Stage stage) {
        sStage = stage;
    }

    public static NavigationProvider getInstance() {
        return sInstance;
    }

    @Override
    public boolean navigateTo(Class view) {
        synchronized (this) {
            if (sPreviousClass != null && view.equals(sPreviousClass)) {
                return false;
            }
            sPreviousClass = view;
        }

        ViewTuple<DraggableView, DraggableViewModel> vm = FluentViewLoader.fxmlView(view).load();
        if (sStage.getScene() != null) {
            Platform.runLater(() -> {
                synchronized (this) {
                    //Remember previous view if it existed.
                    sRootStack.push(sStage.getScene().getRoot());
                    sStage.getScene().setRoot(vm.getView());
                }
            });
        } else {
            //Init scene since this is the first time.
            sStage.setScene(new Scene(vm.getView()));
        }

        return true;
    }

    @Override
    public boolean navigatePrevious() {
        synchronized (this) {
            sPreviousClass = null;
            if (sRootStack.empty()) {
                System.out.println("No previous view.");
                return false;
            }
            sStage.getScene().setRoot(sRootStack.pop());
        }
        return true;
    }
}
