import di.TestNavigationProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import ui.CardDetailView.CardDetailView;
import ui.DraggableView.DraggableView;
import ui.HomeView.HomeViewModel;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitPlatform.class)
class HomeViewModelTest {
    protected TestNavigationProvider navigationProvider = TestNavigationProvider.getInstance();
    protected HomeViewModel homeViewModel = TestDependencies.getInjector().getInstance(HomeViewModel.class);

    @AfterEach
    void tearDownEach() {
        //Reset the navigation provider. Bad practices but we can refactor later.
        //Tests shouldn't really affect the state of future tests.
        TestNavigationProvider.resetInstance();
    }

    @Test
    @DisplayName("NavigateToCardDetailView")
    void verifyNavigation_toCardDetailView_onCardDetailViewAction() {
        homeViewModel.getShowCardDetailViewCommand().execute();

        assertEquals(false, navigationProvider.getNavigationHistory().isEmpty());
        assertEquals(CardDetailView.class, navigationProvider.getNavigationHistory().get(0));
    }

    @Test
    @DisplayName("NavigateToDraggableView")
    void verifyNavigation_toDraggableView_onDraggableViewCommand() {
        homeViewModel.getShowDraggableViewCommand().execute();

        assertEquals(false, navigationProvider.getNavigationHistory().isEmpty());
        assertEquals(DraggableView.class, navigationProvider.getNavigationHistory().get(0));
    }
}
