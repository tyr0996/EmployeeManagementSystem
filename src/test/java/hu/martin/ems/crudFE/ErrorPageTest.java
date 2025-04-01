package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.Page403;
import hu.martin.ems.pages.Page404;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class ErrorPageTest extends BaseCrudTest {
    @Test
    public void pageLoadFailedIllegalAccessException()  {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("robi", "robi", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);
        Page403 page = new Page403(driver, port);

        SoftAssert sa = new SoftAssert();
        sa.assertNotNull(page.getAngryCat(), "cat image");
        sa.assertNotNull(page.getPermissionMessage(), "don't have permission message");
        sa.assertNotNull(page.getTipText(), "try to get permission message");
        sa.assertNotNull(page.getCoffeeText(), "coffee break message");

        sa.assertAll();

        sa.assertEquals(page.getPermissionMessage().getText(), "You don't have permission to perform this action!");
        sa.assertEquals(page.getTipText().getText(), "Try to get permission from your boss!");
        sa.assertEquals(page.getCoffeeText().getText(), "While you wait, take a coffee break.");

        sa.assertAll();
    }

    @Test
    public void pageLoadFailedNotFoundException() throws InterruptedException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("robi", "robi", true);
        driver.get("http://localhost:" + port + "/notExistingPageURL");
        Page404 page = new Page404(driver, port);


        SoftAssert sa = new SoftAssert();
        sa.assertNotNull(page.getPlayfulCat(), "cat image");
        sa.assertNotNull(page.getNotFoundMessage(), "kitty played it message");
        sa.assertNotNull(page.getTipText(), "try again message");

        sa.assertAll();

        sa.assertEquals(page.getNotFoundMessage().getText(), "The page cannot be found because the kitty above played it somewhere");
        sa.assertEquals(page.getTipText().getText(), "Maybe try again later");

        sa.assertAll();

    }
}
